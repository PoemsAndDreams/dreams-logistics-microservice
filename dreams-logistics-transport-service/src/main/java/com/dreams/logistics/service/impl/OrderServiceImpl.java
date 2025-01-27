package com.dreams.logistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreams.logistics.common.ErrorCode;
import com.dreams.logistics.constant.CommonConstant;
import com.dreams.logistics.exception.BusinessException;
import com.dreams.logistics.model.dto.msg.TradeStatusMsg;
import com.dreams.logistics.model.dto.order.OrderAddRequest;
import com.dreams.logistics.model.dto.order.OrderSearchRequest;
import com.dreams.logistics.model.dto.order.OrderUpdateRequest;
import com.dreams.logistics.model.entity.Order;
import com.dreams.logistics.model.entity.OrderCargo;
import com.dreams.logistics.model.vo.OrderVO;
import com.dreams.logistics.service.OrderCargoService;
import com.dreams.logistics.service.OrderService;
import com.dreams.logistics.mapper.OrderMapper;
import com.dreams.logistics.utils.SqlUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
* @author xiayutian
* @description 针对表【order(订单)】的数据库操作Service实现
* @createDate 2025-01-25 14:15:02
*/
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order>
    implements OrderService{


    @Resource
    private OrderService orderService;

    @Resource
    private OrderCargoService orderCargoService;

    @Override
    public Page<OrderVO> page(OrderSearchRequest orderSearchRequest) {
        int current = orderSearchRequest.getCurrent();
        long size = orderSearchRequest.getPageSize();
        Order order = new Order();
        BeanUtils.copyProperties(orderSearchRequest, order);
        Page<Order> orderPage = orderService.page(new Page<>(current, size),
                orderService.getQueryWrapper(orderSearchRequest));

        List<Order> records = orderPage.getRecords();

        List<OrderVO> collect = records.stream().map(orderRecord -> {
            OrderVO orderVO = new OrderVO();
            Long id = orderRecord.getId();
            OrderCargo orderCargo = orderCargoService.getByOrderId(id);

            String name = orderCargo.getName();
            BigDecimal totalVolume = orderCargo.getTotalVolume();
            BigDecimal totalWeight = orderCargo.getTotalWeight();

            BeanUtils.copyProperties(orderRecord, orderVO);
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
        BeanUtils.copyProperties(orderAddRequest, order);

        order.setEstimatedStartTime(orderAddRequest.getPickupTimeRange().get(0));
        order.setEstimatedArrivalTime(orderAddRequest.getPickupTimeRange().get(1));

        List<Long> receiverAddressId = orderAddRequest.getReceiverAddressId();
        order.setReceiverProvinceId(receiverAddressId.get(0));
        order.setReceiverMemberId(receiverAddressId.get(1));
        order.setReceiverCityId(receiverAddressId.get(2));

        List<Long> senderAddressId = orderAddRequest.getSenderAddressId();
        order.setSenderProvinceId(senderAddressId.get(0));
        order.setSenderCountyId(senderAddressId.get(1));
        order.setSenderCityId(senderAddressId.get(2));

        boolean s = orderService.save(order);
        OrderCargo orderCargo = new OrderCargo();
        orderCargo.setOrderId(order.getId());
        orderCargo.setName(orderAddRequest.getName());
        orderCargo.setQuantity(1);
        orderCargo.setVolume(orderAddRequest.getVolume());
        orderCargo.setWeight(orderAddRequest.getWeight());
        orderCargo.setRemark(orderAddRequest.getMark());
        orderCargo.setTotalVolume(orderAddRequest.getVolume());
        orderCargo.setTotalWeight(orderAddRequest.getWeight());
        boolean c = orderCargoService.save(orderCargo);

        return s && c;
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
        order.setEstimatedStartTime(orderUpdateRequest.getPickupTimeRange().get(0));
        order.setEstimatedArrivalTime(orderUpdateRequest.getPickupTimeRange().get(1));

        List<Long> receiverAddressId = orderUpdateRequest.getReceiverAddressId();
        order.setReceiverProvinceId(receiverAddressId.get(0));
        order.setReceiverMemberId(receiverAddressId.get(1));
        order.setReceiverCityId(receiverAddressId.get(2));

        List<Long> senderAddressId = orderUpdateRequest.getSenderAddressId();
        order.setSenderProvinceId(senderAddressId.get(0));
        order.setSenderCountyId(senderAddressId.get(1));
        order.setSenderCityId(senderAddressId.get(2));
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
        BeanUtils.copyProperties(orderVO,orderVO);
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


        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
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
}




