package com.dreams.logistics.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreams.logistics.model.dto.msg.TradeStatusMsg;
import com.dreams.logistics.model.dto.order.OrderAddRequest;
import com.dreams.logistics.model.dto.order.OrderSearchRequest;
import com.dreams.logistics.model.dto.order.OrderUpdateRequest;
import com.dreams.logistics.model.entity.DcUser;
import com.dreams.logistics.model.entity.Order;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dreams.logistics.model.vo.OrderVO;

import java.util.List;

/**
* @author xiayutian
* @description 针对表【order(订单)】的数据库操作Service
* @createDate 2025-01-25 14:15:02
*/
public interface OrderService extends IService<Order> {

    Page<OrderVO> page(OrderSearchRequest orderSearchRequest);

    Boolean saveOrderVO(OrderAddRequest orderAddRequest);

    boolean removeOrder(Long id);

    boolean updateOrder(OrderUpdateRequest orderUpdateRequest);

    OrderVO getOrderVOById(long id);

    QueryWrapper<Order> getQueryWrapper(OrderSearchRequest orderSearchRequest);

    void updatePayStatus(List<Long> ids, Integer status);

    void updateRefundInfo(List<TradeStatusMsg> msgList);
}
