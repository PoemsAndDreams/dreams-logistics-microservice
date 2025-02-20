package com.dreams.logistics.model.dto.order;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 取件模型
 */
@Data
public class OrderPickupDTO {
    /**
     * 订单Id
     */
    private Long id;

    /**
     * 物品名称
     */
    private String goodName;

    /**
     * 体积，单位m^3
     */
    private BigDecimal volume;

    /**
     * 重量，单位kg
     */
    private BigDecimal weight;

    /**
     * 备注
     */
    private String remark;

    /**
     * 付款方式,1.寄付，2到付
     */
    private Integer payMethod;

    /**
     * 金额
     */
    private BigDecimal amount;
}
