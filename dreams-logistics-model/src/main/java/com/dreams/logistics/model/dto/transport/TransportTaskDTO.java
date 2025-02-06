package com.dreams.logistics.model.dto.transport;

import com.dreams.logistics.enums.TransportTaskAssignedStatus;
import com.dreams.logistics.enums.TransportTaskLoadingStatus;
import com.dreams.logistics.enums.TransportTaskStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 运输任务
 */
@Data
public class TransportTaskDTO {

    /**
     * id
     */
    private Long id;

    /**
     * 车辆计划id
     */
    private Long truckPlanId;

    /**
     * 车次id
     */
    private Long transportTripsId;

    /**
     * 起始机构id
     */
    private Long startAgencyId;

    /**
     * 目的机构id
     */
    private Long endAgencyId;

    /**
     * 任务状态，1为待执行（对应 待提货）、2为进行中（对应在途）、3为待确认（保留状态）、4为已完成（对应 已交付）、5为已取消
     */
    private TransportTaskStatus status;

    /**
     * 任务分配状态(1未分配2已分配3待人工分配)
     */
    private TransportTaskAssignedStatus assignedStatus;

    /**
     * 满载状态(1.半载2.满载3.空载)
     */
    private TransportTaskLoadingStatus loadingStatus;

    /**
     * 车辆id
     */
    private Long truckId;

    /**
     * 提货凭证
     */
    private String cargoPickUpPicture;

    /**
     * 货物照片
     */
    private String cargoPicture;

    /**
     * 运回单凭证
     */
    private String transportCertificate;

    /**
     * 计划发车时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime planDepartureTime;

    /**
     * 实际发车时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualDepartureTime;

    /**
     * 计划到达时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime planArrivalTime;

    /**
     * 实际到达时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualArrivalTime;

    /**
     * 交付货物照片
     */
    private String deliverPicture;

    /**
     * 提货纬度
     */
    private String deliveryLatitude;

    /**
     * 提货经度
     */
    private String deliveryLongitude;

    /**
     * 交付纬度
     */
    private String deliverLatitude;

    /**
     * 交付经度
     */
    private String deliverLongitude;


    /**
     * 任务创建时间
     */
    @JsonFormat(
            pattern = "yyyy-MM-dd HH:mm:ss"
    )
    private LocalDateTime createTime;

    /**
     * 运单id列表
     */
    private List<String> transportOrderIds;

    /**
     * 运单数量
     */
    private Integer transportOrderCount;


    private Integer page;


    private Integer pageSize;

    /**
     * id列表
     */
    private List<Long> ids;

    /**
     * 距离，单位：米
     */
    private Double distance;

    /**
     * 更新时间
     */
    private LocalDateTime updated;

}
