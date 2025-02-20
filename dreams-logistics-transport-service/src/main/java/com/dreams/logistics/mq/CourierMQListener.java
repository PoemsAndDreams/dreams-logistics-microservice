package com.dreams.logistics.mq;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import com.dreams.logistics.common.Constants;
import com.dreams.logistics.enums.*;
import com.dreams.logistics.exception.BusinessException;
import com.dreams.logistics.model.dto.msg.CourierMsg;
import com.dreams.logistics.model.dto.msg.CourierTaskMsg;
import com.dreams.logistics.model.dto.msg.TransportInfoMsg;
import com.dreams.logistics.model.entity.Order;
import com.dreams.logistics.model.entity.PickupDispatchTask;
import com.dreams.logistics.model.entity.TransportOrder;
import com.dreams.logistics.service.MQFeign;
import com.dreams.logistics.service.OrderService;
import com.dreams.logistics.service.PickupDispatchTaskService;
import com.dreams.logistics.service.TransportOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 快递员的消息处理，该处理器处理两个消息：
 * 1. 生成快递员取派件任务
 * 2. 快递员取件成功，订单转运单
 */
@Component
@Slf4j
public class CourierMQListener {

    @Resource
    private TransportOrderService transportOrderService;
    @Resource
    private MQFeign mqFeign;
    @Resource
    private PickupDispatchTaskService pickupDispatchTaskService;
    @Resource
    private OrderService orderService;

    /**
     * 生成快递员取派件任务
     *
     * @param msg 消息
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = Constants.MQ.Queues.WORK_PICKUP_DISPATCH_TASK_CREATE),
            exchange = @Exchange(name = Constants.MQ.Exchanges.PICKUP_DISPATCH_TASK_DELAYED, type = ExchangeTypes.TOPIC, delayed = Constants.MQ.DELAYED),
            key = Constants.MQ.RoutingKeys.PICKUP_DISPATCH_TASK_CREATE
    ))
    public void listenCourierTaskMsg(String msg) {
        //{"taskType":1,"orderId":225125208064,"created":1654767899885,"courierId":1001,"agencyId":8001,"estimatedStartTime":1654224658728,"mark":"带包装"}
        log.info("接收到快递员任务的消息 >>> msg = {}", msg);
        //解析消息
        CourierTaskMsg courierTaskMsg = JSONUtil.toBean(msg, CourierTaskMsg.class);

        //幂等性处理：判断订单对应的取派件任务是否存在，判断条件：订单号+任务状态
        List<PickupDispatchTask> list = this.pickupDispatchTaskService.findByOrderId(courierTaskMsg.getOrderId(), PickupDispatchTaskType.codeOf(courierTaskMsg.getTaskType()));
        for (PickupDispatchTask pickupDispatchTaskEntity : list) {
            if (pickupDispatchTaskEntity.getStatus() == PickupDispatchTaskStatus.NEW) {
                //消息重复消费
                return;
            }
        }

        // 订单不存在 不进行调度
        Order order = orderService.getById(courierTaskMsg.getOrderId());
        if (ObjectUtil.isEmpty(order)) {
            return;
        }
        // 如果已经取消或者删除 则不进行调度
        if (order.getStatus().equals(OrderStatus.CANCELLED.getCode()) || order.getStatus().equals(OrderStatus.DEL.getCode())) {
            return;
        }

        PickupDispatchTask pickupDispatchTask = BeanUtil.toBean(courierTaskMsg, PickupDispatchTask.class);
        //任务类型
        pickupDispatchTask.setTaskType(PickupDispatchTaskType.codeOf(courierTaskMsg.getTaskType()));

        //预计开始时间，结束时间向前推一小时
        LocalDateTime estimatedStartTime = LocalDateTimeUtil.offset(pickupDispatchTask.getEstimatedEndTime(), -1, ChronoUnit.HOURS);
        pickupDispatchTask.setEstimatedStartTime(estimatedStartTime);
        // 默认未签收状态
        pickupDispatchTask.setSignStatus(PickupDispatchTaskSignStatus.NOT_SIGNED);

        //分配状态
        if (ObjectUtil.isNotEmpty(pickupDispatchTask.getCourierId())) {
            pickupDispatchTask.setAssignedStatus(PickupDispatchTaskAssignedStatus.DISTRIBUTED);
        } else {
            pickupDispatchTask.setAssignedStatus(PickupDispatchTaskAssignedStatus.MANUAL_DISTRIBUTED);
        }

        PickupDispatchTask result = this.pickupDispatchTaskService.saveTaskPickupDispatch(pickupDispatchTask);
        if (result == null) {
            //保存任务失败
            throw new BusinessException(StrUtil.format("快递员任务保存失败 msg = {}", msg));
        }
    }

    /**
     * 快递员取件成功
     *
     * @param msg 消息
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = Constants.MQ.Queues.WORK_COURIER_PICKUP_SUCCESS),
            exchange = @Exchange(name = Constants.MQ.Exchanges.COURIER, type = ExchangeTypes.TOPIC),
            key = Constants.MQ.RoutingKeys.COURIER_PICKUP
    ))
    public void listenCourierPickupMsg(String msg) {
        //解析消息
        CourierMsg courierMsg = JSONUtil.toBean(msg, CourierMsg.class);

        //订单转运单
        TransportOrder transportOrder = this.transportOrderService.orderToTransportOrder(courierMsg.getOrderId());

        //发送运单跟踪消息
        String info = StrUtil.format("快递员已取件， 取件人【{}，电话 {}】", courierMsg.getCourierName(), courierMsg.getCourierMobile());
        //构建消息实体类
        String transportInfoMsg = TransportInfoMsg.builder()
                .transportOrderId(transportOrder.getId())//运单id
                .status("已取件")//消息状态
                .info(info)//消息详情
                .created(DateUtil.current())//创建时间
                .build().toJson();
        this.mqFeign.sendMsg(Constants.MQ.Exchanges.TRANSPORT_INFO, Constants.MQ.RoutingKeys.TRANSPORT_INFO_APPEND, transportInfoMsg);
    }
}
