package com.dreams.logistics.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.dreams.logistics.common.Constants;
import com.dreams.logistics.enums.*;
import com.dreams.logistics.exception.BusinessException;
import com.dreams.logistics.model.dto.msg.CourierMsg;
import com.dreams.logistics.model.dto.msg.TransportInfoMsg;
import com.dreams.logistics.model.dto.msg.TransportOrderStatusMsg;
import com.dreams.logistics.model.dto.order.OrderPickupDTO;
import com.dreams.logistics.model.dto.pickupDispatchTask.PickupDispatchTaskDTO;
import com.dreams.logistics.model.dto.task.TaskPickupVO;
import com.dreams.logistics.model.dto.task.TaskSignVO;
import com.dreams.logistics.model.dto.transport.TransportOrderDTO;
import com.dreams.logistics.model.dto.transportInfo.TransportOrderPointVO;
import com.dreams.logistics.model.entity.*;
import com.dreams.logistics.model.vo.OrderVO;
import com.dreams.logistics.service.*;
import com.dreams.logistics.utils.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TaskServiceImpl implements TaskService {

    @Resource
    private PickupDispatchTaskService pickupDispatchTaskService;

    @Resource
    private OrderService orderService;

    @Resource
    private TransportOrderService transportOrderService;
    @Resource
    private TransportInfoService transportInfoService;

    @Resource
    private MQFeign mqFeign;


    /**
     * 取件
     *
     * @param vo 取件对象
     * @return 是否成功
     */
    @Override
    public boolean pickup(TaskPickupVO vo) {
        log.info("取件信息:{}", vo);
        //1.根据任务id查询取派件任务
        PickupDispatchTask taskDTO = pickupDispatchTaskService.getById(Long.valueOf(vo.getId()));
        if (ObjectUtil.notEqual(taskDTO.getStatus(), PickupDispatchTaskStatus.NEW)) {
            throw new BusinessException("快递必须是未取状态！");
        }
        Long orderId = taskDTO.getOrderId();

        //2.根据订单id查询订单
        OrderVO orderDB = orderService.getOrderVOById(orderId);

        // todo 3.身份校验:先查询用户信息，若已实名认证直接放行；未实名认证必须进行认证
        
        //4.更新取派件任务
        PickupDispatchTask pickupDispatchTaskDTO = pickupDispatchTaskService.getById(Long.valueOf(vo.getId()));

        pickupDispatchTaskDTO.setStatus(PickupDispatchTaskStatus.COMPLETED);//任务状态

        pickupDispatchTaskService.updateStatus(pickupDispatchTaskDTO);


        //5.获取快递员信息
        DcUser user = SecurityUtil.getUser();

        //6.构建更新订单消息数据
        OrderPickupDTO orderPickupDTO = BeanUtil.toBean(vo, OrderPickupDTO.class);

        orderPickupDTO.setId(orderId);

        String orderPickupJson = JSONUtil.toJsonStr(orderPickupDTO);

        //6.1更新订单相关信息
        orderService.orderPickup(orderPickupDTO);


        //7.发送取件成功的消息
        String msg = CourierMsg.builder().orderId(orderId).courierId(user.getId()).courierName(user.getUserName()).courierMobile(user.getPhone()).created(System.currentTimeMillis())
                //更新订单消息放到扩展信息中，需要的业务再处理，不需要的忽略即可
                .info(orderPickupJson).build().toJson();
        this.mqFeign.sendMsg(Constants.MQ.Exchanges.COURIER, Constants.MQ.RoutingKeys.COURIER_PICKUP, msg);

        return true;
    }

    
    /**
     * 签收任务
     *
     * @param vo 签收对象
     */
    @Override
    public void sign(TaskSignVO vo) {
        //1.更新任务
        PickupDispatchTask taskDTO = pickupDispatchTaskService.getById(Long.valueOf(vo.getId()));
        if (ObjectUtil.notEqual(taskDTO.getSignStatus(), PickupDispatchTaskSignStatus.NOT_SIGNED)) {
            throw new BusinessException("快递已签收/拒收");
        }

        taskDTO.setStatus(PickupDispatchTaskStatus.COMPLETED);//任务状态
        taskDTO.setSignStatus(PickupDispatchTaskSignStatus.RECEIVED);//签收状态(1为已签收，2为拒收)
        taskDTO.setSignRecipient(SignRecipientEnum.codeOf(vo.getSignRecipient()));//签收人，1-本人，2-代收
        pickupDispatchTaskService.updateStatus(taskDTO);

        //2.更新订单
        Order order = orderService.getById(taskDTO.getOrderId());
        order.setStatus(OrderStatus.RECEIVED.getCode());
        orderService.updateById(order);

        //3.todo 存储签收消息,发送签收信息


        //4.发送运单跟踪消息
        //4.1获取快递员信息
        DcUser user = SecurityUtil.getUser();
        String info = CharSequenceUtil.format("您的快递已签收，如有疑问请联系快递员【{}，电话{}】，期待再次为您服务", user.getUserName(), user.getPhone());

        //4.2查询运单
        TransportOrder transportOrder = transportOrderService.findByOrderId(order.getId());

        //4.3构建消息实体类
        TransportInfoMsg transportInfoMsg = TransportInfoMsg.builder().transportOrderId(transportOrder.getId())//运单id
                .status("已签收")//消息状态
                .info(info)//消息详情
                .created(DateUtil.current())//创建时间
                .build();

        //4.4发送消息
        this.mqFeign.sendMsg(Constants.MQ.Exchanges.TRANSPORT_INFO, Constants.MQ.RoutingKeys.TRANSPORT_INFO_APPEND, transportInfoMsg.toJson());

        //5.发送完成轨迹消息
        TransportOrderStatusMsg transportOrderStatusMsg = TransportOrderStatusMsg.builder()
                .idList(Arrays.asList(transportOrder.getId()))
                .build();
        this.mqFeign.sendMsg(Constants.MQ.Exchanges.TRANSPORT_ORDER_DELAYED, Constants.MQ.RoutingKeys.TRANSPORT_ORDER_UPDATE_STATUS_PREFIX + "RECEIVED", transportOrderStatusMsg.toJson());
    }

    /**
     * 拒收任务
     *
     * @param id 任务id
     */
    @Override
    public void reject(String id) {
        //1.状态校验
        PickupDispatchTask task = pickupDispatchTaskService.getById(Long.valueOf(id));
        if (ObjectUtil.notEqual(task.getSignStatus(), PickupDispatchTaskSignStatus.NOT_SIGNED)) {
            throw new BusinessException("快递已签收/拒收");
        }

        TransportOrder transportOrder = transportOrderService.findByOrderId(task.getOrderId());
        if (ObjectUtil.equal(transportOrder.getIsRejection(), true)) {
            throw new BusinessException("快递已被收件人拒收，不能再次拒收！");
        }

        //2.更新任务
        task.setStatus(PickupDispatchTaskStatus.COMPLETED);//任务状态
        task.setSignStatus(PickupDispatchTaskSignStatus.REJECTION);//签收状态(1为已签收，2为拒收)
        task.setActualEndTime(LocalDateTime.now());//实际完成时间
        pickupDispatchTaskService.updateStatus(task);

        //3.更新订单
        Order order = orderService.getById(task.getOrderId());
        order.setStatus(OrderStatus.REJECTION.getCode());
        orderService.updateById(order);

        //4.更新运单状态为 拒收
        transportOrderService.updateStatus(Arrays.asList(transportOrder.getId()), TransportOrderStatus.REJECTED);

        //5.发送运单跟踪消息
        //5.1获取快递员信息
        DcUser user = SecurityUtil.getUser();
        String info = CharSequenceUtil.format("您的快递已拒收，快递将返回到网点，如有疑问请电联快递员【联系人{}，电话：{}】", user.getUserName(), user.getPhone());

        //5.2构建消息实体类
        TransportInfoMsg transportInfoMsg = TransportInfoMsg.builder().transportOrderId(transportOrder.getId()).status("已拒收").info(info).created(DateUtil.current()).build();

        //5.3发送消息
        this.mqFeign.sendMsg(Constants.MQ.Exchanges.TRANSPORT_INFO, Constants.MQ.RoutingKeys.TRANSPORT_INFO_APPEND, transportInfoMsg.toJson());
    }

    /**
     * 运单跟踪
     *
     * @param id 运单id
     * @return 运单跟踪信息
     */
    @Override
    public List<TransportOrderPointVO> tracks(String id) {
        //1.调用transport-info接口，获取相关运单的追踪信息
        TransportInfo transportInfo = transportInfoService.queryByTransportOrderId(id);
        if (ObjectUtil.hasEmpty(transportInfo, transportInfo.getInfoList())) {
            return Collections.emptyList();
        }

        //2.解析运单追踪信息，封装到vo
        return transportInfo.getInfoList().stream().map(x -> {
            TransportOrderPointVO vo = BeanUtil.toBean(x, TransportOrderPointVO.class);
            vo.setCreated(LocalDateTimeUtil.of(x.getCreated()));
            return vo;
        }).collect(Collectors.toList());
    }

    
}








