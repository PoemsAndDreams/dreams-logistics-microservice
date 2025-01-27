package com.dreams.logistics.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.kernel.Config;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreams.logistics.common.Constants;
import com.dreams.logistics.common.ErrorCode;
import com.dreams.logistics.constant.ConstantsIF;
import com.dreams.logistics.constant.TradingCacheConstant;
import com.dreams.logistics.constant.TradingConstant;
import com.dreams.logistics.enums.PayChannelEnum;
import com.dreams.logistics.enums.TradingEnum;
import com.dreams.logistics.enums.TradingStateEnum;
import com.dreams.logistics.exception.BusinessException;
import com.dreams.logistics.handler.HandlerFactory;
import com.dreams.logistics.handler.PayHandler;
import com.dreams.logistics.handler.alipay.AlipayConfig;
import com.dreams.logistics.model.dto.msg.TradeStatusMsg;
import com.dreams.logistics.model.dto.order.OrderAddRequest;
import com.dreams.logistics.model.dto.order.OrderSearchRequest;
import com.dreams.logistics.model.dto.order.OrderUpdateRequest;
import com.dreams.logistics.model.entity.Order;
import com.dreams.logistics.model.entity.OrderCargo;
import com.dreams.logistics.model.entity.RefundRecord;
import com.dreams.logistics.model.entity.Trading;
import com.dreams.logistics.model.vo.OrderVO;
import com.dreams.logistics.model.vo.PayResponseVO;
import com.dreams.logistics.model.vo.RefundRecordVO;
import com.dreams.logistics.model.vo.TradingVO;
import com.dreams.logistics.service.*;
import org.apache.commons.lang3.ObjectUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

/**
 * @author PoemsAndDreams
 * @date 2025-01-25 15:47
 * @description //TODO
 */
@Service
public class PayServiceImpl implements PayService {

    @Resource
    private QRCodeService qrCodeService;


    @Resource
    private CheckService checkService;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private TradingService tradingService;
    @Resource
    private RefundRecordService refundRecordService;

    @Resource
    private MQService mqService;



    @Override
    public PayResponseVO createPay(Trading trading) {
        checkService.checkCreateTrading(trading);

        // 如果已经 在 付款中 返回 二维码
        Trading tradingQuery = tradingService.findTradByProductOrderNo(trading.getProductOrderNo());
        if (ObjectUtil.isNotEmpty(tradingQuery)){
            if (ObjectUtil.equals(tradingQuery.getTradingState(), TradingStateEnum.YJS)) {
                //订单已完成，不返回二维码
                throw new BusinessException(TradingEnum.TRADING_STATE_SUCCEED);
            } else if (ObjectUtil.equals(tradingQuery.getTradingState(), TradingStateEnum.FKZ))  {
                PayResponseVO payResponseVO = new PayResponseVO();
                payResponseVO.setProductOrderNo(tradingQuery.getProductOrderNo());
                payResponseVO.setTradingOrderNo(trading.getTradingOrderNo());
                payResponseVO.setQrCode(tradingQuery.getQrCode());
                return payResponseVO;
            }
        }

        trading.setTradingType(TradingConstant.TRADING_TYPE_FK);

        //对交易订单加锁
        Long productOrderNo = trading.getProductOrderNo();
        String key = TradingCacheConstant.CREATE_PAY + productOrderNo;
        RLock lock = redissonClient.getFairLock(key);

        try{
            //获取锁
            if (lock.tryLock(TradingCacheConstant.REDIS_WAIT_TIME, TimeUnit.SECONDS)) {
                //交易前置处理：幂等性处理
                checkService.checkIdempotentCreateTrading(trading);

                //调用不同的支付渠道进行处理
                PayChannelEnum payChannelEnum = PayChannelEnum.valueOf(trading.getTradingChannel());
                PayHandler payHandler = HandlerFactory.get(payChannelEnum, PayHandler.class);
                payHandler.createDownLineTrading(trading);

                //生成统一收款二维码
                String placeOrderMsg = trading.getPlaceOrderMsg();
                String qrCode = qrCodeService.generate(placeOrderMsg, payChannelEnum);
                trading.setQrCode(qrCode);

                //新增或更新交易数据
                Boolean flag = this.tradingService.saveOrUpdate(trading);
                if (!flag) {
                    throw new BusinessException(TradingEnum.SAVE_OR_UPDATE_FAIL);
                }

                PayResponseVO payResponseVO = new PayResponseVO();
                BeanUtils.copyProperties(trading,payResponseVO);
                return payResponseVO;
            }
            throw new BusinessException(TradingEnum.NATIVE_PAY_FAIL);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(TradingEnum.NATIVE_PAY_FAIL);
        } finally {
            lock.unlock();
        }

    }

