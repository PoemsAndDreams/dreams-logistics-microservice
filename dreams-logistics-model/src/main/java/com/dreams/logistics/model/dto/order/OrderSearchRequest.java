package com.dreams.logistics.model.dto.order;

import com.dreams.logistics.common.PageRequest;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 订单
 * @TableName order
 */

@Data
public class OrderSearchRequest extends PageRequest implements Serializable {

    /**
     * 付款状态,1.未付2已付
     */
    private Integer paymentStatus;

    /**
     * 金额
     */
    private BigDecimal amount;


    /**
     * 订单类型，1为同城订单，2为城际订单
     */
    private Integer orderType;

    /**
     * 取件类型，1为网点自寄，2为上门取件
     */
    private Integer pickupType;

    /**
     * 货物名称
     */
    private String name;

    /**
     * 货品体积
     */
    private BigDecimal volume;

    /**
     * 货品重量
     */
    private BigDecimal weight;

    /**
     * 收件人省区县id
     */
    private List<Long> receiverAddressId;

    /**
     * 收件人详细地址
     */
    private String receiverAddress;

    /**
     * 收件人姓名
     */
    private String receiverName;

    /**
     * 收件人电话
     */
    private String receiverPhone;

    /**
     * 发件人省区县id
     */
    private List<Long> senderAddressId;


    /**
     * 发件人详细地址
     */
    private String senderAddress;

    /**
     * 发件人姓名
     */
    private String senderName;

    /**
     * 发件人电话
     */
    private String senderPhone;

    /**
     * 备注
     */
    private String mark;


    /**
     * 预计取件开始到达时间
     */
    private List<Date> pickupTimeRange;


    private static final long serialVersionUID = 1L;
}