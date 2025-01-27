package com.dreams.logistics.model.vo;

import com.dreams.logistics.enums.PayChannelEnum;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author PoemsAndDreams
 * @date 2025-01-26 15:01
 * @description //支付二维码返回类
 */
@Data
public class PayRequestVO {

    /**
     * 商户号不能为空
     */
    private Long enterpriseId;
    /**
     *业务系统订单号
     */
    private Long productOrderNo;

    /**
     *支付渠道
     */
    private PayChannelEnum tradingChannel;

    /**
     * 交易金额
     */
    private BigDecimal tradingAmount;


    /**
     * 备注
     */
    private String remark;

}
