package com.dreams.logistics.service;

import com.dreams.logistics.enums.TransportTaskStatus;
import com.dreams.logistics.model.dto.transport.TransportTaskDTO;
import com.dreams.logistics.model.dto.transport.request.TransportTaskCompleteDTO;
import com.dreams.logistics.model.dto.transport.request.TransportTaskStartDTO;
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

    TransportTaskDTO findById(Long transportTaskId);

    void completeTransportTask(TransportTaskCompleteDTO transportTaskCompleteDTO);

    void startTransportTask(TransportTaskStartDTO transportTaskStartDTO);

    Boolean updateStatus(Long id, TransportTaskStatus status);

    List<TransportTaskDTO> findAllByOrderIdOrTaskId(String transportOrderId, Long taskTransportId);

    List<Long> findByAgencyId(Long startAgencyId, Long endAgencyId);

}
