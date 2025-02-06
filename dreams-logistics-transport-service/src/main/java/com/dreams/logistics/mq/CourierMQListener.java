package com.dreams.logistics.mq;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import com.dreams.logistics.common.Constants;
import com.dreams.logistics.model.dto.msg.CourierMsg;
import com.dreams.logistics.model.dto.msg.TransportInfoMsg;
import com.dreams.logistics.model.entity.TransportOrder;
import com.dreams.logistics.service.MQFeign;
import com.dreams.logistics.service.OrderService;
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
    private OrderService orderService;


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
