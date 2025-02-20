package com.dreams.logistics.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreams.logistics.common.Constants;
import com.dreams.logistics.common.ErrorCode;
import com.dreams.logistics.constant.CommonConstant;
import com.dreams.logistics.enums.*;
import com.dreams.logistics.exception.BusinessException;
import com.dreams.logistics.model.dto.msg.OrderMsg;
import com.dreams.logistics.model.dto.msg.TransportInfoMsg;
import com.dreams.logistics.model.dto.pickupDispatchTask.PickupDispatchTaskDTO;
import com.dreams.logistics.model.dto.pickupDispatchTask.PickupDispatchTaskQueryRequest;
import com.dreams.logistics.model.dto.transport.CourierTaskCountDTO;
import com.dreams.logistics.model.entity.DcUser;
import com.dreams.logistics.model.entity.PickupDispatchTask;
import com.dreams.logistics.model.entity.TransportOrder;
import com.dreams.logistics.model.vo.PickupDispatchTaskVO;
import com.dreams.logistics.model.vo.UserVO;
import com.dreams.logistics.service.*;
import com.dreams.logistics.mapper.PickupDispatchTaskMapper;
import com.dreams.logistics.utils.ObjectUtil;
import com.dreams.logistics.utils.SqlUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author xiayutian
* @description 针对表【pickup_dispatch_task(取件、派件任务信息表)】的数据库操作Service实现
* @createDate 2025-02-16 20:10:20
*/
@Service
public class PickupDispatchTaskServiceImpl extends ServiceImpl<PickupDispatchTaskMapper, PickupDispatchTask>
    implements PickupDispatchTaskService{

    @Resource
    private PickupDispatchTaskMapper pickupDispatchTaskMapper;
    @Resource
    private TransportOrderService transportOrderService;
    @Resource
    private OrderService orderService;

    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private MQFeign mqFeign;

    @Override
    public PickupDispatchTask saveTaskPickupDispatch(PickupDispatchTask taskPickupDispatch) {
        // 设置任务状态为新任务
        taskPickupDispatch.setStatus(PickupDispatchTaskStatus.NEW);
        boolean result = super.save(taskPickupDispatch);
        if (result) {
            //生成运单跟踪消息和快递员端取件/派件消息通知
            this.generateMsg(taskPickupDispatch);

            return taskPickupDispatch;
        }
        throw new BusinessException(WorkExceptionEnum.PICKUP_DISPATCH_TASK_SAVE_ERROR);
    }

    /**
     * 生成运单跟踪消息和快递员端取件/派件消息通知
     *
     * @param taskPickupDispatch 取派件任务
     */
    private void generateMsg(PickupDispatchTask taskPickupDispatch) {
        //分配给快递员的取派件任务需要发送消息
        if (ObjectUtil.isNotEmpty(taskPickupDispatch.getCourierId())) {
            if (ObjectUtil.equal(taskPickupDispatch.getTaskType(), PickupDispatchTaskType.DISPATCH)) {
                //派件任务需要发送运单跟踪消息
                this.sendTransportInfoMsg(taskPickupDispatch);

                // todo 通知快递员：你有新的任务
            } else {
                //取件任务需要生成取件相关通知消息

                // todo 通知快递员：你有新的任务

            }
        }
    }

    /**
     * 发送派件相关运单跟踪消息
     *
     * @param taskPickupDispatch 取派件任务
     */
    private void sendTransportInfoMsg(PickupDispatchTask taskPickupDispatch) {

        DcUser user = userFeignClient.getById(taskPickupDispatch.getCourierId());

        TransportOrder transportOrder = transportOrderService.findByOrderId(taskPickupDispatch.getOrderId());
        String info = CharSequenceUtil.format("您的快递正在派送途中，派件人【{}，电话 {}】", user.getUserName(), user.getPhone());

        //构建消息实体类
        String transportInfoMsg = TransportInfoMsg.builder()
                .transportOrderId(transportOrder.getId())
                .status("派送中")
                .info(info)
                .created(DateUtil.current())
                .build().toJson();
        this.mqFeign.sendMsg(Constants.MQ.Exchanges.TRANSPORT_INFO, Constants.MQ.RoutingKeys.TRANSPORT_INFO_APPEND, transportInfoMsg);
    }

    @Override
    public List<CourierTaskCountDTO> findCountByCourierIds(List<Long> courierIds, PickupDispatchTaskType pickupDispatchTaskType, String date) {
        //计算一天的时间的边界
        DateTime dateTime = DateUtil.parse(date);
        LocalDateTime startDateTime = DateUtil.beginOfDay(dateTime).toLocalDateTime();
        LocalDateTime endDateTime = DateUtil.endOfDay(dateTime).toLocalDateTime();
        return this.pickupDispatchTaskMapper
                .findCountByCourierIds(courierIds, pickupDispatchTaskType.getCode(), startDateTime, endDateTime);
    }

    @Override
    public List<PickupDispatchTaskDTO> findTodayTaskByCourierId(Long courierId) {
        //查询指定快递员当天所有的派件取件任务
        LambdaQueryWrapper<PickupDispatchTask> queryWrapper = Wrappers.<PickupDispatchTask>lambdaQuery()
                .eq(PickupDispatchTask::getCourierId, courierId)
                .ge(PickupDispatchTask::getEstimatedStartTime, LocalDateTimeUtil.beginOfDay(LocalDateTime.now()))
                .le(PickupDispatchTask::getEstimatedStartTime, LocalDateTimeUtil.endOfDay(LocalDateTime.now()));
        List<PickupDispatchTask> list = super.list(queryWrapper);
        return BeanUtil.copyToList(list, PickupDispatchTaskDTO.class);
    }


    @Override
    public List<PickupDispatchTask> findByOrderId(Long orderId, PickupDispatchTaskType taskType) {
        LambdaQueryWrapper<PickupDispatchTask> wrapper = Wrappers.<PickupDispatchTask>lambdaQuery()
                .eq(PickupDispatchTask::getOrderId, orderId)
                .eq(PickupDispatchTask::getTaskType, taskType)
                .orderByAsc(PickupDispatchTask::getCreated);
        return this.list(wrapper);
    }
    @Override
    public List<PickupDispatchTask> findByOrderId(Long orderId) {
        LambdaQueryWrapper<PickupDispatchTask> wrapper = Wrappers.<PickupDispatchTask>lambdaQuery()
                .eq(PickupDispatchTask::getOrderId, orderId)
                .orderByAsc(PickupDispatchTask::getCreated);
        return this.list(wrapper);
    }


    @Override
    public boolean deleteByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return false;
        }
        // 通过id列表构造对象列表
        List<PickupDispatchTask> list = ids.stream().map(id -> {
            PickupDispatchTask dispatchTaskEntity = new PickupDispatchTask();
            dispatchTaskEntity.setId(id);
            //TODO 发送消息，同步更新快递员任务（ES）
            return dispatchTaskEntity;
        }).collect(Collectors.toList());
        return super.updateBatchById(list);
    }

    @Override
    public Boolean updateCourierId(Long id, Long originalCourierId, Long targetCourierId) {
        if (ObjectUtil.hasEmpty(id, targetCourierId, originalCourierId)) {
            throw new BusinessException(WorkExceptionEnum.UPDATE_COURIER_PARAM_ERROR);
        }
        if (ObjectUtil.equal(originalCourierId, targetCourierId)) {
            throw new BusinessException(WorkExceptionEnum.UPDATE_COURIER_EQUAL_PARAM_ERROR);
        }
        PickupDispatchTask pickupDispatchTask = super.getById(id);
        if (ObjectUtil.isEmpty(pickupDispatchTask)) {
            throw new BusinessException(WorkExceptionEnum.PICKUP_DISPATCH_TASK_NOT_FOUND);
        }
        //校验原快递id是否正确（本来无快递员id的情况除外）
        if (ObjectUtil.isNotEmpty(pickupDispatchTask.getCourierId())
                && ObjectUtil.notEqual(pickupDispatchTask.getCourierId(), originalCourierId)) {
            throw new BusinessException(WorkExceptionEnum.UPDATE_COURIER_ID_PARAM_ERROR);
        }
        //更改快递员id
        pickupDispatchTask.setCourierId(targetCourierId);
        // 标识已分配状态
        pickupDispatchTask.setAssignedStatus(PickupDispatchTaskAssignedStatus.DISTRIBUTED);
        //TODO 发送消息，同步更新快递员任务(ES)
        return super.updateById(pickupDispatchTask);
    }

    @Override
    public QueryWrapper<PickupDispatchTask> getQueryWrapper(PickupDispatchTaskQueryRequest pickupDispatchTaskQueryRequest) {

        if (pickupDispatchTaskQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        Long id = pickupDispatchTaskQueryRequest.getId();
        Long orderId = pickupDispatchTaskQueryRequest.getOrderId();
        PickupDispatchTaskType taskType = pickupDispatchTaskQueryRequest.getTaskType();
        PickupDispatchTaskStatus status = pickupDispatchTaskQueryRequest.getStatus();
        PickupDispatchTaskSignStatus signStatus = pickupDispatchTaskQueryRequest.getSignStatus();
        SignRecipientEnum signRecipient = pickupDispatchTaskQueryRequest.getSignRecipient();
        Long agencyId = pickupDispatchTaskQueryRequest.getAgencyId();
        Long courierId = pickupDispatchTaskQueryRequest.getCourierId();
        Date estimatedStartTime = pickupDispatchTaskQueryRequest.getEstimatedStartTime();
        Date actualStartTime = pickupDispatchTaskQueryRequest.getActualStartTime();
        Date estimatedEndTime = pickupDispatchTaskQueryRequest.getEstimatedEndTime();
        Date actualEndTime = pickupDispatchTaskQueryRequest.getActualEndTime();
        Date cancelTime = pickupDispatchTaskQueryRequest.getCancelTime();
        PickupDispatchTaskAssignedStatus assignedStatus = pickupDispatchTaskQueryRequest.getAssignedStatus();
        String mark = pickupDispatchTaskQueryRequest.getMark();
        Date created = pickupDispatchTaskQueryRequest.getCreated();
        Date updated = pickupDispatchTaskQueryRequest.getUpdated();
        PickupDispatchTaskIsDeleted isDeleted = pickupDispatchTaskQueryRequest.getIsDeleted();
        PickupDispatchTaskCancelReason cancelReason = pickupDispatchTaskQueryRequest.getCancelReason();
        String cancelReasonDescription = pickupDispatchTaskQueryRequest.getCancelReasonDescription();
        List<Long> ids = pickupDispatchTaskQueryRequest.getIds();
        List<Long> orderIds = pickupDispatchTaskQueryRequest.getOrderIds();
        String sortField = pickupDispatchTaskQueryRequest.getSortField();
        String sortOrder = pickupDispatchTaskQueryRequest.getSortOrder();


        QueryWrapper<PickupDispatchTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(orderId != null, "order_id", orderId);
        queryWrapper.eq(taskType != null, "task_type", taskType);
        queryWrapper.eq(status != null, "status", status);
        queryWrapper.eq(signStatus != null, "sign_status", signStatus);
        queryWrapper.eq(signRecipient != null, "sign_recipient", signRecipient);
        queryWrapper.eq(agencyId != null, "agency_id", agencyId);
        queryWrapper.eq(courierId != null, "courier_id", courierId);
        queryWrapper.eq(estimatedStartTime != null, "estimated_start_time", estimatedStartTime);
        queryWrapper.eq(actualStartTime != null, "actual_start_time", actualStartTime);
        queryWrapper.eq(estimatedEndTime != null, "estimated_end_time", estimatedEndTime);
        queryWrapper.eq(actualEndTime != null, "actual_end_time", actualEndTime);
        queryWrapper.eq(cancelTime != null, "cancel_time", cancelTime);
        queryWrapper.eq(assignedStatus != null, "assigned_status", assignedStatus);
        queryWrapper.eq(StringUtils.isNotBlank(mark), "mark", mark);
        queryWrapper.eq(created != null, "created", created);
        queryWrapper.eq(updated != null, "updated", updated);
        queryWrapper.eq(isDeleted != null, "is_deleted", isDeleted);
        queryWrapper.eq(cancelReason != null, "cancel_reason", cancelReason);
        queryWrapper.like(StringUtils.isNotBlank(cancelReasonDescription), "cancel_reason_description", cancelReasonDescription);
        queryWrapper.in(ids != null && !ids.isEmpty(), "id", ids);
        queryWrapper.in(orderIds != null && !orderIds.isEmpty(), "order_id", orderIds);


        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public List<PickupDispatchTaskVO> getPickupDispatchTaskVO(List<PickupDispatchTask> records) {
        return records.stream().map(record -> {
            // todo 补充字段
            return getPickupDispatchTaskVO(record);
        }).collect(Collectors.toList());
    }

    @Override
    public PickupDispatchTaskVO getPickupDispatchTaskVO(PickupDispatchTask pickupDispatchTask) {
        if (pickupDispatchTask == null) {
            return null;
        }
        PickupDispatchTaskVO dispatchTaskVO = new PickupDispatchTaskVO();
        BeanUtils.copyProperties(pickupDispatchTask, dispatchTaskVO);
        // todo 补充字段
        return dispatchTaskVO;
    }


    @Override
    @Transactional
    public Boolean updateStatus(PickupDispatchTask pickupDispatchTaskDTO) {
        WorkExceptionEnum paramError = WorkExceptionEnum.PICKUP_DISPATCH_TASK_PARAM_ERROR;
        if (ObjectUtil.hasEmpty(pickupDispatchTaskDTO.getId(), pickupDispatchTaskDTO.getStatus())) {
            throw new BusinessException("更新取派件任务状态，id或status不能为空", paramError.getCode());
        }
        PickupDispatchTask pickupDispatchTask = super.getById(pickupDispatchTaskDTO.getId());
        switch (pickupDispatchTaskDTO.getStatus()) {
            case NEW: {
                throw new BusinessException(WorkExceptionEnum.PICKUP_DISPATCH_TASK_STATUS_NOT_NEW);
            }
            case COMPLETED: {
                //任务完成
                pickupDispatchTask.setStatus(PickupDispatchTaskStatus.COMPLETED);
                //设置完成时间
                pickupDispatchTask.setActualEndTime(LocalDateTime.now());
                if (PickupDispatchTaskType.DISPATCH == pickupDispatchTask.getTaskType()) {
                    //如果是派件任务的完成，已签收需要设置签收状态和签收人，拒收只需要设置签收状态
                    if (ObjectUtil.isEmpty(pickupDispatchTaskDTO.getSignStatus())) {
                        throw new BusinessException("完成派件任务，签收状态不能为空", paramError.getCode());
                    }
                    pickupDispatchTask.setSignStatus(pickupDispatchTaskDTO.getSignStatus());
                    if (PickupDispatchTaskSignStatus.RECEIVED == pickupDispatchTaskDTO.getSignStatus()) {
                        if (ObjectUtil.isEmpty(pickupDispatchTaskDTO.getSignRecipient())) {
                            throw new BusinessException("完成派件任务，签收人不能为空", paramError.getCode());
                        }
                        pickupDispatchTask.setSignRecipient(pickupDispatchTaskDTO.getSignRecipient());
                    }
                }
                break;
            }
            case CANCELLED: {
                //任务取消
                if (ObjectUtil.isEmpty(pickupDispatchTaskDTO.getCancelReason())) {
                    throw new BusinessException("取消任务，原因不能为空", paramError.getCode());
                }
                pickupDispatchTask.setStatus(PickupDispatchTaskStatus.CANCELLED);
                pickupDispatchTask.setCancelReason(pickupDispatchTaskDTO.getCancelReason());
                pickupDispatchTask.setCancelReasonDescription(pickupDispatchTaskDTO.getCancelReasonDescription());
                pickupDispatchTask.setCancelTime(LocalDateTime.now());
                if (pickupDispatchTaskDTO.getCancelReason() == PickupDispatchTaskCancelReason.RETURN_TO_AGENCY) {
                    //发送分配快递员派件任务的消息
                    OrderMsg orderMsg = OrderMsg.builder()
                            .agencyId(pickupDispatchTask.getAgencyId())
                            .orderId(pickupDispatchTask.getOrderId())
                            .created(DateUtil.current())
                            .taskType(PickupDispatchTaskType.PICKUP.getCode()) //取件任务
                            .mark(pickupDispatchTask.getMark())
                            .estimatedEndTime(pickupDispatchTask.getEstimatedEndTime()).build();
                    //发送消息（取消任务发生在取件之前，没有运单，参数直接填入null）
                    this.transportOrderService.sendPickupDispatchTaskMsgToDispatch(null, orderMsg);
                } else if (pickupDispatchTaskDTO.getCancelReason() == PickupDispatchTaskCancelReason.CANCEL_BY_USER) {
                    //原因是用户取消，则订单状态改为取消
                    orderService.updateStatus(ListUtil.of(pickupDispatchTask.getOrderId()), OrderStatus.CANCELLED.getCode());
                } else {
                    //其他原因则关闭订单
                    orderService.updateStatus(ListUtil.of(pickupDispatchTask.getOrderId()), OrderStatus.CLOSE.getCode());
                }
                break;
            }
            default: {
                throw new BusinessException("其他未知状态，不能完成更新操作", paramError.getCode());
            }
        }
        //TODO 发送消息，同步更新快递员任务
        return super.updateById(pickupDispatchTask);
    }
}




