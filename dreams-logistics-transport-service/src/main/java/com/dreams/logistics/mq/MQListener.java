package com.dreams.logistics.mq;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;

import com.dreams.logistics.common.Constants;
import com.dreams.logistics.enums.*;
import com.dreams.logistics.model.dto.msg.TradeStatusMsg;
import com.dreams.logistics.model.dto.msg.TransportOrderStatusMsg;
import com.dreams.logistics.model.dto.transport.TransportOrderDTO;
import com.dreams.logistics.model.entity.TransportOrder;
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
import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息处理
 */
@Slf4j
@Component
public class MQListener {
    
    @Resource
    private OrderService orderService;

    @Resource
    private TransportOrderService transportOrderService;

    /**
     * 更新运单状态
     *
     * @param msg 消息
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = Constants.MQ.Queues.OMS_TRANSPORT_ORDER_UPDATE_STATUS),
            exchange = @Exchange(name = Constants.MQ.Exchanges.TRANSPORT_ORDER_DELAYED, type = ExchangeTypes.TOPIC, delayed = Constants.MQ.DELAYED),
            key = Constants.MQ.RoutingKeys.TRANSPORT_ORDER_UPDATE_STATUS_PREFIX + "#"
    ))
    public void listenTransportOrderUpdateStatusMsg(String msg) {
        log.info("接收到更新运单状态的消息 ({})-> {}", Constants.MQ.Queues.OMS_TRANSPORT_ORDER_UPDATE_STATUS, msg);
        TransportOrderStatusMsg transportOrderStatusMsg = JSONUtil.toBean(msg, TransportOrderStatusMsg.class);
        // 具体业务逻辑的处理
        if (ObjectUtil.isEmpty(transportOrderStatusMsg.getStatusCode())) {
            // 无状态值 不处理
            return;
        }
        Integer status = getOrderStatusByTransportOrderStatus(transportOrderStatusMsg.getStatusCode());
        List<TransportOrder> list = transportOrderService.findByIds(transportOrderStatusMsg.getIdList().toArray(new String[0]));
        this.orderService.updateStatus(list.stream().map(TransportOrder::getOrderId).collect(Collectors.toList()), status);
    }

    private Integer getOrderStatusByTransportOrderStatus(Integer statusCode) {
        // 运输中
        if (TransportOrderStatus.PROCESSING.getCode().equals(statusCode)) {
           return OrderStatus.IN_TRANSIT.getCode();
        }
        // 派送中
        if (TransportOrderStatus.ARRIVED_END.getCode().equals(statusCode)) {
            return OrderStatus.DISPATCHING.getCode();
        }
        // 已拒收
        if (TransportOrderStatus.REJECTED.getCode().equals(statusCode)) {
            return OrderStatus.REJECTION.getCode();
        }
        // 默认已关闭
        return OrderStatus.CLOSE.getCode();
    }

    /**
     * 更新支付结果
     * 支付成功
     * @param msg 消息
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = Constants.MQ.Queues.OMS_TRADE_UPDATE_STATUS),
            exchange = @Exchange(name = Constants.MQ.Exchanges.TRADE, type = ExchangeTypes.TOPIC),
            key = Constants.MQ.RoutingKeys.TRADE_UPDATE_STATUS
    ))
    public void listenTradeUpdatePayStatusMsg(String msg) {
        log.info("接收到支付结果状态的消息 ({})-> {}", Constants.MQ.Queues.OMS_TRADE_UPDATE_STATUS, msg);
        List<TradeStatusMsg> tradeStatusMsgList = JSONUtil.toBean(msg, new TypeReference<List<TradeStatusMsg>>() {}, false);

        // 只处理支付成功的
        List<TradeStatusMsg> msgList = tradeStatusMsgList.stream().filter(v -> v.getStatusCode().equals(TradingStateEnum.YJS.getCode())).collect(Collectors.toList());
        if (CollUtil.isEmpty(msgList)) {
            return;
        }

        this.orderService.updatePayStatus(msgList.stream().map(TradeStatusMsg::getProductOrderNo).collect(Collectors.toList()), OrderPaymentStatus.PAID.getStatus());
    }

    /**
     * 更新退款结果
     * 退款
     * @param msg 消息
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = Constants.MQ.Queues.OMS_TRADE_REFUND_STATUS),
            exchange = @Exchange(name = Constants.MQ.Exchanges.TRADE, type = ExchangeTypes.TOPIC),
            key = Constants.MQ.RoutingKeys.REFUND_UPDATE_STATUS
    ))
    public void listenTradeUpdateRefundStatusMsg(String msg) {
        log.info("接收到退款订单的消息 ({})-> {}", Constants.MQ.Queues.OMS_TRADE_REFUND_STATUS, msg);
        List<TradeStatusMsg> tradeStatusMsgList = JSONUtil.toBean(msg, new TypeReference<List<TradeStatusMsg>>() {}, false);

        // 只处理需要退款的
        List<TradeStatusMsg> msgList = tradeStatusMsgList.stream().filter(v -> v.getStatusCode().equals(RefundStatusEnum.SUCCESS.getCode())).collect(Collectors.toList());
        if (CollUtil.isEmpty(msgList)) {
            return;
        }
        this.orderService.updateRefundInfo(msgList);
    }

}
