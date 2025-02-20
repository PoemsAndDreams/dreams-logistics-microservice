package com.dreams.logistics.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreams.logistics.common.Constants;
import com.dreams.logistics.enums.PickupDispatchTaskType;
import com.dreams.logistics.enums.TransportOrderSchedulingStatus;
import com.dreams.logistics.enums.TransportOrderStatus;
import com.dreams.logistics.enums.WorkExceptionEnum;
import com.dreams.logistics.exception.BusinessException;
import com.dreams.logistics.mapper.TransportOrderTaskMapper;
import com.dreams.logistics.model.dto.line.TransportLineNode;
import com.dreams.logistics.model.dto.msg.OrderMsg;
import com.dreams.logistics.model.dto.msg.TransportInfoMsg;
import com.dreams.logistics.model.dto.msg.TransportOrderMsg;
import com.dreams.logistics.model.dto.msg.TransportOrderStatusMsg;
import com.dreams.logistics.model.dto.transport.TransportOrderDTO;
import com.dreams.logistics.model.dto.transport.request.TransportOrderQueryDTO;
import com.dreams.logistics.model.entity.*;
import com.dreams.logistics.service.*;
import com.dreams.logistics.mapper.TransportOrderMapper;
import com.dreams.logistics.utils.ObjectUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author xiayutian
* @description 针对表【transport_order(运单表)】的数据库操作Service实现
* @createDate 2025-02-05 15:19:37
*/
@Service
public class TransportOrderServiceImpl extends ServiceImpl<TransportOrderMapper, TransportOrder>
    implements TransportOrderService{

    @Resource
    private TransportTaskService transportTaskService;
    @Resource
    private TransportOrderTaskMapper transportOrderTaskMapper;
    @Resource
    private MQFeign mqFeign;
    @Resource
    private OrderService orderService;

    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private OrderCargoService orderCargoService;

    @Resource
    private OrderLocationService orderLocationService;
    
    @Resource
    private TransportLineService transportLineService;


    @Resource
    private IdentifierGenerator identifierGenerator;

    @Override
    @Transactional
    public TransportOrder orderToTransportOrder(Long orderId) {
        //幂等性校验
        TransportOrder transportOrderEntity = this.findByOrderId(orderId);
        if (ObjectUtil.isNotEmpty(transportOrderEntity)) {
            return transportOrderEntity;
        }

        //查询订单
        Order detailByOrder = this.orderService.getById(orderId);
        if (ObjectUtil.isEmpty(detailByOrder)) {
            throw new BusinessException(WorkExceptionEnum.ORDER_NOT_FOUND);
        }

        //校验货物的重量和体积数据
        OrderCargo cargoDto = orderCargoService.getByOrderId(orderId);
        
        if (ObjectUtil.isEmpty(cargoDto)) {
            throw new BusinessException(WorkExceptionEnum.ORDER_CARGO_NOT_FOUND);
        }

        //校验位置信息
        OrderLocation orderLocation = orderLocationService.getByOrderId(orderId);
        if (ObjectUtil.isEmpty(orderLocation)) {
            throw new BusinessException(WorkExceptionEnum.ORDER_LOCATION_NOT_FOUND);
        }

        Long sendAgentId = Convert.toLong(orderLocation.getSendAgentId());//起始网点id
        Long receiveAgentId = Convert.toLong(orderLocation.getReceiveAgentId());//终点网点id

        //默认参与调度
        boolean isDispatch = true;
        TransportLineNode transportLineNode = null;
        if (ObjectUtil.equal(sendAgentId, receiveAgentId)) {
            //起点、终点是同一个网点，不需要规划路线，直接发消息生成派件任务即可
            isDispatch = false;
        } else {
            //根据起始机构规划运输路线
            transportLineNode = this.transportLineService.queryPathByDispatchMethod(sendAgentId, receiveAgentId);
            if (ObjectUtil.isEmpty(transportLineNode) || CollUtil.isEmpty(transportLineNode.getNodeList())) {
                throw new BusinessException(WorkExceptionEnum.TRANSPORT_LINE_NOT_FOUND);
            }
        }

        //创建新的运单对象
        TransportOrder transportOrder = new TransportOrder();

        transportOrder.setId(Convert.toStr(identifierGenerator.nextId(transportOrder))); //设置id
        transportOrder.setOrderId(orderId);//订单ID
        transportOrder.setStartAgencyId(sendAgentId);//起始网点id
        transportOrder.setEndAgencyId(receiveAgentId);//终点网点id
        transportOrder.setCurrentAgencyId(sendAgentId);//当前所在机构id

        if (ObjectUtil.isNotEmpty(transportLineNode)) {
            transportOrder.setStatus(TransportOrderStatus.CREATED);//运单状态(1.新建 2.已装车 3.运输中 4.到达终端网点 5.已签收 6.拒收)
            transportOrder.setSchedulingStatus(TransportOrderSchedulingStatus.TO_BE_SCHEDULED);//调度状态(1.待调度2.未匹配线路3.已调度)
            transportOrder.setNextAgencyId(transportLineNode.getNodeList().get(1).getId());//下一个机构id
            transportOrder.setTransportLine(JSONUtil.toJsonStr(transportLineNode));//完整的运输路线
        } else {
            //下个网点就是当前网点
            transportOrder.setNextAgencyId(sendAgentId);
            transportOrder.setStatus(TransportOrderStatus.ARRIVED_END);//运单状态(1.新建 2.已装车 3.运输中 4.到达终端网点 5.已签收 6.拒收)
            transportOrder.setSchedulingStatus(TransportOrderSchedulingStatus.SCHEDULED);//调度状态(1.待调度2.未匹配线路3.已调度)
        }

        transportOrder.setTotalVolume(cargoDto.getVolume());//货品总体积，单位m^3
        transportOrder.setTotalWeight(cargoDto.getWeight());//货品总重量，单位kg
        transportOrder.setIsRejection(false); //默认非拒收订单

        boolean result = super.save(transportOrder);
        if (result) {

            if (isDispatch) {
                //发送消息到调度中心，进行调度
                this.sendTransportOrderMsgToDispatch(transportOrder);
            } else {
                // 不需要调度 发送消息更新订单状态
                this.sendUpdateStatusMsg(ListUtil.toList(transportOrder.getId()), TransportOrderStatus.ARRIVED_END);
                //不需要调度，发送消息生成派件任务
                this.sendDispatchTaskMsgToDispatch(transportOrder);
            }

            //发消息通知其他系统，运单已经生成
            String msg = TransportOrderMsg.builder()
                    .id(transportOrder.getId())
                    .orderId(transportOrder.getOrderId())
                    .created(DateUtil.current())
                    .build().toJson();
            this.mqFeign.sendMsg(Constants.MQ.Exchanges.TRANSPORT_ORDER_DELAYED,
                    Constants.MQ.RoutingKeys.TRANSPORT_ORDER_CREATE, msg, Constants.MQ.NORMAL_DELAY);

            return transportOrder;
        }
        //保存失败
        throw new BusinessException(WorkExceptionEnum.TRANSPORT_ORDER_SAVE_ERROR);
    }

    @Override
    public Page<TransportOrder> findByPage(TransportOrderQueryDTO transportOrderQueryDTO) {

        Page<TransportOrder> iPage = new Page<>(transportOrderQueryDTO.getPage(), transportOrderQueryDTO.getPageSize());

        //设置查询条件
        LambdaQueryWrapper<TransportOrder> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(ObjectUtil.isNotEmpty(transportOrderQueryDTO.getId()), TransportOrder::getId, transportOrderQueryDTO.getId());
        lambdaQueryWrapper.like(ObjectUtil.isNotEmpty(transportOrderQueryDTO.getOrderId()), TransportOrder::getOrderId, transportOrderQueryDTO.getOrderId());
        lambdaQueryWrapper.eq(ObjectUtil.isNotEmpty(transportOrderQueryDTO.getStatus()), TransportOrder::getStatus, transportOrderQueryDTO.getStatus());
        lambdaQueryWrapper.eq(ObjectUtil.isNotEmpty(transportOrderQueryDTO.getSchedulingStatus()), TransportOrder::getSchedulingStatus, transportOrderQueryDTO.getSchedulingStatus());

        lambdaQueryWrapper.eq(ObjectUtil.isNotEmpty(transportOrderQueryDTO.getStartAgencyId()), TransportOrder::getStartAgencyId, transportOrderQueryDTO.getStartAgencyId());
        lambdaQueryWrapper.eq(ObjectUtil.isNotEmpty(transportOrderQueryDTO.getEndAgencyId()), TransportOrder::getEndAgencyId, transportOrderQueryDTO.getEndAgencyId());
        lambdaQueryWrapper.eq(ObjectUtil.isNotEmpty(transportOrderQueryDTO.getCurrentAgencyId()), TransportOrder::getCurrentAgencyId, transportOrderQueryDTO.getCurrentAgencyId());
        lambdaQueryWrapper.orderByDesc(TransportOrder::getCreated);

        return super.page(iPage, lambdaQueryWrapper);
    }

    @Override
    public TransportOrder findByOrderId(Long orderId) {
        LambdaQueryWrapper<TransportOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TransportOrder::getOrderId, orderId);
        return super.getOne(queryWrapper);
    }

    @Override
    public List<TransportOrder> findByOrderIds(Long[] orderIds) {
        LambdaQueryWrapper<TransportOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(TransportOrder::getOrderId, orderIds);
        return super.list(queryWrapper);
    }

    @Override
    public List<TransportOrder> findByIds(String[] ids) {
        LambdaQueryWrapper<TransportOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(TransportOrder::getId, ids);
        return super.list(queryWrapper);
    }

    @Override
    public List<TransportOrder> searchById(String id) {
        LambdaQueryWrapper<TransportOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(TransportOrder::getId, id);
        return super.list(queryWrapper);
    }

    @Override
    public boolean updateStatus(List<String> ids, TransportOrderStatus transportOrderStatus) {
        if (CollUtil.isEmpty(ids)) {
            return false;
        }

        if (TransportOrderStatus.CREATED == transportOrderStatus) {
            //修改订单状态不能为 新建 状态
            throw new BusinessException(WorkExceptionEnum.TRANSPORT_ORDER_STATUS_NOT_CREATED);
        }

        List<TransportOrder> transportOrderList;
        //判断是否为拒收状态，如果是拒收需要重新查询路线，将包裹逆向回去
        if (TransportOrderStatus.REJECTED == transportOrderStatus) {
            //查询运单列表
            transportOrderList = super.listByIds(ids);
            for (TransportOrder transportOrderEntity : transportOrderList) {
                //设置为拒收运单
                transportOrderEntity.setIsRejection(true);
                //根据起始机构规划运输路线，这里要将起点和终点互换
                Long sendAgentId = transportOrderEntity.getEndAgencyId();//起始网点id
                Long receiveAgentId = transportOrderEntity.getStartAgencyId();//终点网点id

                //默认参与调度
                boolean isDispatch = true;
                if (ObjectUtil.equal(sendAgentId, receiveAgentId)) {
                    //相同节点，无需调度，直接生成派件任务
                    isDispatch = false;
                } else {
                    TransportLineNode transportLineNodeDTO = this.transportLineService.queryPathByDispatchMethod(sendAgentId, receiveAgentId);
                    if (ObjectUtil.hasEmpty(transportLineNodeDTO, transportLineNodeDTO.getNodeList())) {
                        throw new BusinessException(WorkExceptionEnum.TRANSPORT_LINE_NOT_FOUND);
                    }

                    //删除掉第一个机构，逆向回去的第一个节点就是当前所在节点
                    transportLineNodeDTO.getNodeList().remove(0);
                    transportOrderEntity.setSchedulingStatus(TransportOrderSchedulingStatus.TO_BE_SCHEDULED);//调度状态：待调度
                    transportOrderEntity.setCurrentAgencyId(sendAgentId);//当前所在机构id
                    transportOrderEntity.setNextAgencyId(transportLineNodeDTO.getNodeList().get(0).getId());//下一个机构id

                    //获取到原有节点信息
                    TransportLineNode transportLineNode = JSONUtil.toBean(transportOrderEntity.getTransportLine(), TransportLineNode.class);
                    //将逆向节点追加到节点列表中
                    transportLineNode.getNodeList().addAll(transportLineNodeDTO.getNodeList());
                    //合并成本
                    transportLineNode.setCost(NumberUtil.add(transportLineNode.getCost(), transportLineNodeDTO.getCost()));
                    transportOrderEntity.setTransportLine(JSONUtil.toJsonStr(transportLineNode));//完整的运输路线
                }
                transportOrderEntity.setStatus(TransportOrderStatus.REJECTED);

                if (isDispatch) {
                    //发送消息参与调度
                    this.sendTransportOrderMsgToDispatch(transportOrderEntity);
                } else {
                    //不需要调度，发送消息生成派件任务
                    transportOrderEntity.setStatus(TransportOrderStatus.ARRIVED_END);
                    this.sendDispatchTaskMsgToDispatch(transportOrderEntity);
                }
            }
        } else {
            //根据id列表封装成运单对象列表
            transportOrderList = ids.stream().map(id -> {
                //获取将发往的目的地机构
                Long nextAgencyId = this.getById(id).getNextAgencyId();
                Organization organDTO = userFeignClient.getOrganizationById(String.valueOf(nextAgencyId));

                //构建消息实体类
                String info = CharSequenceUtil.format("快件发往【{}】", organDTO.getName());
                String transportInfoMsg = TransportInfoMsg.builder()
                        .transportOrderId(id)//运单id
                        .status("运送中")//消息状态
                        .info(info)//消息详情
                        .created(DateUtil.current())//创建时间
                        .build().toJson();
                //发送运单跟踪消息
                this.mqFeign.sendMsg(Constants.MQ.Exchanges.TRANSPORT_INFO, Constants.MQ.RoutingKeys.TRANSPORT_INFO_APPEND, transportInfoMsg);

                //封装运单对象
                TransportOrder transportOrderEntity = new TransportOrder();
                transportOrderEntity.setId(id);
                transportOrderEntity.setStatus(transportOrderStatus);
                return transportOrderEntity;
            }).collect(Collectors.toList());
        }

        //批量更新数据
        boolean result = super.updateBatchById(transportOrderList);

        //发消息通知其他系统运单状态的变化
        this.sendUpdateStatusMsg(ids, transportOrderStatus);

        return result;
    }

    private void sendUpdateStatusMsg(List<String> ids, TransportOrderStatus transportOrderStatus) {
        String msg = TransportOrderStatusMsg.builder()
                .idList(ids)
                .statusName(transportOrderStatus.name())
                .statusCode(transportOrderStatus.getCode())
                .build().toJson();

        //将状态名称写入到路由key中，方便消费方选择性的接收消息
        String routingKey = Constants.MQ.RoutingKeys.TRANSPORT_ORDER_UPDATE_STATUS_PREFIX + transportOrderStatus.name();
        this.mqFeign.sendMsg(Constants.MQ.Exchanges.TRANSPORT_ORDER_DELAYED, routingKey, msg, Constants.MQ.LOW_DELAY);
    }

    @Override
    public boolean updateByTaskId(Long taskId) {
        //通过运输任务查询运单id列表
        List<String> transportOrderIdList = this.transportTaskService.queryTransportOrderIdListById(taskId);

        if (CollUtil.isEmpty(transportOrderIdList)) {
            return false;
        }
        //查询运单列表
        List<TransportOrder> transportOrderList = super.listByIds(transportOrderIdList);

        //查询运单中下个机构的信息
        List<Long> nextAgencyIds = transportOrderList.stream().map(TransportOrder::getNextAgencyId).distinct().filter(Objects::nonNull).collect(Collectors.toList());
        List<Organization> organDTOS = userFeignClient.queryByIds(nextAgencyIds);
        Map<String, Organization> organMap = organDTOS.stream().collect(Collectors.toMap(Organization::getId, dto -> dto));

        for (TransportOrder transportOrder : transportOrderList) {
            //获取将发往的目的地机构
            Organization organDTO = organMap.get(transportOrder.getNextAgencyId().toString());

            //构建消息实体类
            String info = CharSequenceUtil.format("快件到达【{}】", organDTO.getName());
            String transportInfoMsg = TransportInfoMsg.builder()
                    .transportOrderId(transportOrder.getId())//运单id
                    .status("运送中")//消息状态
                    .info(info)//消息详情
                    .created(DateUtil.current())//创建时间
                    .build().toJson();
            //发送运单跟踪消息
            this.mqFeign.sendMsg(Constants.MQ.Exchanges.TRANSPORT_INFO, Constants.MQ.RoutingKeys.TRANSPORT_INFO_APPEND, transportInfoMsg);

            //设置当前所在机构id为下一个机构id
            transportOrder.setCurrentAgencyId(transportOrder.getNextAgencyId());
            //解析完整的运输链路，找到下一个机构id
            String transportLine = transportOrder.getTransportLine();
            JSONObject jsonObject = JSONUtil.parseObj(transportLine);
            Long nextAgencyId = 0L;
            JSONArray nodeList = jsonObject.getJSONArray("nodeList");
            //这里反向循环主要是考虑到拒收的情况，路线中会存在相同的节点，始终可以查找到后面的节点
            //正常：A B C D E ，拒收：A B C D E D C B A
            for (int i = nodeList.size() - 1; i >= 0; i--) {
                JSONObject node = (JSONObject) nodeList.get(i);
                Long agencyId = node.getLong("bid");
                if (ObjectUtil.equal(agencyId, transportOrder.getCurrentAgencyId())) {
                    if (i == nodeList.size() - 1) {
                        //已经是最后一个节点了，也就是到最后一个机构了
                        nextAgencyId = agencyId;
                        transportOrder.setStatus(TransportOrderStatus.ARRIVED_END);
                        //发送消息更新状态
                        this.sendUpdateStatusMsg(ListUtil.toList(transportOrder.getId()), TransportOrderStatus.ARRIVED_END);
                    } else {
                        //后面还有节点
                        nextAgencyId = ((JSONObject) nodeList.get(i + 1)).getLong("bid");
                        //设置运单状态为待调度
                        transportOrder.setSchedulingStatus(TransportOrderSchedulingStatus.TO_BE_SCHEDULED);
                    }
                    break;
                }
            }
            //设置下一个节点id
            transportOrder.setNextAgencyId(nextAgencyId);

            //如果运单没有到达终点，需要发送消息到运单调度的交换机中
            //如果已经到达最终网点，需要发送消息，进行分配快递员作业
            if (ObjectUtil.notEqual(transportOrder.getStatus(), TransportOrderStatus.ARRIVED_END)) {
                this.sendTransportOrderMsgToDispatch(transportOrder);
            } else {
                //发送消息生成派件任务
                this.sendDispatchTaskMsgToDispatch(transportOrder);
            }
        }
        //批量更新运单
        return super.updateBatchById(transportOrderList);
    }

    private void sendDispatchTaskMsgToDispatch(TransportOrder transportOrder) {
        //预计完成时间，如果是中午12点到的快递，当天22点前，否则，第二天22点前
        int offset = 0;
        if (LocalDateTime.now().getHour() >= 12) {
            offset = 1;
        }
        LocalDateTime estimatedEndTime = DateUtil.offsetDay(new Date(), offset)
                .setField(DateField.HOUR_OF_DAY, 22)
                .setField(DateField.MINUTE, 0)
                .setField(DateField.SECOND, 0)
                .setField(DateField.MILLISECOND, 0).toLocalDateTime();

        //发送分配快递员派件任务的消息
        OrderMsg orderMsg = OrderMsg.builder()
                .agencyId(transportOrder.getCurrentAgencyId())
                .orderId(transportOrder.getOrderId())
                .created(DateUtil.current())
                .taskType(PickupDispatchTaskType.DISPATCH.getCode()) //派件任务
                .mark("系统提示：派件前请于收件人电话联系.")
                .estimatedEndTime(estimatedEndTime).build();

        //发送消息
        this.sendPickupDispatchTaskMsgToDispatch(transportOrder, orderMsg);
    }

    /**
     * 发送消息到调度中心，用于生成取派件任务
     *
     * @param transportOrder 运单
     * @param orderMsg       消息内容
     */
    @Override
    public void sendPickupDispatchTaskMsgToDispatch(TransportOrder transportOrder, OrderMsg orderMsg) {
        //查询订单对应的位置信息
        OrderLocation orderLocation = this.orderLocationService.getByOrderId(orderMsg.getOrderId());

        //(1)运单为空：取件任务取消，取消原因为返回网点；重新调度位置取寄件人位置
        //(2)运单不为空：生成的是派件任务，需要根据拒收状态判断位置是寄件人还是收件人
        // 拒收：寄件人  其他：收件人
        String location;
        if (ObjectUtil.isEmpty(transportOrder)) {
            location = orderLocation.getSendLocation();
        } else {
            location = transportOrder.getIsRejection() ? orderLocation.getSendLocation() : orderLocation.getReceiveLocation();
        }

        Double[] coordinate = Convert.convert(Double[].class, StrUtil.split(location, ","));
        Double longitude = coordinate[0];
        Double latitude = coordinate[1];

        //设置消息中的位置信息
        orderMsg.setLongitude(longitude);
        orderMsg.setLatitude(latitude);

        //发送消息,用于生成取派件任务
        this.mqFeign.sendMsg(Constants.MQ.Exchanges.ORDER_DELAYED, Constants.MQ.RoutingKeys.ORDER_CREATE,
                orderMsg.toJson(), Constants.MQ.NORMAL_DELAY);
    }

    /**
     * 发送运单消息到调度中，参与调度
     */
    private void sendTransportOrderMsgToDispatch(TransportOrder transportOrder) {
        Map<Object, Object> msg = MapUtil.builder()
                .put("transportOrderId", transportOrder.getId())
                .put("currentAgencyId", transportOrder.getCurrentAgencyId())
                .put("nextAgencyId", transportOrder.getNextAgencyId())
                .put("totalWeight", transportOrder.getTotalWeight())
                .put("totalVolume", transportOrder.getTotalVolume())
                .put("created", System.currentTimeMillis()).build();
        String jsonMsg = JSONUtil.toJsonStr(msg);
        //发送消息，延迟5秒，确保本地事务已经提交，可以查询到数据
        this.mqFeign.sendMsg(Constants.MQ.Exchanges.TRANSPORT_ORDER_DELAYED,
                Constants.MQ.RoutingKeys.JOIN_DISPATCH, jsonMsg, Constants.MQ.LOW_DELAY);
    }


    /**
     * 根据运输任务id分页查询运单信息
     *
     * @param page             页码
     * @param pageSize         页面大小
     * @param taskId           运输任务id
     * @param transportOrderId 运单id
     * @return 运单对象分页数据
     */
    @Override
    public Page<TransportOrderDTO> pageQueryByTaskId(Integer page, Integer pageSize, String taskId, String transportOrderId) {
        //构建分页查询条件
        Page<TransportOrderTask> transportOrderTaskPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<TransportOrderTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjectUtil.isNotEmpty(taskId), TransportOrderTask::getTransportTaskId, taskId)
                .like(ObjectUtil.isNotEmpty(transportOrderId), TransportOrderTask::getTransportOrderId, transportOrderId)
                .orderByDesc(TransportOrderTask::getCreated);

        //根据运输任务id、运单id查询运输任务与运单关联关系表
        Page<TransportOrderTask> pageResult = transportOrderTaskMapper.selectPage(transportOrderTaskPage, queryWrapper);
        if (ObjectUtil.isEmpty(pageResult.getRecords())) {
            return new Page<>();
        }

        //根据运单id查询运单，并转化为dto
        List<String> transportOrderIds = pageResult.getRecords().stream().map(TransportOrderTask::getTransportOrderId).collect(Collectors.toList());
        List<TransportOrder> entities = baseMapper.selectBatchIds(transportOrderIds);

        //构建分页结果
        List<TransportOrderDTO> orderDTOS = BeanUtil.copyToList(entities, TransportOrderDTO.class);
        Page<TransportOrderDTO> pageResponse = new Page<>(page, pageSize);
        pageResponse.setRecords(orderDTOS);
        pageResponse.setTotal(pageResult.getTotal());
        return pageResponse;
    }
}