    @Override
    public String queryQrCodeUrl(Long tradingOrderNo) {
        Trading trading = tradingService.findTradByTradingOrderNo(tradingOrderNo);
        if (ObjectUtil.equals(trading.getTradingState(), TradingStateEnum.YJS)) {
            //订单已完成，不返回二维码
            throw new BusinessException(TradingEnum.TRADING_STATE_SUCCEED);
        }
        return trading.getQrCode();
    }

    @Override
    public TradingVO getTradingByTradingOrderNo(Long tradingOrderNo) {
        Trading trading = tradingService.findTradByTradingOrderNo(tradingOrderNo);

        checkService.checkGetTrading(trading);

        // 查询交易状态加锁
        String key = TradingCacheConstant.QUERY_PAY + tradingOrderNo;
        RLock lock = redissonClient.getFairLock(key);
        try {
            //获取锁
            if (lock.tryLock(TradingCacheConstant.REDIS_WAIT_TIME, TimeUnit.SECONDS)) {
                //选取不同的支付渠道实现
                PayHandler handler = HandlerFactory.get(trading.getTradingChannel(), PayHandler.class);
                Boolean result = handler.queryTrading(trading);
                if (result) {
                    //如果交易单已经完成，需要将二维码数据删除，节省数据库空间，如果有需要可以再次生成
                    if (ObjectUtil.equals(trading.getTradingState(), TradingStateEnum.YJS) && ObjectUtil.equals(trading.getTradingState(), TradingStateEnum.QXDD)) {
                        trading.setQrCode("");
                    }
                    //更新数据
                    this.tradingService.saveOrUpdate(trading);
                }
                return BeanUtil.toBean(trading, TradingVO.class);
            }
            throw new BusinessException(TradingEnum.NATIVE_QUERY_FAIL);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(TradingEnum.NATIVE_QUERY_FAIL);
        } finally {
            lock.unlock();
        }


    }

