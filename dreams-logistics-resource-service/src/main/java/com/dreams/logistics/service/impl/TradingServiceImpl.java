package com.dreams.logistics.service.impl;

import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreams.logistics.enums.TradingStateEnum;
import com.dreams.logistics.model.entity.Trading;
import com.dreams.logistics.service.TradingService;
import com.dreams.logistics.mapper.TradingMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author xiayutian
* @description 针对表【trading(交易订单表)】的数据库操作Service实现
* @createDate 2025-01-25 14:15:02
*/
@Service
public class TradingServiceImpl extends ServiceImpl<TradingMapper, Trading>
    implements TradingService{

    @Override
    public Trading findTradByTradingOrderNo(Long tradingOrderNo) {
        LambdaQueryWrapper<Trading> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Trading::getTradingOrderNo, tradingOrderNo);
        return super.getOne(queryWrapper);
    }

    @Override
    public Trading findTradByProductOrderNo(Long productOrderNo) {
        LambdaQueryWrapper<Trading> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Trading::getProductOrderNo, productOrderNo);
        return super.getOne(queryWrapper);
    }

    @Override
    public List<Trading> findListByTradingState(TradingStateEnum tradingStateEnum, Integer tradingCount) {
        tradingCount = NumberUtil.max(tradingCount, 10);
        LambdaQueryWrapper<Trading> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Trading::getTradingState, tradingStateEnum)
                .eq(Trading::getEnableFlag, Constants.YES)
                .orderByAsc(Trading::getCreated)
                .last("LIMIT " + tradingCount);
        return list(queryWrapper);
    }
}




