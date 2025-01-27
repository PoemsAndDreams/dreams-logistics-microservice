package com.dreams.logistics.service.impl;

import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreams.logistics.enums.RefundStatusEnum;
import com.dreams.logistics.model.entity.RefundRecord;
import com.dreams.logistics.service.RefundRecordService;
import com.dreams.logistics.mapper.RefundRecordMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author xiayutian
* @description 针对表【refund_record(退款记录表)】的数据库操作Service实现
* @createDate 2025-01-25 14:15:02
*/
@Service
public class RefundRecordServiceImpl extends ServiceImpl<RefundRecordMapper, RefundRecord>
    implements RefundRecordService{

    @Override
    public List<RefundRecord> findListByTradingOrderNo(Long tradingOrderNo) {

        LambdaQueryWrapper<RefundRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RefundRecord::getTradingOrderNo, tradingOrderNo);
        queryWrapper.orderByDesc(RefundRecord::getCreated);
        return super.list(queryWrapper);

    }

    @Override
    public RefundRecord findByRefundNo(Long refundNo) {
        LambdaQueryWrapper<RefundRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RefundRecord::getRefundNo, refundNo);
        return super.getOne(queryWrapper);
    }

    @Override
    public List<RefundRecord> findListByRefundStatus(RefundStatusEnum refundStatusEnum, Integer refundCount) {
        refundCount = NumberUtil.max(refundCount, 10);
        LambdaQueryWrapper<RefundRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RefundRecord::getRefundStatus, refundStatusEnum)
                .orderByAsc(RefundRecord::getCreated)
                .last("LIMIT " + refundCount);
        return list(queryWrapper);
    }
}




