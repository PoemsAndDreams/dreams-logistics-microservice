package com.dreams.logistics.service;

import com.dreams.logistics.model.entity.RefundRecord;
import com.dreams.logistics.model.entity.Trading;

import java.math.BigDecimal;

/**
 * @author PoemsAndDreams
 * @date 2025-01-26 17:10
 * @description //检查类，避免事务失效
 */
public interface CheckService {
    void checkCreateTrading(Trading trading);

    void checkIdempotentCreateTrading(Trading trading);

    void checkGetTrading(Trading trading);

    void checkRefundTrading(Trading trading);

    RefundRecord checkIdempotentRefundTrading(Trading trading, BigDecimal refundAmount);

    void checkQueryRefundTrading(RefundRecord refundRecord);
}
