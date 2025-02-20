package com.dreams.logistics.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 订单
 * @TableName order
 */
@TableName(value ="resource_order")
@Data
public class Order implements Serializable {
    /**
     * id
     */
    @TableId
    private Long id;

    /**
     * 交易系统订单号
     */
    private Long tradingOrderNo;

    /**
     * 支付渠道
     */
    private String tradingChannel;

    /**
     * 付款方式,1.预结2到付
     */
    private Integer paymentMethod;

    /**
     * 付款状态,1.未付2已付
     */
    private Integer paymentStatus;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 退款金额【付款后，单位：元】
     */
    private BigDecimal refund;

    /**
     * 是否有退款：YES，NO
     */
    private String isRefund;

    /**
     * 订单类型，1为同城订单，2为城际订单
     */
    private Integer orderType;

    /**
     * 取件类型，1为网点自寄，2为上门取件
     */
    private Integer pickupType;

    /**
     * 下单客户ID
     */
    private Long memberId;

    /**
     * 收件人ID
     */
    private Long receiverMemberId;

    /**
     * 订单状态
     */
    private Integer status;

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
     * 收件人地址id
     */
    private Long receiverAddressId;

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
     * 发件人地址id
     */
    private Long senderAddressId;

    /**
     * 发件人姓名
     */
    private String senderName;

    /**
     * 发件人电话
     */
    private String senderPhone;

    /**
     * 订单当前所属网点
     */
    private Long currentAgencyId;

    /**
     * 距离，单位：米
     */
    private Double distance;

    /**
     * 备注
     */
    private String mark;

    /**
     * 下单时间
     */
   @ApiModelProperty(example = "2022-08-15 00:00:00", dataType = "string")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 预计取件开始时间
     */
   @ApiModelProperty(example = "2022-08-15 00:00:00", dataType = "string")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime estimatedStartTime;

    /**
     * 预计到达时间
     */
   @ApiModelProperty(example = "2022-08-15 00:00:00", dataType = "string")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime estimatedArrivalTime;

   @ApiModelProperty(example = "2022-08-15 00:00:00", dataType = "string")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT) //MP自动填充
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;

   @ApiModelProperty(example = "2022-08-15 00:00:00", dataType = "string")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updated;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 创建人
     */
    private Long createUser;
}