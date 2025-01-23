package com.dreams.logistics.handler;


import com.dreams.logistics.model.dto.carriage.WaybillDTO;
import com.dreams.logistics.model.entity.Carriage;

/**
 * 运费模板处理链的抽象定义
 */
public abstract class AbstractCarriageChainHandler {

    private AbstractCarriageChainHandler nextHandler;

    /**
     * 执行过滤方法，通过输入参数查找运费模板
     *
     * @param waybillDTO 输入参数
     * @return 运费模板
     */
    public abstract Carriage doHandler(WaybillDTO waybillDTO);

    /**
     * 执行下一个处理器
     *
     * @param waybillDTO     输入参数
     * @param carriage 上个handler处理得到的对象
     * @return
     */
    protected Carriage doNextHandler(WaybillDTO waybillDTO, Carriage carriage) {
        if (nextHandler == null || carriage != null) {
            //如果下游Handler为空 或 上个Handler已经找到运费模板就返回
            return carriage;
        }
        return nextHandler.doHandler(waybillDTO);
    }

    /**
     * 设置下游Handler
     *
     * @param nextHandler 下游Handler
     */
    public void setNextHandler(AbstractCarriageChainHandler nextHandler) {
        this.nextHandler = nextHandler;
    }
}
