package com.dreams.logistics.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreams.logistics.enums.TransportOrderStatus;
import com.dreams.logistics.model.dto.msg.OrderMsg;
import com.dreams.logistics.model.dto.transport.TransportOrderDTO;
import com.dreams.logistics.model.dto.transport.request.TransportOrderQueryDTO;
import com.dreams.logistics.model.entity.TransportOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
* @author xiayutian
* @description 针对表【transport_order(运单表)】的数据库操作Service
* @createDate 2025-02-05 15:19:37
*/
public interface TransportOrderService extends IService<TransportOrder> {

    @Transactional
    TransportOrder orderToTransportOrder(Long orderId);

    Page<TransportOrder> findByPage(TransportOrderQueryDTO transportOrderQueryDTO);

    TransportOrder findByOrderId(Long orderId);

    List<TransportOrder> findByOrderIds(Long[] orderIds);

    List<TransportOrder> findByIds(String[] ids);

    List<TransportOrder> searchById(String id);

    boolean updateStatus(List<String> ids, TransportOrderStatus transportOrderStatus);

    boolean updateByTaskId(Long taskId);

    void sendPickupDispatchTaskMsgToDispatch(TransportOrder transportOrder, OrderMsg orderMsg);

    Page<TransportOrderDTO> pageQueryByTaskId(Integer page, Integer pageSize, String taskId, String transportOrderId);
}
