package com.dreams.logistics.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreams.logistics.model.entity.TransportOrder;
import com.dreams.logistics.model.entity.TransportOrderTask;
import com.dreams.logistics.service.TransportOrderService;
import com.dreams.logistics.service.TransportOrderTaskService;
import com.dreams.logistics.mapper.TransportOrderTaskMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author xiayutian
* @description 针对表【transport_order_task(运单与运输任务关联表)】的数据库操作Service实现
* @createDate 2025-02-05 15:19:37
*/
@Service
public class TransportOrderTaskServiceImpl extends ServiceImpl<TransportOrderTaskMapper, TransportOrderTask>
    implements TransportOrderTaskService{

    @Resource
    private TransportOrderService transportOrderService;

    @Override
    public void batchSaveTransportOrder(List<TransportOrderTask> transportOrderTaskList) {
        saveBatch(transportOrderTaskList);
    }

    @Override
    public List<TransportOrderTask> findAll(String transportOrderId, Long transportTaskId) {
        LambdaQueryWrapper<TransportOrderTask> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(ObjectUtil.isNotEmpty(transportOrderId), TransportOrderTask::getTransportOrderId, transportOrderId);
        lambdaQueryWrapper.like(ObjectUtil.isNotEmpty(transportTaskId), TransportOrderTask::getTransportTaskId, transportTaskId);
        lambdaQueryWrapper.orderBy(true, false, TransportOrderTask::getCreated);
        return list(lambdaQueryWrapper);
    }

    @Override
    public List<TransportOrder> getByTransportTaskId(Long id) {
        LambdaQueryWrapper<TransportOrderTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TransportOrderTask::getTransportTaskId,id);
        List<TransportOrderTask> list = this.list(wrapper);
        return list.stream().map(record -> {
            return transportOrderService.getById(record.getTransportOrderId());
        }).collect(Collectors.toList());
    }
}




