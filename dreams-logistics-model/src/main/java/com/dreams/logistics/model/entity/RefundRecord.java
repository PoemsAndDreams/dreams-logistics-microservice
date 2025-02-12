package com.dreams.logistics.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

import com.dreams.logistics.enums.RefundStatusEnum;
import com.dreams.logistics.enums.TradingStateEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 退款记录表
 * @TableName refund_record
 */
@TableName(value ="refund_record")
@Data
public class RefundRecord implements Serializable {
    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 交易系统订单号
     */
    private Long tradingOrderNo;

    /**
     * 业务系统订单号
     */
    private Long productOrderNo;

    /**
     * 本次退款订单号
     */
    private Long refundNo;

    /**
     * 商户号
     */
    private Long enterpriseId;

    /**
     * 退款渠道【支付宝、微信、现金】
     */
    private String tradingChannel;

    /**
     * 退款状态：1-退款中，2-成功, 3-失败
     */
    private RefundStatusEnum refundStatus;

    /**
     * 返回编码
     */
    private String refundCode;

    /**
     * 返回信息
     */
    private String refundMsg;

    /**
     * 备注【订单门店，桌台信息】
     */
    private String remark;

    /**
     * 本次退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 原订单金额
     */
    private BigDecimal total;

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
}