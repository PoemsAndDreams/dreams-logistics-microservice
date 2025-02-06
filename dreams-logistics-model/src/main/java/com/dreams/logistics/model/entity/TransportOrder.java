package com.dreams.logistics.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.dreams.logistics.enums.TransportOrderSchedulingStatus;
import com.dreams.logistics.enums.TransportOrderStatus;
import lombok.Data;

/**
 * 运单表
 * @TableName transport_order
 */
@TableName(value ="transport_order")
@Data
public class TransportOrder implements Serializable {
    /**
     * id
     */
    @TableId
    private String id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 运单状态(1.新建 2.已装车 3.运输中 4.到达终端网点 5.已签收 6.拒收)
     */
    private TransportOrderStatus status;

    /**
     * 调度状态(1.待调度2.未匹配线路3.已调度)
     */
    private TransportOrderSchedulingStatus schedulingStatus;

    /**
     * 起始网点id
     */
    private Long startAgencyId;

    /**
     * 终点网点id
     */
    private Long endAgencyId;

    /**
     * 当前所在机构id
     */
    private Long currentAgencyId;

    /**
     * 下一个机构id
     */
    private Long nextAgencyId;

    /**
     * 完整的运输路线
     */
    private String transportLine;

    /**
     * 货品总体积
     */
    private BigDecimal totalVolume;

    /**
     * 货品总重量
     */
    private BigDecimal totalWeight;

    /**
     * 是否为拒收运单
     */
    private Boolean isRejection;

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