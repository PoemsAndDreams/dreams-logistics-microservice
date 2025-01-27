package com.dreams.logistics.model.vo;


import com.dreams.logistics.enums.RefundStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundRecordVO {

    /**
     * 主键
     */
    private Long id;
    /**
     * 交易系统订单号【对于三方来说：商户订单】
     */
    private Long tradingOrderNo;

    /**
     * 业务系统订单号
     */
    private Long productOrderNo;

    /**
     * 本次退款订单号
     */
    private String refundNo;

    /**
     * 商户号
     */
    private Long enterpriseId;


    /**
     * 退款渠道【支付宝、微信、现金】
     */
    private String tradingChannel;

    /**
     * 退款状态
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
    private String memo;


    /**
     * 原订单金额
     */
    private BigDecimal total;


    /**
     * 创建时间
     */
    protected LocalDateTime created;


    /**
     * 更新时间
     */
    protected LocalDateTime updated;

}