    @Override
    public Boolean refundTrading(Long tradingOrderNo, BigDecimal refundAmount) {
        //通过单号查询交易单数据
        Trading trading = this.tradingService.findTradByTradingOrderNo(tradingOrderNo);
        //设置退款金额
        trading.setRefund(NumberUtil.add(refundAmount, trading.getRefund()));

        //入库前置检查
        checkService.checkRefundTrading(trading);

        String key = TradingCacheConstant.REFUND_PAY + tradingOrderNo;
        RLock lock = redissonClient.getFairLock(key);
        try {
            //获取锁
            if (lock.tryLock(TradingCacheConstant.REDIS_WAIT_TIME, TimeUnit.SECONDS)) {
                //幂等性的检查
                RefundRecord refundRecord = checkService.checkIdempotentRefundTrading(trading, refundAmount);
                if (null == refundRecord) {
                    return false;
                }

                //选取不同的支付渠道实现
                PayHandler handler = HandlerFactory.get(refundRecord.getTradingChannel(), PayHandler.class);
                Boolean result = handler.refundTrading(refundRecord);
                if (result) {
                    //更新退款记录数据
                    refundRecordService.saveOrUpdate(refundRecord);

                    //设置交易单是退款订单
                    trading.setIsRefund(ConstantsIF.YES);
                    this.tradingService.saveOrUpdate(trading);
                }
                return true;
            }
            throw new BusinessException(TradingEnum.NATIVE_QUERY_FAIL);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(TradingEnum.NATIVE_QUERY_FAIL);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public RefundRecordVO queryRefundTrading(Long refundNo) {
        //通过单号查询交易单数据
        RefundRecord refundRecord = refundRecordService.findByRefundNo(refundNo);
        //查询前置处理
        checkService.checkQueryRefundTrading(refundRecord);

        String key = TradingCacheConstant.REFUND_QUERY_PAY + refundNo;
        RLock lock = redissonClient.getFairLock(key);
        try {
            //获取锁
            if (lock.tryLock(TradingCacheConstant.REDIS_WAIT_TIME, TimeUnit.SECONDS)) {

                //选取不同的支付渠道实现
                PayHandler handler = HandlerFactory.get(refundRecord.getTradingChannel(), PayHandler.class);
                Boolean result = handler.queryRefundTrading(refundRecord);
                if (result) {
                    //更新数据
                    this.refundRecordService.saveOrUpdate(refundRecord);
                }
                return BeanUtil.toBean(refundRecord, RefundRecordVO.class);
            }
            throw new BusinessException(TradingEnum.REFUND_FAIL);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            //log.error("查询退款交易单数据异常: refundRecord = {}", refundRecord, e);
            throw new BusinessException(TradingEnum.REFUND_FAIL);
        } finally {
            lock.unlock();
        }

    }

    @Override
    public void notifyAliPay(HttpServletRequest request, Long enterpriseId) {
        //获取参数
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, String> param = new HashMap<>();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            param.put(entry.getKey(), StrUtil.join(",", entry.getValue()));
        }

        String tradeStatus = param.get("trade_status");
        if (!StrUtil.equals(tradeStatus, TradingConstant.ALI_TRADE_SUCCESS)) {
            return;
        }

        //查询配置
        Config config = AlipayConfig.getConfig(enterpriseId);
        Factory.setOptions(config);
        try {
            Boolean result = Factory
                    .Payment
                    .Common().verifyNotify(param);
            if (!result) {
                throw new BusinessException(ErrorCode.SIGNATURE_VERIFICATION_FAILED);
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SIGNATURE_VERIFICATION_FAILED);
        }


        //获取交易单号
        Long tradingOrderNo = Convert.toLong(param.get("out_trade_no"));
        //更新交易单
        this.updateTrading(tradingOrderNo, "支付成功", JSONUtil.toJsonStr(param));

    }

    @Override
    public void updateTrading(Long tradingOrderNo, String message, String jsonStr) {
        String key = TradingCacheConstant.CREATE_PAY + tradingOrderNo;
        RLock lock = redissonClient.getFairLock(key);
        try {
            //获取锁
            if (lock.tryLock(TradingCacheConstant.REDIS_WAIT_TIME, TimeUnit.SECONDS)) {
                Trading trading = this.tradingService.findTradByTradingOrderNo(tradingOrderNo);
                if (trading.getTradingState() == TradingStateEnum.YJS) {
                    // 已付款
                    return;
                }

                //设置成付款成功
                trading.setTradingState(TradingStateEnum.YJS);
                //清空二维码数据
                trading.setQrCode("");
                trading.setResultMsg(message);
                trading.setResultJson(jsonStr);
                this.tradingService.saveOrUpdate(trading);

                // 发消息通知其他系统支付成功
                TradeStatusMsg tradeStatusMsg = TradeStatusMsg.builder()
                        .tradingOrderNo(trading.getTradingOrderNo())
                        .productOrderNo(trading.getProductOrderNo())
                        .statusCode(TradingStateEnum.YJS.getCode())
                        .statusName(TradingStateEnum.YJS.name())
                        .build();

                String msg = JSONUtil.toJsonStr(Collections.singletonList(tradeStatusMsg));
                mqService.sendMsg(Constants.MQ.Exchanges.TRADE, Constants.MQ.RoutingKeys.TRADE_UPDATE_STATUS, msg);
                return;
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SERVICE_FAILURE);
        } finally {
            lock.unlock();
        }
        throw new BusinessException(ErrorCode.SERVICE_FAILURE);
    }

}
