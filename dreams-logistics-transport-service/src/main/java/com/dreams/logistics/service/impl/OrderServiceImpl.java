package com.dreams.logistics.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreams.logistics.common.Constants;
import com.dreams.logistics.common.ErrorCode;
import com.dreams.logistics.constant.CommonConstant;
import com.dreams.logistics.enums.*;
import com.dreams.logistics.exception.BusinessException;
import com.dreams.logistics.model.dto.msg.OrderMsg;
import com.dreams.logistics.model.dto.msg.TradeStatusMsg;
import com.dreams.logistics.model.dto.order.OrderAddRequest;
import com.dreams.logistics.model.dto.order.OrderPickupDTO;
import com.dreams.logistics.model.dto.order.OrderSearchRequest;
import com.dreams.logistics.model.dto.order.OrderUpdateRequest;
import com.dreams.logistics.model.entity.*;
import com.dreams.logistics.model.vo.OrderVO;
import com.dreams.logistics.service.*;
import com.dreams.logistics.mapper.OrderMapper;
import com.dreams.logistics.utils.BaiduMap;
import com.dreams.logistics.utils.SecurityUtil;
import com.dreams.logistics.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.neo4j.types.Coordinate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author xiayutian
* @description 针对表【order(订单)】的数据库操作Service实现
* @createDate 2025-01-25 14:15:02
*/
@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order>
    implements OrderService{


    @Resource
    private OrderService orderService;

    @Resource
    private OrderCargoService orderCargoService;

    @Resource
    private AreaService areaService;
    @Resource
    private MQFeign mqFeign;

    @Resource
    private BaiduMap baiduMap;
    
    @Resource
    private OrderLocationService orderLocationService;

    @Resource
    private ScopeService scopeService;

    @Override
    public Page<OrderVO> page(OrderSearchRequest orderSearchRequest) {
        int current = orderSearchRequest.getCurrent();
        long size = orderSearchRequest.getPageSize();
        Order order = BeanUtil.toBean(orderSearchRequest, Order.class);
        Page<Order> orderPage = orderService.page(new Page<>(current, size),
                orderService.getQueryWrapper(orderSearchRequest));

        List<Order> records = orderPage.getRecords();

        List<OrderVO> collect = records.stream().map(orderRecord -> {

            Long id = orderRecord.getId();
            OrderCargo orderCargo = orderCargoService.getByOrderId(id);

            String name = orderCargo.getName();
            BigDecimal totalVolume = orderCargo.getTotalVolume();
            BigDecimal totalWeight = orderCargo.getTotalWeight();

            OrderVO orderVO = BeanUtil.toBean(orderRecord, OrderVO.class);
            orderVO.setName(name);
            orderVO.setVolume(totalVolume);
            orderVO.setWeight(totalWeight);

            return orderVO;
        }).collect(Collectors.toList());

        Page<OrderVO> orderVOPage = new Page<>();
        orderVOPage.setRecords(collect);

        return orderVOPage;
    }

    @Override
    public Boolean saveOrderVO(OrderAddRequest orderAddRequest) {
        Order order = new Order();
        BeanUtils.copyProperties(orderAddRequest,order);

        LocalDateTime timeOne = LocalDateTime.ofInstant(orderAddRequest.getPickupTimeRange().get(0).toInstant(), ZoneId.systemDefault());
        LocalDateTime timeTwo = LocalDateTime.ofInstant(orderAddRequest.getPickupTimeRange().get(1).toInstant(), ZoneId.systemDefault());

        order.setEstimatedStartTime(timeOne);
        order.setEstimatedArrivalTime(timeTwo);

        //省市区
        List<Long> receiverAddressId = orderAddRequest.getReceiverAddressId();
        order.setReceiverProvinceId(receiverAddressId.get(0));
        order.setReceiverCityId(receiverAddressId.get(1));
        order.setReceiverCountyId(receiverAddressId.get(2));

        List<Long> senderAddressId = orderAddRequest.getSenderAddressId();
        order.setSenderProvinceId(senderAddressId.get(0));
        order.setSenderCityId(senderAddressId.get(1));
        order.setSenderCountyId(senderAddressId.get(2));


        //获取位置信息
        OrderLocation orderLocation = buildOrderLocation(order);
        log.info("订单位置为：{}", orderLocation);

        // 距离 设置当前机构ID
        appendOtherInfo(order, orderLocation);

        order.setCreateTime(LocalDateTime.now());
        order.setPaymentStatus(OrderPaymentStatus.UNPAID.getStatus());
        if (OrderPickupType.NO_PICKUP.getCode().equals(order.getPickupType())) {
            order.setStatus(OrderStatus.OUTLETS_SINCE_SENT.getCode());
        } else {
            order.setStatus(OrderStatus.PENDING.getCode());
        }

        DcUser user = SecurityUtil.getUser();
        order.setCreateUser(user.getId());

        if (orderService.save(order)){
            OrderCargo orderCargo = new OrderCargo();
            orderCargo.setOrderId(order.getId());
            orderCargo.setName(orderAddRequest.getName());
            orderCargo.setQuantity(1);
            orderCargo.setVolume(orderAddRequest.getVolume());
            orderCargo.setWeight(orderAddRequest.getWeight());
            orderCargo.setRemark(orderAddRequest.getMark());
            orderCargo.setTotalVolume(orderAddRequest.getVolume());
            orderCargo.setTotalWeight(orderAddRequest.getWeight());
            orderCargoService.save(orderCargo);

            orderLocation.setOrderId(order.getId());
            orderLocationService.save(orderLocation);
        }

        // 生成订单mq 调度服务用来调度 之后快递员服务处理
        noticeOrderStatusChange(order, orderLocation);


        return true;
    }

    /**
     * 取件
     *
     * @param orderEntity 订单
     * @param orderLocation 位置
     */
    private void noticeOrderStatusChange(Order orderEntity, OrderLocation orderLocation) {
        String[] split = orderLocation.getSendLocation().split(",");
        double lnt = Double.parseDouble(split[0]);
        double lat = Double.parseDouble(split[1]);

        LocalDateTime estimatedArrivalTime = orderEntity.getEstimatedArrivalTime();

        OrderMsg orderMsg = OrderMsg.builder()
                .created(LocalDateTimeUtil.toEpochMilli(orderEntity.getEstimatedArrivalTime()))
                .estimatedEndTime(estimatedArrivalTime)
                .mark(orderEntity.getMark())
                .taskType(PickupDispatchTaskType.PICKUP.getCode())
                .latitude(lat)
                .longitude(lnt)
                .agencyId(orderEntity.getCurrentAgencyId())
                .orderId(orderEntity.getId())
                .build();


        //发送消息
        this.mqFeign.sendMsg(Constants.MQ.Exchanges.ORDER_DELAYED, Constants.MQ.RoutingKeys.ORDER_CREATE, orderMsg.toJson(), Constants.MQ.LOW_DELAY);
    }
    
    /**
     * 补充数据
     * @param order 订单
     * @param orderLocation 订单位置
     */
    private void appendOtherInfo(Order order, OrderLocation orderLocation) {
        // 当前机构
        order.setCurrentAgencyId(Long.valueOf(orderLocation.getSendAgentId()));

        //查询地图服务商
        String[] sendLocation = orderLocation.getSendLocation().split(",");
        double sendLnt = Double.parseDouble(sendLocation[0]);
        double sendLat = Double.parseDouble(sendLocation[1]);

        String[] receiveLocation = orderLocation.getReceiveLocation().split(",");
        double receiveLnt = Double.parseDouble(receiveLocation[0]);
        double receiveLat = Double.parseDouble(receiveLocation[1]);


        String driving = "";
        //设置地图参数
        try {
            driving = baiduMap.directionLiteByDriving(sendLat + "," + sendLnt, receiveLat + "," + receiveLnt);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (StrUtil.isEmpty(driving)) {
            return;
        }

        JSONObject jsonObject = JSONUtil.parseObj(driving);
        //距离，单位：米
        Double distance = Convert.toDouble(jsonObject.getByPath("result.routes[0].distance"));
        order.setDistance(distance);

        //时间，单位：秒
        Long duration = Convert.toLong(jsonObject.getByPath("result.routes[0].duration"), -1L);

        // 预计到达时间 这里根据地图大致估算时间 并非实际时间
        order.setEstimatedArrivalTime(LocalDateTime.now().plus(duration, ChronoUnit.SECONDS));
    }


    /**
     * 根据发收件人地址获取起止机构ID 调用机构范围微服务
     *
     * @param order 订单
     * @return 位置信息
     */
    private OrderLocation buildOrderLocation(Order order) {
        String address = senderFullAddress(order);
        HashMap<String,String> result = getAgencyId(address);
        // 网点
        String sendAgentId = result.get("agencyId");
        // 坐标
        String sendLocation = result.get("location");

        String receiverAddress = receiverFullAddress(order);
        HashMap<String,String> resultReceive = getAgencyId(receiverAddress);
        // 网点
        String receiveAgentId = resultReceive.get("agencyId");
        // 坐标
        String receiveAgentLocation = resultReceive.get("location");
        
        OrderLocation orderLocation = new OrderLocation();
        orderLocation.setOrderId(order.getId());
        orderLocation.setSendLocation(sendLocation);
        orderLocation.setSendAgentId(sendAgentId);
        orderLocation.setReceiveLocation(receiveAgentLocation);
        orderLocation.setReceiveAgentId(receiveAgentId);
        return orderLocation;
    }


    /**
     * 合并地址
     * @param entity 订单
     * @return 地址
     */
    private String senderFullAddress(Order entity) {
        Long province = entity.getSenderProvinceId();
        Long city = entity.getSenderCityId();
        Long county = entity.getSenderCountyId();

        StringBuilder area = areaAddress(province, city, county);
        area.append(entity.getSenderAddress());

        return area.toString();
    }

    /**
     * 合并地址
     * @param orderDTO 订单
     * @return 地址
     */
    private String receiverFullAddress(Order orderDTO) {
        Long province = orderDTO.getReceiverProvinceId();
        Long city = orderDTO.getReceiverCityId();
        Long county = orderDTO.getReceiverCountyId();

        StringBuilder stringBuilder = areaAddress(province, city, county);
        stringBuilder.append(orderDTO.getReceiverAddress());
        return stringBuilder.toString();
    }

    /**
     * 合并地址
     * @return 地址
     */
    private StringBuilder areaAddress(Long province, Long city, Long county) {
        StringBuilder stringBuffer = new StringBuilder();

        Area provinceAddress = areaService.findById(province);
        Area cityAddress = areaService.findById(city);
        Area countyAddress = areaService.findById(county);

        stringBuffer.append(provinceAddress.getName());
        stringBuffer.append(cityAddress.getName());
        stringBuffer.append(countyAddress.getName());
        return stringBuffer;
    }


    /**
     * 根据地址计算网点
     *
     * @param address 地址
     * @return
     */
    private HashMap<String,String> getAgencyId(String address)  {

        if (ObjectUtil.isEmpty(address)) {
            log.error("地址不能为空");
            throw new BusinessException(ErrorCode.ADDRESS_CANNOT_BE_EMPTY);
        }

        List<Double> list = baiduMap.geocodingReturn(address);
        // 提取经度和纬度
        double lng = list.get(0);
        double lat =  list.get(1);

        log.info("地址和坐标-->" + address + "--" + lng + " , " + lat);

        DecimalFormat df = new DecimalFormat("#.######");
        String lngStr = df.format(lng);
        String latStr = df.format(lat);

        String location = StrUtil.format("{},{}", lngStr, latStr);


        List<ServiceScope> serviceScopeS = scopeService.queryListByPoint(ServiceTypeEnum.codeOf(1), new GeoJsonPoint(lng, lat));

        if (CollectionUtils.isEmpty(serviceScopeS)) {
            log.error("地址不在服务范围");
            throw new BusinessException(ErrorCode.ADDRESS_IS_NOT_IN_SERVICE);
        }
        HashMap<String,String> result = new HashMap();

        result.put("agencyId", serviceScopeS.get(0).getBid().toString());
        result.put("location", location);
        return result;
    }



    @Override
    public boolean removeOrder(Long id) {
        boolean b = orderService.removeById(id);
        Long orderCargoId = orderCargoService.getByOrderId(id).getId();
        return orderCargoService.removeById(orderCargoId);
    }

    @Override
    public boolean updateOrder(OrderUpdateRequest orderUpdateRequest) {

        Order order = orderService.getById(orderUpdateRequest.getId());
        BeanUtils.copyProperties(orderUpdateRequest, order);

        LocalDateTime timeOne = LocalDateTime.ofInstant(orderUpdateRequest.getPickupTimeRange().get(0).toInstant(), ZoneId.systemDefault());
        LocalDateTime timeTwo = LocalDateTime.ofInstant(orderUpdateRequest.getPickupTimeRange().get(1).toInstant(), ZoneId.systemDefault());

        order.setEstimatedStartTime(timeOne);
        order.setEstimatedArrivalTime(timeTwo);

        List<Long> receiverAddressId = orderUpdateRequest.getReceiverAddressId();
        order.setReceiverProvinceId(receiverAddressId.get(0));
        order.setReceiverCityId(receiverAddressId.get(1));
        order.setReceiverCountyId(receiverAddressId.get(2));

        List<Long> senderAddressId = orderUpdateRequest.getSenderAddressId();
        order.setSenderProvinceId(senderAddressId.get(0));
        order.setSenderCityId(senderAddressId.get(1));
        order.setSenderCountyId(senderAddressId.get(2));
        boolean a = orderService.updateById(order);

        OrderCargo orderCargo = orderCargoService.getByOrderId(orderUpdateRequest.getId());
        orderCargo.setName(orderUpdateRequest.getName());
        orderCargo.setQuantity(1);
        orderCargo.setVolume(orderUpdateRequest.getVolume());
        orderCargo.setWeight(orderUpdateRequest.getWeight());
        orderCargo.setRemark(orderUpdateRequest.getMark());
        orderCargo.setTotalVolume(orderUpdateRequest.getVolume());
        orderCargo.setTotalWeight(orderUpdateRequest.getWeight());

        boolean b = orderCargoService.updateById(orderCargo);

        return a && b;
    }

    @Override
    public OrderVO getOrderVOById(long id) {
        Order order = orderService.getById(id);
        OrderCargo orderCargo = orderCargoService.getByOrderId(order.getId());
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order,orderVO);
        orderVO.setName(orderCargo.getName());
        orderVO.setVolume(orderCargo.getTotalVolume());
        orderVO.setWeight(orderCargo.getWeight());
        return orderVO;

    }
    @Override
    public QueryWrapper<Order> getQueryWrapper(OrderSearchRequest orderQueryRequest) {

        if (orderQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        Integer paymentStatus = orderQueryRequest.getPaymentStatus();
        BigDecimal amount = orderQueryRequest.getAmount();
        Integer orderType = orderQueryRequest.getOrderType();
        Integer pickupType = orderQueryRequest.getPickupType();


        List<Long> receiverAddressId = orderQueryRequest.getReceiverAddressId();
        List<Long> senderAddressId = orderQueryRequest.getSenderAddressId();
        List<Date> pickupTimeRange = orderQueryRequest.getPickupTimeRange();


        String receiverAddress = orderQueryRequest.getReceiverAddress();
        String receiverName = orderQueryRequest.getReceiverName();
        String receiverPhone = orderQueryRequest.getReceiverPhone();


        String senderAddress = orderQueryRequest.getSenderAddress();
        String senderName = orderQueryRequest.getSenderName();
        String senderPhone = orderQueryRequest.getSenderPhone();
        String mark = orderQueryRequest.getMark();
        String sortField = orderQueryRequest.getSortField();
        String sortOrder = orderQueryRequest.getSortOrder();

        Long createUser = orderQueryRequest.getCreateUser();

        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(createUser != null, "create_user", createUser);
        queryWrapper.eq(!Objects.isNull(paymentStatus), "payment_status", paymentStatus);
        queryWrapper.eq(!Objects.isNull(amount), "amount", amount);
        queryWrapper.eq(!Objects.isNull(orderType), "order_type", orderType);
        queryWrapper.eq(!Objects.isNull(pickupType), "pickup_type", pickupType);


        if (!Objects.isNull(receiverAddressId)){
            Long receiverProvinceId = receiverAddressId.get(0);
            Long receiverCityId = receiverAddressId.get(1);
            Long receiverCountyId = receiverAddressId.get(2);

            queryWrapper.eq(!Objects.isNull(receiverProvinceId), "receiver_province_id", receiverProvinceId);
            queryWrapper.eq(!Objects.isNull(receiverCityId), "receiver_city_id", receiverCityId);
            queryWrapper.eq(!Objects.isNull(receiverCountyId), "receiver_county_id", receiverCountyId);
        }

        if (!Objects.isNull(senderAddressId)){
            Long senderProvinceId = senderAddressId.get(0);
            Long senderCityId = senderAddressId.get(1);
            Long senderCountyId = senderAddressId.get(2);
            queryWrapper.eq(!Objects.isNull(senderProvinceId), "sender_province_id", senderProvinceId);
            queryWrapper.eq(!Objects.isNull(senderCityId), "sender_city_id", senderCityId);
            queryWrapper.eq(!Objects.isNull(senderCountyId), "sender_county_id", senderCountyId);
        }

        if (!Objects.isNull(pickupTimeRange)){
            Date estimatedStartTime = pickupTimeRange.get(0);
            Date estimatedArrivalTime = pickupTimeRange.get(1);
            queryWrapper.ge(!Objects.isNull(estimatedStartTime), "estimated_start_time", estimatedStartTime);
            queryWrapper.le(!Objects.isNull(estimatedArrivalTime), "estimated_arrival_time", estimatedArrivalTime);
        }

        queryWrapper.like(!Objects.isNull(receiverAddress), "receiver_address", receiverAddress);
        queryWrapper.like(!Objects.isNull(receiverName), "receiver_name", receiverName);
        queryWrapper.like(!Objects.isNull(receiverPhone), "receiver_phone", receiverPhone);

        queryWrapper.like(!Objects.isNull(senderAddress), "sender_address", senderAddress);
        queryWrapper.like(!Objects.isNull(senderName), "sender_name", senderName);
        queryWrapper.like(!Objects.isNull(senderPhone), "sender_phone", senderPhone);
        queryWrapper.like(!Objects.isNull(mark), "mark", mark);


        queryWrapper.eq(!Objects.isNull(paymentStatus), "payment_status", paymentStatus);


        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }


    /**
     * 更新支付状态
     *
     * @param ids    订单ID
     * @param status 状态
     */
    @Override
    public void updatePayStatus(List<Long> ids, Integer status) {
        LambdaUpdateWrapper<Order> updateWrapper = Wrappers.<Order>lambdaUpdate()
                .set(Order::getPaymentStatus, status)
                .in(Order::getId, ids);
        update(updateWrapper);
    }



    @Override
    public void updateRefundInfo(List<TradeStatusMsg> msgList) {
        // todo 退款需要更改多个业务
    }

    @Override
    public void updateStatus(List<Long> orderId, Integer code) {
        update(Wrappers.<Order>lambdaUpdate()
                .in(Order::getId, orderId)
                .set(Order::getStatus, code));
    }

    @Override
    public void orderPickup(OrderPickupDTO orderPickupDTO) {
        //5.更新订单
        Order order = new Order();
        order.setPaymentMethod(orderPickupDTO.getPayMethod());//付款方式,1.预结2到付
        order.setPaymentStatus(OrderPaymentStatus.UNPAID.getStatus());//付款状态,1.未付2已付
        order.setAmount(orderPickupDTO.getAmount());//金额
        order.setStatus(OrderStatus.PICKED_UP.getCode());//订单状态
        order.setMark(orderPickupDTO.getRemark());//备注
        order.setId(orderPickupDTO.getId());
        updateById(order);

        //6.更新订单货品
        BigDecimal volume = NumberUtil.round(orderPickupDTO.getVolume(), 4);
        BigDecimal weight = NumberUtil.round(orderPickupDTO.getWeight(), 2);

        OrderCargo cargoDTO = orderCargoService.getByOrderId(orderPickupDTO.getId());

        OrderCargo orderCargo = new OrderCargo();
        orderCargo.setName(orderPickupDTO.getGoodName());//货物名称
        orderCargo.setVolume(volume);//货品体积，单位m^3
        orderCargo.setWeight(weight);//货品重量，单位kg
        orderCargo.setTotalVolume(volume);//货品总体积，单位m^3
        orderCargo.setTotalWeight(weight);//货品总重量，单位kg
        orderCargo.setId(cargoDTO.getId());
        orderCargoService.saveOrUpdate(orderCargo);
    }
}




