package com.dreams.logistics.service;

import com.dreams.logistics.model.entity.PayChannel;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author xiayutian
* @description 针对表【pay_channel】的数据库操作Service
* @createDate 2025-01-25 14:15:02
*/
public interface PayChannelService extends IService<PayChannel> {

    PayChannel findByEnterpriseId(Long enterpriseId, String tradingChannelAliPay);
}
