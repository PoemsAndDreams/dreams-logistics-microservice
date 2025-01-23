package com.dreams.logistics.handler;

import cn.hutool.core.collection.CollUtil;

import com.dreams.logistics.exception.BusinessException;
import com.dreams.logistics.model.dto.carriage.WaybillDTO;
import com.dreams.logistics.model.entity.Carriage;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

/**
 * 查找运费模板处理链 @Order注解
 */
@Component
public class CarriageChainHandler {

    /**
     * 利用Spring注入特性，按照 @Order 从小到达排序注入到集合中
     */
    @Resource
    private List<AbstractCarriageChainHandler> chainHandlers;

    private AbstractCarriageChainHandler firstHandler;

    /**
     * 组装处理链
     */
    @PostConstruct
    private void constructChain() {
        if (CollUtil.isEmpty(chainHandlers)) {
            throw new BusinessException(60001,"not found carriage chain handler!");
        }
        //处理链中第一个节点
        firstHandler = chainHandlers.get(0);
        for (int i = 0; i < chainHandlers.size(); i++) {
            if (i == chainHandlers.size() - 1) {
                //最后一个处理链节点
                chainHandlers.get(i).setNextHandler(null);
            } else {
                //设置下游节点
                chainHandlers.get(i).setNextHandler(chainHandlers.get(i + 1));
            }
        }
    }

    public Carriage findCarriage(WaybillDTO waybillDTO) {
        //从第一个节点开始处理
        return firstHandler.doHandler(waybillDTO);
    }

}
