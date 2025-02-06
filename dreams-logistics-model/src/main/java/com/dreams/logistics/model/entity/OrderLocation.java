package com.dreams.logistics.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 订单位置信息 
 * @TableName order_location
 */
@TableName(value ="order_location")
@Data
public class OrderLocation implements Serializable {
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
     * 发货地址坐标
     */
    private String sendLocation;

    /**
     * 收货地址坐标
     */
    private String receiveLocation;

    /**
     * 发货起始网点计算结果
     */
    private String sendAgentId;

    /**
     * 接受终止网点计算结果
     */
    private String receiveAgentId;

    /**
     * 记录状态 0：无效，1有效
     */
    private String status;

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