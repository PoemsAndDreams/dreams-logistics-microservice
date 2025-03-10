package com.dreams.logistics.service;

import com.dreams.logistics.model.entity.TransportOrder;
import com.dreams.logistics.model.entity.TransportOrderTask;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author xiayutian
* @description 针对表【transport_order_task(运单与运输任务关联表)】的数据库操作Service
* @createDate 2025-02-05 15:19:37
*/
public interface TransportOrderTaskService extends IService<TransportOrderTask> {

    void batchSaveTransportOrder(List<TransportOrderTask> resultList);

    List<TransportOrderTask> findAll(String transportOrderId, Long transportTaskId);

    List<TransportOrder> getByTransportTaskId(Long id);
}
