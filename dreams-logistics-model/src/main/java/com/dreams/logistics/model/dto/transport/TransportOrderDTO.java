package com.dreams.logistics.model.dto.transport;

import com.dreams.logistics.enums.TransportOrderSchedulingStatus;
import com.dreams.logistics.enums.TransportOrderStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransportOrderDTO {

    //运单号
    private String id;
    //订单ID
    private Long orderId;
    //运单状态
    private TransportOrderStatus status;
    //调度状态调度状态
    private TransportOrderSchedulingStatus schedulingStatus;
    /**
     * 任务创建时间
     */
   @ApiModelProperty(example = "2022-08-15 00:00:00", dataType = "string")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;

    /**
     * 任务更新时间
     */
   @ApiModelProperty(example = "2022-08-15 00:00:00", dataType = "string")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updated;

    //起始网点id
    private Long startAgencyId;
    //终点网点id
    private Long endAgencyId;
    //当前所在机构id
    private Long currentAgencyId;
    //下一个机构id
    private Long nextAgencyId;
    //完整的运输路线
    private String transportLine;
    //货品总体积
    private BigDecimal totalVolume;
    //货品总重量
    private BigDecimal totalWeight;
    //是否为拒收运单，默认非拒收
    private Boolean isRejection = false;
}
