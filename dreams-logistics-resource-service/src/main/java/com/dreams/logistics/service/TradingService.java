package com.dreams.logistics.service;

import com.dreams.logistics.enums.TradingStateEnum;
import com.dreams.logistics.model.entity.Trading;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author xiayutian
* @description 针对表【trading(交易订单表)】的数据库操作Service
* @createDate 2025-01-25 14:15:02
*/
public interface TradingService extends IService<Trading> {

    Trading findTradByTradingOrderNo(Long tradingOrderNo);

    Trading findTradByProductOrderNo(Long productOrderNo);

    List<Trading> findListByTradingState(TradingStateEnum tradingStateEnum, Integer tradingCount);
}
