package com.dreams.logistics.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

import com.dreams.logistics.enums.TradingStateEnum;
import lombok.Data;

/**
 * 交易订单表
 * @TableName trading
 */
@TableName(value ="trading")
@Data
public class Trading implements Serializable {
    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 业务系统订单号
     */
    private Long productOrderNo;

    /**
     * 交易系统订单号
     */
    private Long tradingOrderNo;

    /**
     * 支付渠道【支付宝、微信、现金、免单挂账】
     */
    private String tradingChannel;

    /**
     * 交易类型【付款、退款、免单、挂账】
     */
    private String tradingType;

    /**
     * 交易单状态【1-待付款,2-付款中,3-付款失败,4-已结算,5-取消订单,6-免单,7-挂账】
     */
    private TradingStateEnum tradingState;

    /**
     * 收款人姓名
     */
    private String payeeName;

    /**
     * 收款人账户ID
     */
    private Long payeeId;

    /**
     * 付款人姓名
     */
    private String payerName;

    /**
     * 付款人Id
     */
    private Long payerId;

    /**
     * 交易金额，单位：元
     */
    private BigDecimal tradingAmount;

    /**
     * 退款金额【付款后，单位：元】
     */
    private BigDecimal refund;

    /**
     * 是否有退款：YES，NO
     */
    private String isRefund;

    /**
     * 第三方交易返回编码【最终确认交易结果】
     */
    private String resultCode;

    /**
     * 第三方交易返回提示消息【最终确认交易信息】
     */
    private String resultMsg;

    /**
     * 第三方交易返回信息json【分析交易最终信息】
     */
    private String resultJson;

    /**
     * 统一下单返回编码
     */
    private String placeOrderCode;

    /**
     * 统一下单返回信息
     */
    private String placeOrderMsg;

    /**
     * 统一下单返回信息json
     */
    private String placeOrderJson;

    /**
     * 商户号
     */
    private Long enterpriseId;

    /**
     * 备注
     */
    private String remark;

    /**
     * 二维码base64数据
     */
    private String qrCode;

    /**
     * open_id标识
     */
    private String openId;

    /**
     * 是否有效
     */
    private String enableFlag;


    @TableField(fill = FieldFill.INSERT) //MP自动填充
    private LocalDateTime created;


    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updated;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}