package com.dreams.logistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreams.logistics.model.entity.OrderCargo;
import com.dreams.logistics.service.OrderCargoService;
import com.dreams.logistics.mapper.OrderCargoMapper;
import org.springframework.stereotype.Service;

/**
* @author xiayutian
* @description 针对表【order_cargo(货品总重量 )】的数据库操作Service实现
* @createDate 2025-01-25 16:21:23
*/
@Service
public class OrderCargoServiceImpl extends ServiceImpl<OrderCargoMapper, OrderCargo>
    implements OrderCargoService{

    @Override
    public OrderCargo getByOrderId(Long id) {
        LambdaQueryWrapper<OrderCargo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderCargo::getOrderId,id);
        return this.getOne(wrapper);
    }
}




