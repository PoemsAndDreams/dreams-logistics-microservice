package com.dreams.logistics.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.dreams.logistics.model.dto.workSchedule.WorkScheduleAddRequest;
import com.dreams.logistics.model.dto.workSchedule.WorkScheduleQueryRequest;
import com.dreams.logistics.model.dto.workSchedule.WorkScheduleUpdateRequest;
import com.dreams.logistics.model.entity.Area;
import com.dreams.logistics.model.entity.WorkSchedule;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dreams.logistics.model.vo.WorkScheduleVO;

import java.time.LocalDateTime;
import java.util.List;

/**
* @author xiayutian
* @description 针对表【work_schedule】的数据库操作Service
* @createDate 2025-02-07 08:07:45
*/
public interface WorkScheduleService extends IService<WorkSchedule> {

    boolean saveWorkSchedule(WorkScheduleAddRequest workScheduleAddRequest);

    boolean updateWorkSchedule(WorkScheduleUpdateRequest workScheduleUpdateRequest);

    WorkScheduleVO getWorkSchedule(long id);

    Wrapper<WorkSchedule> getQueryWrapper(WorkScheduleQueryRequest workScheduleQueryRequest);

    List<WorkScheduleVO> getWorkScheduleVO(List<WorkSchedule> records);

    WorkScheduleVO getWorkScheduleVO(WorkSchedule workSchedule);

    List<Long> getWorkingDrivers(List<Long> driverIds, LocalDateTime planDepartureTime, LocalDateTime planArrivalTime);

    List<WorkScheduleVO> employeeSchedule(List<Long> userIds,  LocalDateTime estimatedEndTime);
}
