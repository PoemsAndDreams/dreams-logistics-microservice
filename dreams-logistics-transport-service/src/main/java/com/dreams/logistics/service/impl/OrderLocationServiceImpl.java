package com.dreams.logistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreams.logistics.model.entity.OrderCargo;
import com.dreams.logistics.model.entity.OrderLocation;
import com.dreams.logistics.service.OrderLocationService;
import com.dreams.logistics.mapper.OrderLocationMapper;
import org.springframework.stereotype.Service;

/**
* @author xiayutian
* @description 针对表【order_location(订单位置信息 )】的数据库操作Service实现
* @createDate 2025-02-04 08:47:38
*/
@Service
public class OrderLocationServiceImpl extends ServiceImpl<OrderLocationMapper, OrderLocation>
    implements OrderLocationService{

    @Override
    public OrderLocation getByOrderId(Long orderId) {
        LambdaQueryWrapper<OrderLocation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderLocation::getOrderId,orderId);
        return this.getOne(wrapper);
    }
}




