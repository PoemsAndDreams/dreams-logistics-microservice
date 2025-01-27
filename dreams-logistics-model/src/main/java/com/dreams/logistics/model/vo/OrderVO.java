package com.dreams.logistics.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单
 * @TableName order
 */
@Data
public class OrderVO implements Serializable {
    /**
     * id
     */
    private Long id;
    /**
     * 付款状态,1.未付2已付
     */
    private Integer paymentStatus;

    /**
     * 金额
     */
    private BigDecimal amount;

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
     * 收件人省份id
     */
    private Long receiverProvinceId;

    /**
     * 收件人城市id
     */
    private Long receiverCityId;

    /**
     * 收件人区县id
     */
    private Long receiverCountyId;

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
     * 发件人省份id
     */
    private Long senderProvinceId;

    /**
     * 发件人城市id
     */
    private Long senderCityId;

    /**
     * 发件人区县id
     */
    private Long senderCountyId;

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
     * 距离，单位：米
     */
    private Double distance;

    /**
     * 预计取件开始时间
     */
    private Date estimatedStartTime;

    /**
     * 预计到达时间
     */
    private Date estimatedArrivalTime;

    /**
     * 下单时间
     */
    private Date createTime;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}