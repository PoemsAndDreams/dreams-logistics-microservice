package com.dreams.logistics.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 车辆类型表
 * @TableName truck_type
 */
@TableName(value ="truck_type")
@Data
public class TruckType implements Serializable {
    /**
     * id
     */
    @TableId
    private Long id;

    /**
     * 车辆类型名称
     */
    private String name;

    /**
     * 准载重量，单位：吨
     */
    private BigDecimal allowableLoad;

    /**
     * 准载体积，单位：立方米
     */
    private BigDecimal allowableVolume;

    /**
     * 长，单位：米
     */
    private BigDecimal measureLong;

    /**
     * 宽，单位：米
     */
    private BigDecimal measureWidth;

    /**
     * 高，单位：米
     */
    private BigDecimal measureHigh;

    /**
     * 状态 0：禁用 1：正常
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date created;

    /**
     * 更新时间
     */
    private Date updated;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}