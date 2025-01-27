package com.dreams.logistics.handler.alipay;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.kernel.Config;
import com.alipay.easysdk.kernel.util.ResponseChecker;
import com.alipay.easysdk.payment.common.models.AlipayTradeCloseResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeQueryResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeRefundResponse;
import com.alipay.easysdk.payment.facetoface.models.AlipayTradePrecreateResponse;
import com.dreams.logistics.constant.TradingConstant;
import com.dreams.logistics.enums.PayChannelEnum;
import com.dreams.logistics.enums.RefundStatusEnum;
import com.dreams.logistics.enums.TradingEnum;
import com.dreams.logistics.enums.TradingStateEnum;
import com.dreams.logistics.exception.BusinessException;
import com.dreams.logistics.handler.PayHandler;
import com.dreams.logistics.model.entity.PayChannelInter;
import com.dreams.logistics.model.entity.RefundRecord;
import com.dreams.logistics.model.entity.Trading;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 支付宝的扫描支付的具体实现
 */
@Slf4j
@Component("aliPayHandler")
@PayChannelInter(type = PayChannelEnum.ALI_PAY)
public class AliPayHandler implements PayHandler {

    @Override
    public void createDownLineTrading(Trading trading) throws BusinessException {
        //查询配置
        Config config = AlipayConfig.getConfig(trading.getEnterpriseId());
        //Factory使用配置
        Factory.setOptions(config);
        AlipayTradePrecreateResponse response;

        String baseUrl = StrUtil.addSuffixIfNot(config.notifyUrl, "/");
        String notifyUrl = baseUrl + trading.getEnterpriseId();

        try {
            //调用支付宝API面对面支付
            response = Factory
                    .Payment
                    .FaceToFace()
                    .asyncNotify(notifyUrl)
                    .preCreate(trading.getRemark(), //订单描述
                            Convert.toStr(trading.getTradingOrderNo()), //业务订单号
                            Convert.toStr(trading.getTradingAmount())); //金额
        } catch (Exception e) {
            log.error("支付宝统一下单创建失败：trading = {}", trading, e);
            throw new BusinessException(TradingEnum.NATIVE_PAY_FAIL, e);
        }

        //受理结果【只表示请求是否成功，而不是支付是否成功】
        boolean isSuccess = ResponseChecker.success(response);
        //6.1、受理成功：修改交易单
        if (isSuccess) {
            String subCode = response.getSubCode();
            String subMsg = response.getQrCode();
            trading.setPlaceOrderCode(subCode); //返回的编码
            trading.setPlaceOrderMsg(subMsg); //二维码需要展现的信息
            trading.setPlaceOrderJson(JSONUtil.toJsonStr(response));
            trading.setTradingState(TradingStateEnum.FKZ);
            return;
        }
        throw new BusinessException(JSONUtil.toJsonStr(response), TradingEnum.NATIVE_PAY_FAIL.getCode());
    }

    @Override
    public Boolean queryTrading(Trading trading) throws BusinessException {
        //查询配置
        Config config = AlipayConfig.getConfig(trading.getEnterpriseId());
        //Factory使用配置
        Factory.setOptions(config);
        AlipayTradeQueryResponse queryResponse;
        try {
            //调用支付宝API：通用查询支付情况
            queryResponse = Factory
                    .Payment
                    .Common()
                    .query(String.valueOf(trading.getTradingOrderNo()));
        } catch (Exception e) {
            String msg = StrUtil.format("查询支付宝统一下单失败：trading = {}", trading);
            log.error(msg, e);
            throw new BusinessException(msg, TradingEnum.NATIVE_QUERY_FAIL.getCode());
        }

        //修改交易单状态
        trading.setResultCode(queryResponse.getSubCode());
        trading.setResultMsg(queryResponse.getSubMsg());
        trading.setResultJson(JSONUtil.toJsonStr(queryResponse));

        boolean success = ResponseChecker.success(queryResponse);
        //响应成功，分析交易状态
        if (success) {
            String tradeStatus = queryResponse.getTradeStatus();
            if (StrUtil.equals(TradingConstant.ALI_TRADE_CLOSED, tradeStatus)) {
                //支付取消：TRADE_CLOSED（未付款交易超时关闭，或支付完成后全额退款）
                trading.setTradingState(TradingStateEnum.QXDD);
            } else if (StrUtil.equalsAny(tradeStatus, TradingConstant.ALI_TRADE_SUCCESS, TradingConstant.ALI_TRADE_FINISHED)) {
                // TRADE_SUCCESS（交易支付成功）
                // TRADE_FINISHED（交易结束，不可退款）
                trading.setTradingState(TradingStateEnum.YJS);
            } else {
                //非最终状态不处理，当前交易状态：WAIT_BUYER_PAY（交易创建，等待买家付款）不处理
                return false;
            }
            return true;
        }
        throw new BusinessException(trading.getResultJson(), TradingEnum.NATIVE_QUERY_FAIL.getCode());
    }

