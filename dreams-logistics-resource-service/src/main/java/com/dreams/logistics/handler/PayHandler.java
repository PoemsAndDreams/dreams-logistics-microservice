package com.dreams.logistics.handler;


import com.dreams.logistics.exception.BusinessException;
import com.dreams.logistics.model.entity.RefundRecord;
import com.dreams.logistics.model.entity.Trading;

/**
 * @ClassName PayHandler.java
 * @Description 支付方式Handler：商户生成二维码，用户扫描支付
 */
public interface PayHandler {


    /***
     * @description 统一收单线下交易预创建
     * 收银员通过收银台或商户后台调用此接口，生成二维码后，展示给用户，由用户扫描二维码完成订单支付。
     * @param trading 交易单
     */
    void createDownLineTrading(Trading trading) throws BusinessException;
    /***
     * 统一收单线下交易查询
     * 该接口提供所有支付订单的查询，商户可以通过该接口主动查询订单状态，完成下一步的业务逻辑。
     * @return 是否有变化
     */
    Boolean queryTrading(Trading trading) throws BusinessException;

    /***
     * 关闭交易
     * @return 是否成功
     */
    Boolean closeTrading(Trading trading) throws BusinessException;

    /***
     * 统一收单交易退款接口
     * 当交易发生之后一段时间内，由于买家或者卖家的原因需要退款时，卖家可以通过退款接口将支付款退还给买家，
     * 将在收到退款请求并且验证成功之后，按照退款规则将支付款按原路退到买家帐号上。
     * @param refundRecord 退款记录对象
     * @return 是否有变化
     */
    Boolean refundTrading(RefundRecord refundRecord) throws BusinessException;

    /***
     * 统一收单交易退款查询接口
     *
     * @param refundRecord 退款交易单号
     * @return 是否有变化
     */
    Boolean queryRefundTrading(RefundRecord refundRecord) throws BusinessException;

}
