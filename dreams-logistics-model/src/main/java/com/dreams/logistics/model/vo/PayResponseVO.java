package com.dreams.logistics.model.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author PoemsAndDreams
 * @date 2025-01-26 15:01
 * @description //支付二维码返回类
 */
@Data
public class PayResponseVO {

    /**
     *业务系统订单号
     */
    private Long productOrderNo;

    /**
     *交易系统订单号
     */
    private Long tradingOrderNo;

    /**
     * 二维码
     */
    private String qrCode;

}
