package com.dreams.logistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.dreams.logistics.constant.ConstantsIF;
import com.dreams.logistics.model.entity.PayChannel;
import com.dreams.logistics.service.PayChannelService;
import com.dreams.logistics.mapper.PayChannelMapper;
import org.springframework.stereotype.Service;

/**
* @author xiayutian
* @description 针对表【pay_channel】的数据库操作Service实现
* @createDate 2025-01-25 14:15:02
*/
@Service
public class PayChannelServiceImpl extends ServiceImpl<PayChannelMapper, PayChannel>
    implements PayChannelService{

    @Override
    public PayChannel findByEnterpriseId(Long enterpriseId, String channelLabel) {
        
        LambdaQueryWrapper<PayChannel> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PayChannel::getEnterpriseId, enterpriseId)
                .eq(PayChannel::getChannelLabel, channelLabel)
                .eq(PayChannel::getEnableFlag, ConstantsIF.YES);
        //TODO 缓存
        return super.getOne(queryWrapper);
        
    }
}




