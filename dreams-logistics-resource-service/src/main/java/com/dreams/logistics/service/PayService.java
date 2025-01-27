package com.dreams.logistics.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreams.logistics.model.dto.order.OrderAddRequest;
import com.dreams.logistics.model.dto.order.OrderSearchRequest;
import com.dreams.logistics.model.dto.order.OrderUpdateRequest;
import com.dreams.logistics.model.entity.Trading;
import com.dreams.logistics.model.vo.OrderVO;
import com.dreams.logistics.model.vo.PayResponseVO;
import com.dreams.logistics.model.vo.RefundRecordVO;
import com.dreams.logistics.model.vo.TradingVO;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

/**
 * @author PoemsAndDreams
 * @date 2025-01-25 15:47
 * @description //TODO
 */
public interface PayService {

    PayResponseVO createPay(Trading trading);

    String queryQrCodeUrl(Long tradingOrderNo);

    TradingVO getTradingByTradingOrderNo(Long tradingOrderNo);

    Boolean refundTrading(Long tradingOrderNo, BigDecimal refundAmount);

    RefundRecordVO queryRefundTrading(Long refundNo);

    void notifyAliPay(HttpServletRequest request, Long enterpriseId);

    void updateTrading(Long tradingOrderNo, String message, String jsonStr);
}
