package com.dreams.logistics.model.dto.truckPlan;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * TransportTripsDto
 */
@Data
public class TruckPlanDto {
    /**
     * id
     */
    private Long id;
    /**
     * 车辆id
     */
    private Long truckId;
    /**
     * 车次id
     */
    private Long transportTripsId;
    /**
     * 司机Id
     */
    private List<Long> driverIds;

    /**
     * 计划发车时间
     */
   @ApiModelProperty(example = "2022-08-15 00:00:00", dataType = "string")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime planDepartureTime;

    /**
     * 计划到达时间
     */
   @ApiModelProperty(example = "2022-08-15 00:00:00", dataType = "string")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime planArrivalTime;

    /**
     * 计划到达时间
     */
   @ApiModelProperty(example = "2022-08-15 00:00:00", dataType = "string")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime nextDepartureTime;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 创建时间
     */
   @ApiModelProperty(example = "2022-08-15 00:00:00", dataType = "string")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;

    /**
     * 更新时间
     */
   @ApiModelProperty(example = "2022-08-15 00:00:00", dataType = "string")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updated;

    /**
     * 车辆结构
     */
    private TruckDto truckDto;

    //起始机构id
    private Long startOrganId;

    //结束机构id
    private Long endOrganId;

    /**
     * 所属线路id
     */
    private Long transportLineId;
}