package com.dreams.logistics.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 货品总重量 
 * @TableName order_cargo
 */
@TableName(value ="order_cargo")
@Data
public class OrderCargo implements Serializable {
    /**
     * id
     */
    @TableId
    private Long id;

    /**
     * 订单id
     */
    private Long orderId;

    /**
     * 运单id
     */
    private Long tranOrderId;

    /**
     * 货物类型id
     */
    private String goodsTypeId;

    /**
     * 货物名称
     */
    private String name;

    /**
     * 货物单位
     */
    private String unit;

    /**
     * 货品货值
     */
    private BigDecimal cargoValue;

    /**
     * 货品条码
     */
    private String cargoBarcode;

    /**
     * 货品数量
     */
    private Integer quantity;

    /**
     * 货品体积
     */
    private BigDecimal volume;

    /**
     * 货品重量
     */
    private BigDecimal weight;

    /**
     * 货品备注
     */
    private String remark;

    /**
     * 货品总体积
     */
    private BigDecimal totalVolume;

    /**
     * 货品总重量
     */
    private BigDecimal totalWeight;

    @TableField(fill = FieldFill.INSERT) //MP自动填充
    private LocalDateTime created;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updated;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}