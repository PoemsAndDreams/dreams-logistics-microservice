package com.dreams.logistics.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreams.logistics.model.entity.TransportOrderTask;
import com.dreams.logistics.model.entity.TransportTask;
import com.dreams.logistics.service.TransportOrderTaskService;
import com.dreams.logistics.service.TransportTaskService;
import com.dreams.logistics.mapper.TransportTaskMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author xiayutian
* @description 针对表【transport_task(运输任务表)】的数据库操作Service实现
* @createDate 2025-02-05 15:19:37
*/
@Service
public class TransportTaskServiceImpl extends ServiceImpl<TransportTaskMapper, TransportTask>
    implements TransportTaskService{

    @Resource
    private TransportOrderTaskService transportOrderTaskService;

    @Override
    public List<String> queryTransportOrderIdListById(Long id) {
        //通过运输任务找到运单id列表
        LambdaQueryWrapper<TransportOrderTask> queryWrapper = new LambdaQueryWrapper<TransportOrderTask>().eq(TransportOrderTask::getTransportTaskId, id);
        List<TransportOrderTask> orderList = this.transportOrderTaskService.list(queryWrapper);
        if (CollUtil.isEmpty(orderList)) {
            return Collections.emptyList();
        }
        //运单id列表
        return orderList.stream().map(TransportOrderTask::getTransportOrderId).collect(Collectors.toList());
    }
}




