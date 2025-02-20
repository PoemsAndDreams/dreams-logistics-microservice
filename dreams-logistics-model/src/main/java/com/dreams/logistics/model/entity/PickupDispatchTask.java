package com.dreams.logistics.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import com.dreams.logistics.enums.*;
import lombok.Data;

/**
 * 取件、派件任务信息表
 * @TableName pickup_dispatch_task
 */
@TableName(value ="pickup_dispatch_task")
@Data
public class PickupDispatchTask implements Serializable {
    /**
     * id
     */
    @TableId
    private Long id;

    /**
     * 关联订单id
     */
    private Long orderId;

    /**
     * 任务类型，1为取件任务，2为派件任务
     */
    private PickupDispatchTaskType taskType;

    /**
     * 任务状态
     */
    private PickupDispatchTaskStatus status;

    /**
     * 签收状态(1为已签收，2为拒收)
     */
    private PickupDispatchTaskSignStatus signStatus;

    /**
     * 任务分配状态(2已分配3待人工分配)
     */
    private PickupDispatchTaskAssignedStatus assignedStatus;


    /**
     * 签收人，1本人，2代收
     */
    private SignRecipientEnum signRecipient;

    /**
     * 网点ID
     */
    private Long agencyId;

    /**
     * 快递员ID
     */
    private Long courierId;

    /**
     * 预计开始时间
     */
    private LocalDateTime estimatedStartTime;

    /**
     * 实际开始时间
     */
    private LocalDateTime actualStartTime;

    /**
     * 预计完成时间
     */
    private LocalDateTime estimatedEndTime;

    /**
     * 实际完成时间
     */
    private LocalDateTime actualEndTime;

    /**
     * 取消时间
     */
    private LocalDateTime cancelTime;

    /**
     * 取消原因
     */
    private PickupDispatchTaskCancelReason cancelReason;

    /**
     * 取消原因具体描述
     */
    private String cancelReasonDescription;

    /**
     * 备注
     */
    private String mark;

    /**
     * 创建时间
     */
    private Date created;

    /**
     * 更新时间
     */
    private Date updated;

    /**
     * 删除：0-否，1-是
     */
    @TableLogic
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}