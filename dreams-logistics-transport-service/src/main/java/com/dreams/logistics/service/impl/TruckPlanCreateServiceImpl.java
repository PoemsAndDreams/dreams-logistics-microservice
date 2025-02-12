package com.dreams.logistics.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SimpleQuery;
import com.dreams.logistics.constant.TruckPlanScheduleStatusEnum;
import com.dreams.logistics.constant.TruckPlanStatusEnum;
import com.dreams.logistics.constant.TruckTripsPeriodEnum;
import com.dreams.logistics.exception.BusinessException;
import com.dreams.logistics.mapper.TruckPlanMapper;
import com.dreams.logistics.model.entity.TruckDriver;
import com.dreams.logistics.model.entity.TruckPlan;
import com.dreams.logistics.model.entity.TruckTrips;
import com.dreams.logistics.model.entity.TruckTripsTruckDriver;
import com.dreams.logistics.service.TruckDriverService;
import com.dreams.logistics.service.TruckPlanCreateService;

import com.dreams.logistics.service.TruckTripsService;
import com.dreams.logistics.service.WorkScheduleService;
import com.dreams.logistics.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TruckPlanCreateServiceImpl extends ServiceImpl<TruckPlanMapper, TruckPlan>
        implements TruckPlanCreateService {

    @Resource
    private TruckTripsService truckTripsService;

    @Resource
    private WorkScheduleService  workScheduleService;

    @Resource
    private TruckDriverService truckDriverService;

    /**
     * 创建首次计划 首次关系后台新增 安排司机和车辆给车次时候触发
     *
     * @param truckPlan 车辆计划
     */
    @Override
    public void createPlan(TruckPlan truckPlan) {
        truckPlan.setScheduleStatus(TruckPlanScheduleStatusEnum.UNASSIGNED.getCode());

        // 根据车次周期计算间隔天数
        TruckTrips truckTrips = truckTripsService.getById(truckPlan.getTransportTripsId());
        // 计划发车时间 车次的发车时间 今日的分钟数
        LocalDateTime planDepartureTime = DateUtils.getStartTime(LocalDateTime.now()).plusMinutes(truckTrips.getDepartureTime());
        truckPlan.setPlanDepartureTime(planDepartureTime);
        // 计划到达时间
        LocalDateTime planArrivalTime = truckPlan.getPlanDepartureTime().plusMinutes(truckTrips.getEstimatedTime());
        truckPlan.setPlanArrivalTime(planArrivalTime);

        // 排班
        List<TruckDriver> truckDriver = truckDriverService.findByTruckId(truckPlan.getTruckId());
        List<Long> driverIds = truckDriver.stream().map(TruckDriver::getDriverId).collect(Collectors.toList());

        List<Long> workingDrivers =  workScheduleService.getWorkingDrivers(driverIds, planDepartureTime, planArrivalTime);

        if (workingDrivers.size() < 2) {
            throw new BusinessException("车辆至少配置俩名上班的司机才能执行运输任务");
        }
        // 设置司机
        truckPlan.setDriverIds(StrUtil.join(",", workingDrivers));
        // 插入一条新数据
        super.save(truckPlan);
    }

    /**
     * 异步创建下一次计划
     * 前一次计划完成触发
     *
     * @param truckId        车辆ID
     * @param driverIds      司机ID列表
     * @param currentOrganId 当前位置
     * @return 异步任务
     */
    @Override
    @Async
    public CompletableFuture<String> createNextPlans(Long truckId, List<Long> driverIds, Long currentOrganId) {
        // 根据车辆ID获取车次IDs  如果解除了绑定 计划也会被弃用
        List<Long> transportTripsIds = SimpleQuery.list(
                Wrappers.<TruckTripsTruckDriver>lambdaQuery().eq(TruckTripsTruckDriver::getTruckId, truckId),
                TruckTripsTruckDriver::getTransportTripsId);

        if (CollectionUtils.isEmpty(transportTripsIds)) {
            log.error("选举车次 车次不存在 truckId {} currentOrganId {}", truckId, currentOrganId);
            return CompletableFuture.completedFuture("ok");
        }
        // 创建所有车次的计划
        transportTripsIds.stream().distinct().forEach(v -> createNextPlan(truckId, driverIds, v));
        // 异步任务返回
        return CompletableFuture.completedFuture("ok");
    }

    /**
     * 创建某个节点后续每条线路的计划
     *
     * @param truckId          车辆ID
     * @param driverIds        司机ID列表
     * @param transportTripsId 车次ID
     */
    private void createNextPlan(Long truckId, List<Long> driverIds, Long transportTripsId) {
        // 查询最新一条车次计划
        TruckPlan last = getOne(Wrappers.<TruckPlan>lambdaQuery()
                .eq(TruckPlan::getTruckId, truckId)
                .eq(TruckPlan::getTransportTripsId, transportTripsId)
                .orderByDesc(TruckPlan::getPlanDepartureTime)
                // 1条
                .last("limit 1")
        );

        TruckPlan truckPlanNew = TruckPlan.builder()
                // 车辆id
                .truckId(truckId)
                // 设置计划状态
                .status(TruckPlanStatusEnum.NORMAL.getCode())
                // 调度状态
                .scheduleStatus(TruckPlanScheduleStatusEnum.UNASSIGNED.getCode())
                // 车次id
                .transportTripsId(transportTripsId).build();

        // 根据车次周期计算间隔天数
        TruckTrips truckTrips = truckTripsService.getById(transportTripsId);

        // 最后的发车时间
        LocalDateTime prePlanDepartureTime = last.getPlanDepartureTime();

        // 计划发车时间
        //如果车次周期是月，则通过每月加一个月来计算发车时间，直到该时间是未来时间。
        //如果车次周期是周或日，则分别加上1天或7天来计算发车时间，并确保该时间为未来时间。
        LocalDateTime planDepartureTime;
        if (truckTrips.getPeriod().equals(TruckTripsPeriodEnum.MONTH.getCode())) {
            // 周期为月的情况 循环直到是未来的时间
            planDepartureTime = prePlanDepartureTime.plusMonths(1);
            while (planDepartureTime.isBefore(LocalDateTime.now())) {
                planDepartureTime = prePlanDepartureTime.plusMonths(1);
            }
        } else {
            int day = truckTrips.getPeriod().equals(TruckTripsPeriodEnum.WEEK.getCode()) ? 7 : 1;
            // 周期为周 / 日 的情况 循环直到是未来的时间
            planDepartureTime = prePlanDepartureTime.plusDays(day);
            while (planDepartureTime.isBefore(LocalDateTime.now())) {
                planDepartureTime = prePlanDepartureTime.plusDays(day);
            }
        }
        // 按照最新的车次时间设置计划发车时间
        LocalDateTime planDepartureTimeNew = DateUtils.getStartTime(planDepartureTime).plusMinutes(truckTrips.getDepartureTime());
        truckPlanNew.setPlanDepartureTime(planDepartureTimeNew);

        // 计划到达时间
        LocalDateTime planArrivalTime = planDepartureTime.plusMinutes(truckTrips.getEstimatedTime());
        truckPlanNew.setPlanArrivalTime(planArrivalTime);

        // 整合排班
        List<Long> workingDrivers =  workScheduleService.getWorkingDrivers(driverIds, planDepartureTime, planArrivalTime);
        // 设置司机
        truckPlanNew.setDriverIds(StrUtil.join(",", workingDrivers));

        // 去重复
        long count = count(Wrappers.<TruckPlan>lambdaQuery()
                        .eq(TruckPlan::getTruckId, truckId)
                        .eq(TruckPlan::getTransportTripsId, transportTripsId)
                        .between(TruckPlan::getPlanDepartureTime, truckPlanNew.getPlanDepartureTime().minusMinutes(1), truckPlanNew.getPlanDepartureTime().plusMinutes(1))
        );
        if (count == 0) {
            // 插入一条新数据
            super.save(truckPlanNew);
        }
    }
}
