package com.dreams.logistics.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.dreams.logistics.common.BaseResponse;
import com.dreams.logistics.common.ErrorCode;
import com.dreams.logistics.common.ResultUtils;
import com.dreams.logistics.enums.TradingEnum;
import com.dreams.logistics.enums.TradingStateEnum;
import com.dreams.logistics.exception.BusinessException;
import com.dreams.logistics.model.entity.Trading;
import com.dreams.logistics.model.vo.PayRequestVO;
import com.dreams.logistics.model.vo.PayResponseVO;
import com.dreams.logistics.model.vo.RefundRecordVO;
import com.dreams.logistics.model.vo.TradingVO;
import com.dreams.logistics.service.PayService;
import com.dreams.logistics.service.TradingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

/**
 * 支付相关接口
 */
@RestController
@RequestMapping("/pay")
@Slf4j
public class PayController {

    @Resource
    private PayService payService;

    @Resource
    private TradingService tradingService;

    /**
     * 扫码支付
     * @param payRequestVO
     * @return
     */
    @PostMapping("/create")
    public BaseResponse<PayResponseVO> createPay(@RequestBody PayRequestVO payRequestVO){

        Trading trading = BeanUtil.toBean(payRequestVO,Trading.class);

        return ResultUtils.success(payService.createPay(trading));
    }



    /**
     * 查看二维码
     *
     * @param tradingOrderNo 交易单号
     * @return 二维码图片 base64格式
     */
    @GetMapping("getCode/{tradingOrderNo}")
    public BaseResponse<String> getQrCode(@PathVariable("tradingOrderNo") Long tradingOrderNo) {
        return ResultUtils.success(payService.queryQrCodeUrl(tradingOrderNo));
    }



    /***
     * 统一收单线下交易查询
     *
     * @param tradingOrderNo 交易单号
     * @return 交易单
     */
    @PostMapping("query/{tradingOrderNo}")
    public BaseResponse<TradingVO> queryTrading(@PathVariable("tradingOrderNo") Long tradingOrderNo) {
        return ResultUtils.success(payService.getTradingByTradingOrderNo(tradingOrderNo));
    }


    /***
     * 统一收单交易退款接口
     * @param tradingOrderNo 交易单号
     * @param refundAmount 退款金额
     * @return
     */
    @PostMapping("refund")
    public void refundTrading(@RequestParam("tradingOrderNo") Long tradingOrderNo,
                              @RequestParam("refundAmount") BigDecimal refundAmount) {
        Boolean result = payService.refundTrading(tradingOrderNo, refundAmount);
        if (!result) {
            throw new BusinessException(TradingEnum.BASIC_REFUND_COUNT_OUT_FAIL);
        }
    }

    /***
     * 统一收单交易退款查询接口
     * @param refundNo 退款交易单号
     * @return 退款记录
     */
    @PostMapping("refund/{refundNo}")
    public BaseResponse<RefundRecordVO> queryRefundDownLineTrading(@PathVariable("refundNo") Long refundNo) {
        return ResultUtils.success(payService.queryRefundTrading(refundNo));
    }


    /**
     * 支付宝支付成功回调（成功后需要响应success）
     *
     * @param enterpriseId 商户id
     * @return 正常响应200，否则响应500
     */
    @PostMapping("alipay/notify/{enterpriseId}")
    public BaseResponse<String> notifyAliPay(HttpServletRequest request,
                                               @PathVariable("enterpriseId") Long enterpriseId) {
        try {
            //支付宝通知的业务处理
            payService.notifyAliPay(request, enterpriseId);
        } catch (BusinessException e) {
            //响应500
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtils.success("success");
    }




}
