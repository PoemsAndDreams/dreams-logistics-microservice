package com.dreams.logistics.service;

import com.dreams.logistics.model.entity.TransportTask;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author xiayutian
* @description 针对表【transport_task(运输任务表)】的数据库操作Service
* @createDate 2025-02-05 15:19:37
*/
public interface TransportTaskService extends IService<TransportTask> {

    List<String> queryTransportOrderIdListById(Long taskId);
}