    @Override
    public Boolean closeTrading(Trading trading) throws BusinessException {
        //查询配置
        Config config = AlipayConfig.getConfig(trading.getEnterpriseId());
        //Factory使用配置
        Factory.setOptions(config);
        try {
            //调用支付宝API：通用查询支付情况
            AlipayTradeCloseResponse closeResponse = Factory
                    .Payment
                    .Common()
                    .close(String.valueOf(trading.getTradingOrderNo()));
            boolean success = ResponseChecker.success(closeResponse);
            if (success) {
                trading.setTradingState(TradingStateEnum.QXDD);
                return true;
            }
            return false;
        } catch (Exception e) {
            throw new BusinessException(TradingEnum.CLOSE_FAIL, e);
        }
    }

    @Override
    public Boolean refundTrading(RefundRecord refundRecord) throws BusinessException {
        //查询配置
        Config config = AlipayConfig.getConfig(refundRecord.getEnterpriseId());
        //Factory使用配置
        Factory.setOptions(config);
        //调用支付宝API：通用查询支付情况
        AlipayTradeRefundResponse refundResponse;
        try {
            // 支付宝easy sdk
            refundResponse = Factory
                    .Payment
                    .Common()
                    //扩展参数：退款单号
                    .optional("out_request_no", refundRecord.getRefundNo())
                    .refund(Convert.toStr(refundRecord.getTradingOrderNo()),
                            Convert.toStr(refundRecord.getRefundAmount()));
        } catch (Exception e) {
            String msg = StrUtil.format("调用支付宝退款接口出错！refundRecord = {}", refundRecord);
            log.error(msg, e);
            throw new BusinessException(msg, TradingEnum.NATIVE_REFUND_FAIL.getCode());
        }
        refundRecord.setRefundCode(refundResponse.getSubCode());
        refundRecord.setRefundMsg(JSONUtil.toJsonStr(refundResponse));
        boolean success = ResponseChecker.success(refundResponse);
        if (success) {
            refundRecord.setRefundStatus(RefundStatusEnum.SENDING);
            return true;
        }
        throw new BusinessException(refundRecord.getRefundMsg(), TradingEnum.NATIVE_REFUND_FAIL.getCode());
    }

    @Override
    public Boolean queryRefundTrading(RefundRecord refundRecord) throws BusinessException {
        //查询配置
        Config config = AlipayConfig.getConfig(refundRecord.getEnterpriseId());
        //Factory使用配置
        Factory.setOptions(config);
        AlipayTradeFastpayRefundQueryResponse response;
        try {
            response = Factory.Payment.Common().queryRefund(
                    Convert.toStr(refundRecord.getTradingOrderNo()),
                    Convert.toStr(refundRecord.getRefundNo()));
        } catch (Exception e) {
            log.error("调用支付宝查询退款接口出错！refundRecord = {}", refundRecord, e);
            throw new BusinessException(TradingEnum.NATIVE_REFUND_FAIL, e);
        }

        refundRecord.setRefundCode(response.getSubCode());
        refundRecord.setRefundMsg(JSONUtil.toJsonStr(response));
        boolean success = ResponseChecker.success(response);
        if (success) {
            refundRecord.setRefundStatus(RefundStatusEnum.SUCCESS);
            return true;
        }
        throw new BusinessException(refundRecord.getRefundMsg(), TradingEnum.NATIVE_REFUND_FAIL.getCode());
    }

}
