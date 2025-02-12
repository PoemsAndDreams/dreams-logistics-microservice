package com.dreams.logistics.model.dto.transport.request;

import com.dreams.logistics.enums.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 取派件任务分页查询
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PickupDispatchTaskPageQueryDTO {

    private Integer page;

    private Integer pageSize;
    /**
     * 取派件任务id
     */
    private Long id;
    /**
     * 订单id
     */
    private Long orderId;

    /**
     * 营业部id
     */
    private Long agencyId;

    /**
     * 快递员id
     */
    private Long courierId;
    /**
     * 任务类型，1为取件任务，2为派件任务
     */
    private PickupDispatchTaskType taskType;

    /**
     * 任务状态，1为新任务、2为已完成、3为已取消
     */
    private PickupDispatchTaskStatus status;

    /**
     * 任务分配状态(2已分配，3待人工分配)
     */
    private PickupDispatchTaskAssignedStatus assignedStatus;

    /**
     * 签收状态(1为已签收，2为拒收)
     */
    private PickupDispatchTaskSignStatus signStatus;

    /**
     * 最小预计完成时间
     */
   @ApiModelProperty(example = "2022-08-15 00:00:00", dataType = "string")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime minEstimatedEndTime;

    /**
     * 最大预计完成时间
     */
   @ApiModelProperty(example = "2022-08-15 00:00:00", dataType = "string")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime maxEstimatedEndTime;

    /**
     * 最小实际完成时间
     */
   @ApiModelProperty(example = "2022-08-15 00:00:00", dataType = "string")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime minActualEndTime;

    /**
     * 最大实际完成时间
     */
   @ApiModelProperty(example = "2022-08-15 00:00:00", dataType = "string")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime maxActualEndTime;

    /**
     * 删除：0-否，1-是;不传则查全部
     */
    private PickupDispatchTaskIsDeleted isDeleted;
}
