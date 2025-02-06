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
 * 车辆信息表
 * @TableName truck
 */
@TableName(value ="truck")
@Data
public class Truck implements Serializable {
    /**
     * id
     */
    @TableId
    private Long id;

    /**
     * 车辆类型id
     */
    private Long truckTypeId;

    /**
     * 品牌
     */
    private String brand;

    /**
     * 车牌号码
     */
    private String licensePlate;


    /**
     * 准载重量
     */
    private BigDecimal allowableLoad;

    /**
     * 准载体积
     */
    private BigDecimal allowableVolume;


    /**
     * 当前所在机构id
     */
    private Long currentOrganId;

    /**
     * 运输状态 0：已到达 1：运输中
     */
    private Integer runStatus;

    /**
     * 状态 0：禁用 1：正常
     */
    private Integer status;

    /**
     * 工作状态：1启用 0停用
     */
    private Integer workStatus;


    /**
     * 满载系数
     */
    private Double loadingRatio;

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