package com.dreams.logistics.model.dto.driverJob;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;


@Data
public class DriverReturnRegisterDTO {

    /**
     * 运输任务id
     */
    private String id;

    /**
     * 出车时间
     */
    private String outStorageTime;

    /**
     * 回车时间
     */
    private String intoStorageTime;

    /**
     * 车辆是否可用
     */
    private Boolean isAvailable;

}
