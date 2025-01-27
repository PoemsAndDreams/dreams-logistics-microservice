package com.dreams.logistics.service;

import com.dreams.logistics.enums.RefundStatusEnum;
import com.dreams.logistics.model.entity.RefundRecord;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author xiayutian
* @description 针对表【refund_record(退款记录表)】的数据库操作Service
* @createDate 2025-01-25 14:15:02
*/
public interface RefundRecordService extends IService<RefundRecord> {

    List<RefundRecord> findListByTradingOrderNo(Long tradingOrderNo);


    RefundRecord findByRefundNo(Long refundNo);

    List<RefundRecord> findListByRefundStatus(RefundStatusEnum refundStatusEnum, Integer refundCount);
}
