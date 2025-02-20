package com.dreams.logistics.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dreams.logistics.enums.PickupDispatchTaskIsDeleted;
import com.dreams.logistics.enums.PickupDispatchTaskStatus;
import com.dreams.logistics.enums.PickupDispatchTaskType;
import com.dreams.logistics.model.dto.pickupDispatchTask.PickupDispatchTaskQueryRequest;
import com.dreams.logistics.model.dto.transport.CourierTaskCountDTO;
import com.dreams.logistics.model.dto.pickupDispatchTask.PickupDispatchTaskDTO;
import com.dreams.logistics.model.dto.transport.response.PickupDispatchTaskStatisticsDTO;
import com.dreams.logistics.model.entity.DcUser;
import com.dreams.logistics.model.entity.PickupDispatchTask;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dreams.logistics.model.vo.PickupDispatchTaskVO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
* @author xiayutian
* @description 针对表【pickup_dispatch_task(取件、派件任务信息表)】的数据库操作Service
* @createDate 2025-02-16 20:10:20
*/
public interface PickupDispatchTaskService extends IService<PickupDispatchTask> {


    PickupDispatchTask saveTaskPickupDispatch(PickupDispatchTask taskPickupDispatch);

    List<CourierTaskCountDTO> findCountByCourierIds(List<Long> courierIds, PickupDispatchTaskType pickupDispatchTaskType, String date);

    List<PickupDispatchTaskDTO> findTodayTaskByCourierId(Long courierId);

    List<PickupDispatchTask> findByOrderId(Long orderId, PickupDispatchTaskType taskType);

    List<PickupDispatchTask> findByOrderId(Long orderId);

    boolean deleteByIds(List<Long> ids);

    Boolean updateCourierId(Long id, Long originalCourierId, Long targetCourierId);

    QueryWrapper<PickupDispatchTask> getQueryWrapper(PickupDispatchTaskQueryRequest pickupDispatchTaskQueryRequest);

    List<PickupDispatchTaskVO> getPickupDispatchTaskVO(List<PickupDispatchTask> records);

    PickupDispatchTaskVO getPickupDispatchTaskVO(PickupDispatchTask pickupDispatchTask);

    @Transactional
    Boolean updateStatus(PickupDispatchTask pickupDispatchTask);
}
