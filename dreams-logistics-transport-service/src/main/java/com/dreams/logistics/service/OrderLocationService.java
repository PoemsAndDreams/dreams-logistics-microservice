package com.dreams.logistics.service;

import com.dreams.logistics.model.entity.OrderLocation;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author xiayutian
* @description 针对表【order_location(订单位置信息 )】的数据库操作Service
* @createDate 2025-02-04 08:47:38
*/
public interface OrderLocationService extends IService<OrderLocation> {

    OrderLocation getByOrderId(Long orderId);
}
