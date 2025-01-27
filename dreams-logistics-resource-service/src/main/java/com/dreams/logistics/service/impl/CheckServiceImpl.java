package com.dreams.logistics.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.dreams.logistics.enums.RefundStatusEnum;
import com.dreams.logistics.enums.TradingEnum;
import com.dreams.logistics.enums.TradingStateEnum;
import com.dreams.logistics.exception.BusinessException;
import com.dreams.logistics.model.entity.RefundRecord;
import com.dreams.logistics.model.entity.Trading;
import com.dreams.logistics.service.CheckService;
import com.dreams.logistics.service.RefundRecordService;
import com.dreams.logistics.service.TradingService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author PoemsAndDreams
 * @date 2025-01-26 17:11
 * @description //检查类，避免事务失效
 */

@Service
public class CheckServiceImpl implements CheckService {
    
    @Resource
    private TradingService tradingService;
    @Resource
    private IdentifierGenerator identifierGenerator;

    @Resource
    private RefundRecordService refundRecordService;

    @Override
    public void checkCreateTrading(Trading trading) {

        boolean allEmpty = ObjectUtil.isAllEmpty(trading, trading.getProductOrderNo(), trading.getTradingChannel(), trading.getTradingAmount());

        if (allEmpty){
            throw new BusinessException(TradingEnum.CONFIG_ERROR);
        }
    }

    @Override
    public void checkIdempotentCreateTrading(Trading trading) {
        Trading tradingIde = tradingService.findTradByProductOrderNo(trading.getProductOrderNo());
        if (ObjectUtil.isEmpty(tradingIde)) {
            //新交易单，生成交易号
            trading.setTradingOrderNo((Long) identifierGenerator.nextId(trading));
            return;
        }

        TradingStateEnum tradingState = tradingIde.getTradingState();
        if (ObjectUtil.equals(tradingState, TradingStateEnum.YJS) && ObjectUtil.equals(tradingState, TradingStateEnum.MD)) {
            //已结算、免单：直接抛出重复支付异常
            throw new BusinessException(TradingEnum.TRADING_STATE_SUCCEED);
        } else if (ObjectUtil.equals(TradingStateEnum.FKZ, tradingState)) {
            //付款中，如果支付渠道一致，说明是重复，抛出支付中异常，否则需要更换支付渠道
            //举例：第一次通过支付宝付款，付款中用户取消，改换了微信支付
            if (StrUtil.equals(trading.getTradingChannel(), tradingIde.getTradingChannel())) {
                throw new BusinessException(TradingEnum.TRADING_STATE_PAYING);
            } else {
                trading.setId(tradingIde.getId()); // id设置为原订单的id
                //重新生成交易号，在这里就会出现id 与 TradingOrderNo 数据不同的情况，其他情况下是一样的
                trading.setTradingOrderNo(Convert.toLong(identifierGenerator.nextId(trading)));
            }
        } else if (ObjectUtil.equals(tradingState, TradingStateEnum.QXDD) && ObjectUtil.equals(tradingState, TradingStateEnum.GZ)) {
            //取消订单,挂账：创建交易号，对原交易单发起支付
            trading.setId(tradingIde.getId()); // id设置为原订单的id
            //重新生成交易号，在这里就会出现id 与 TradingOrderNo 数据不同的情况，其他情况下是一样的
            trading.setTradingOrderNo(Convert.toLong(identifierGenerator.nextId(trading)));
        } else {
            //其他情况：直接交易失败
            throw new BusinessException(TradingEnum.PAYING_TRADING_FAIL);
        }
        
        
    }

    @Override
    public void checkGetTrading(Trading trading) {
        if (ObjectUtil.isEmpty(trading)) {
            throw new BusinessException(TradingEnum.NOT_FOUND);
        }

        //校验交易单是否已经完成或已取消
        TradingStateEnum tradingState = trading.getTradingState();
        if (ObjectUtil.equals(tradingState, TradingStateEnum.YJS) && ObjectUtil.equals(tradingState, TradingStateEnum.QXDD)) {
            throw new BusinessException(TradingEnum.TRADING_STATE_SUCCEED);
        }
    }

    @Override
    public void checkRefundTrading(Trading trading) {
        if (ObjectUtil.isEmpty(trading)) {
            throw new BusinessException(TradingEnum.NOT_FOUND);
        }

        if (trading.getTradingState() != TradingStateEnum.YJS) {
            throw new BusinessException(TradingEnum.NATIVE_REFUND_FAIL);
        }

        //退款总金额不可超实付总金额
        if (NumberUtil.isGreater(trading.getRefund(), trading.getTradingAmount())) {
            throw new BusinessException(TradingEnum.BASIC_REFUND_OUT_FAIL);
        }
    }

    @Override
    public RefundRecord checkIdempotentRefundTrading(Trading trading, BigDecimal refundAmount) {
        //查询退款次数，不能大于20次
        List<RefundRecord> recordList = refundRecordService.findListByTradingOrderNo(trading.getTradingOrderNo());
        int size = CollUtil.size(recordList);
        if (size >= 20) {
            return null;
        }

        RefundRecord refundRecord = new RefundRecord();
        //退款单号
        refundRecord.setRefundNo(Convert.toLong(this.identifierGenerator.nextId(refundRecord)));
        refundRecord.setTradingOrderNo(trading.getTradingOrderNo());
        refundRecord.setProductOrderNo(trading.getProductOrderNo());
        refundRecord.setRefundAmount(refundAmount);
        refundRecord.setEnterpriseId(trading.getEnterpriseId());
        refundRecord.setTradingChannel(trading.getTradingChannel());
        refundRecord.setRefundStatus(RefundStatusEnum.SENDING);
        refundRecord.setTotal(trading.getTradingAmount());
        String remark = StrUtil.format("退款（{}）", size + 1);
        refundRecord.setRemark(remark);

        return refundRecord;


    }

    @Override
    public void checkQueryRefundTrading(RefundRecord refundRecord) {
        if (ObjectUtil.isEmpty(refundRecord)) {
            throw new BusinessException(TradingEnum.REFUND_NOT_FOUND);
        }

        if (ObjectUtil.equals(refundRecord.getRefundStatus(), RefundStatusEnum.SUCCESS)) {
            throw new BusinessException(TradingEnum.REFUND_ALREADY_COMPLETED);
        }
    }
}
