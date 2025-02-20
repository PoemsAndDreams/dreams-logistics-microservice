package com.dreams.logistics.controller.inner;

import cn.hutool.core.bean.BeanUtil;
import com.dreams.logistics.enums.PickupDispatchTaskType;
import com.dreams.logistics.enums.StatusEnum;
import com.dreams.logistics.model.dto.transport.CourierTaskCountDTO;
import com.dreams.logistics.model.dto.truckPlan.TruckPlanDto;
import com.dreams.logistics.model.dto.workSchedule.WorkScheduleAddRequest;
import com.dreams.logistics.model.entity.Area;
import com.dreams.logistics.model.entity.TruckPlan;
import com.dreams.logistics.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author PoemsAndDreams
 * @date 2025-02-10 20:50
 * @description //TODO
 */
@RestController
@RequestMapping("/inner")
@Slf4j
public class TransportInnerController implements TransportFeignClient {

    @Resource
    private WorkScheduleService workScheduleService;

    @Resource
    private TruckPlanService truckPlanService;

    @Resource
    private PickupDispatchTaskService pickupDispatchTaskService;
    @Resource
    private ScopeService scopeService;

    @Resource
    private AreaService areaService;

    @Override
    @PostMapping("/add")
    public Boolean add(@RequestBody WorkScheduleAddRequest workScheduleAddRequest) {
        return workScheduleService.saveWorkSchedule(workScheduleAddRequest);
    }

    /**
     * 获取未分配运输任务的车次计划列表
     * @return 未分配运输任务的车次计划列表
     */
    @PostMapping("/unassignedPlan")
    @Override
    public List<TruckPlanDto> pullUnassignedPlan(@RequestParam(name = "shardTotal") Integer shardTotal, @RequestParam(name = "shardIndex") Integer shardIndex) {
        // 查询计划
        return truckPlanService.pullUnassignedPlan(shardTotal, shardIndex);
    }

    /**
     * 根据ID获取
     * @param id 数据库ID
     * @return 返回信息
     */
    @PostMapping("/{id}")
    @Override
    public TruckPlanDto findById(Long id) {
        TruckPlan truckPlanEntity = truckPlanService.getById(id);
        return BeanUtil.toBean(truckPlanEntity, TruckPlanDto.class);
    }

    /**
     * 计划完成
     * @param currentOrganId 结束机构id
     * @param planId          计划ID
     * @param truckId 车辆ID
     * @param statusEnum 车辆状态枚举
     * @return 车次与车辆和司机关联关系列表
     */
    @PostMapping("finished")
    @Override
    public void finished(@RequestParam("currentOrganId") Long currentOrganId, @RequestParam("planId") Long planId, @RequestParam("truckId") Long truckId, @RequestParam("statusEnum") StatusEnum statusEnum) {
        truckPlanService.finishedPlan(currentOrganId, planId, truckId, statusEnum);
    }


    @Override
    @PostMapping("count")
    public List<CourierTaskCountDTO> findCountByCourierIds(@RequestParam("courierIds") List<Long> courierIds,
                                                           @RequestParam("taskType") PickupDispatchTaskType taskType,
                                                           @RequestParam("date") String date){
        return this.pickupDispatchTaskService.findCountByCourierIds(courierIds, taskType, date);

    }


    @Override
    @PostMapping("/queryCourierIdListByCondition")
    public List<Long> queryCourierIdListByCondition(@RequestParam("agencyId") Long agencyId,
                                                    @RequestParam("longitude") Double longitude,
                                                    @RequestParam("latitude") Double latitude,
                                                    @RequestParam("estimatedEndTime") Long estimatedEndTime){
        return scopeService.queryCourierIdListByCondition(agencyId, longitude, latitude, estimatedEndTime);

    }

    @PostMapping("/area/id")
    @Override
    public Area findAreaById(@RequestParam("id") String id) {
        return areaService.getById(id);
    }

}
