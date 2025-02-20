package com.dreams.logistics.service;

import com.dreams.logistics.enums.StatusEnum;
import com.dreams.logistics.model.dto.truckPlan.TruckPlanDto;
import com.dreams.logistics.model.entity.TruckPlan;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
* @author xiayutian
* @description 针对表【truck_plan(车辆计划表)】的数据库操作Service
* @createDate 2025-02-05 15:19:37
*/
public interface TruckPlanService extends IService<TruckPlan> {

    List<TruckPlanDto> pullUnassignedPlan(Integer shardTotal, Integer shardIndex);

    void scheduledPlan(Set<Long> planIds);

    @Transactional
    void finishedPlan(Long currentOrganId, Long planId, Long truckId, StatusEnum statusEnum);

    TruckPlanDto findById(Long truckPlanId);
}
