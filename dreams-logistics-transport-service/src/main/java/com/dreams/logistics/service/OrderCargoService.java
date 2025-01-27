package com.dreams.logistics.service;

import com.dreams.logistics.model.entity.OrderCargo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author xiayutian
* @description 针对表【order_cargo(货品总重量 )】的数据库操作Service
* @createDate 2025-01-25 16:21:23
*/
public interface OrderCargoService extends IService<OrderCargo> {


    OrderCargo getByOrderId(Long id);
}
