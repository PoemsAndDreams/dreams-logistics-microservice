package com.dreams.logistics.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreams.logistics.enums.TransportTaskStatus;
import com.dreams.logistics.enums.WorkExceptionEnum;
import com.dreams.logistics.exception.BusinessException;
import com.dreams.logistics.model.dto.transport.TransportTaskDTO;
import com.dreams.logistics.model.dto.transport.request.TransportTaskCompleteDTO;
import com.dreams.logistics.model.dto.transport.request.TransportTaskStartDTO;
import com.dreams.logistics.model.entity.TransportOrder;
import com.dreams.logistics.model.entity.TransportOrderTask;
import com.dreams.logistics.model.entity.TransportTask;
import com.dreams.logistics.service.TransportOrderTaskService;
import com.dreams.logistics.service.TransportTaskService;
import com.dreams.logistics.mapper.TransportTaskMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
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

    @Override
    public TransportTaskDTO findById(Long transportTaskId) {
        TransportTask transportTask = this.getById(transportTaskId);
        TransportTaskDTO transportTaskDTO = BeanUtil.toBean(transportTask, TransportTaskDTO.class);

        List<TransportOrder> transportOrders = transportOrderTaskService.getByTransportTaskId(transportTaskDTO.getId());
        transportTaskDTO.setTransportOrders(transportOrders);
        return transportTaskDTO;
    }

    @Override
    public void completeTransportTask(TransportTaskCompleteDTO transportTaskCompleteDTO) {
        TransportTask transportTask = new TransportTask();
        transportTask.setId(Long.valueOf(transportTaskCompleteDTO.getTransportTaskId()));
        // 已完成
        transportTask.setStatus(TransportTaskStatus.COMPLETED);
        transportTask.setTransportCertificate(transportTaskCompleteDTO.getTransportCertificate());
        transportTask.setDeliverPicture(transportTaskCompleteDTO.getDeliverPicture());

        //实际到达时间
        transportTask.setActualArrivalTime(LocalDateTime.now());
        super.updateById(transportTask);
    }

    @Override
    public void startTransportTask(TransportTaskStartDTO transportTaskStartDTO) {
        TransportTask transportTask = new TransportTask();
        transportTask.setId(Long.valueOf(transportTaskStartDTO.getTransportTaskId()));

        // 进行中
        transportTask.setStatus(TransportTaskStatus.PROCESSING);
        transportTask.setCargoPickUpPicture(transportTaskStartDTO.getCargoPickUpPicture());
        transportTask.setCargoPicture(transportTaskStartDTO.getCargoPicture());

        //实际发车时间
        transportTask.setActualDepartureTime(LocalDateTime.now());
        super.updateById(transportTask);
    }

    @Override
    public Boolean updateStatus(Long id, TransportTaskStatus status) {

        if (TransportTaskStatus.PENDING == status) {
            //修改运输任务状态不能为 待执行 状态
            throw new BusinessException(WorkExceptionEnum.TRANSPORT_TASK_STATUS_NOT_PENDING);
        }

        if (TransportTaskStatus.PROCESSING == status) {
            // 开始任务
            TransportTaskStartDTO transportTaskStartDTO = new TransportTaskStartDTO();
            transportTaskStartDTO.setTransportTaskId(String.valueOf(id));
            this.startTransportTask(transportTaskStartDTO);
            return true;
        } else if (TransportTaskStatus.COMPLETED == status) {
            // 完成任务
            TransportTaskCompleteDTO transportTaskCompleteDTO = new TransportTaskCompleteDTO();
            transportTaskCompleteDTO.setTransportTaskId(String.valueOf(id));
            this.completeTransportTask(transportTaskCompleteDTO);
            return true;
        } else {
            //修改其他状态
            TransportTask transportTask = new TransportTask();
            transportTask.setId(id);
            transportTask.setStatus(status);
            return super.updateById(transportTask);
        }


    }

    @Override
    public List<TransportTaskDTO> findAllByOrderIdOrTaskId(String transportOrderId, Long transportTaskId) {
        if (ObjectUtil.isAllEmpty(transportOrderId, transportTaskId)) {
            throw new BusinessException(WorkExceptionEnum.TRANSPORT_TASK_QUERY_PARAM_ERROR);
        }

        if (ObjectUtil.isNotEmpty(transportTaskId)) {
            TransportTask transportTask = super.getById(transportTaskId);
            if (ObjectUtil.isEmpty(transportTask)) {
                throw new BusinessException(WorkExceptionEnum.TRANSPORT_TASK_NOT_FOUND);
            }
            return ListUtil.toList(BeanUtil.toBean(transportTask, TransportTaskDTO.class));
        }

        //通过关联表查询出运输任务id列表
        List<TransportOrderTask> transportOrderTaskList = transportOrderTaskService.findAll(transportOrderId, null);
        if (CollUtil.isEmpty(transportOrderTaskList)) {
            return ListUtil.empty();
        }

        //根据运输任务id列表查询 运输任务数据列表
        List<Long> transportTaskIds = CollUtil.getFieldValues(transportOrderTaskList, "transportTaskId", Long.class);
        List<TransportTask> transportTasList = super.listByIds(transportTaskIds);
        return BeanUtil.copyToList(transportTasList, TransportTaskDTO.class);
    }

    @Override
    public List<Long> findByAgencyId(Long startAgencyId, Long endAgencyId) {
        //1.构造查询条件
        LambdaQueryWrapper<TransportTask> queryWrapper = Wrappers.<TransportTask>lambdaQuery()
                .eq(ObjectUtil.isNotEmpty(startAgencyId), TransportTask::getStartAgencyId, startAgencyId)
                .eq(ObjectUtil.isNotEmpty(endAgencyId), TransportTask::getEndAgencyId, endAgencyId);

        //2.根据起始机构查询运输任务
        List<TransportTask> transportTaskEntityList = this.list(queryWrapper);

        //3.从运输任务中抽取id
        return transportTaskEntityList.stream().map(TransportTask::getId).collect(Collectors.toList());
    }
}




