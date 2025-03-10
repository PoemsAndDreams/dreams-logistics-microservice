package com.dreams.logistics.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SimpleQuery;
import com.dreams.logistics.constant.TruckPlanScheduleStatusEnum;
import com.dreams.logistics.constant.TruckPlanStatusEnum;
import com.dreams.logistics.entity.line.DispatchConfiguration;
import com.dreams.logistics.enums.StatusEnum;
import com.dreams.logistics.exception.BusinessException;
import com.dreams.logistics.mapper.TruckPlanMapper;
import com.dreams.logistics.model.dto.truckPlan.OrganIdsDto;
import com.dreams.logistics.model.dto.truckPlan.TruckDto;
import com.dreams.logistics.model.dto.truckPlan.TruckPlanDto;
import com.dreams.logistics.model.entity.Truck;
import com.dreams.logistics.model.entity.TruckDriver;
import com.dreams.logistics.model.entity.TruckPlan;
import com.dreams.logistics.model.entity.TruckTrips;
import com.dreams.logistics.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 车次与车辆关联信息表 服务类
 */
@Slf4j
@Service
public class TruckPlanServiceImpl extends ServiceImpl<TruckPlanMapper, TruckPlan>
        implements TruckPlanService {

    @Resource
    TruckService truckService;

    @Resource
    TruckPlanCreateService truckPlanCreateService;

    @Resource
    TruckTripsService truckTripsService;

    @Resource
    private TruckDriverService truckDriverService;

    @Resource
    private DispatchConfigurationService dispatchConfigurationService;

    /**
     * 获取未分配运输任务的车次计划列表
     *
     * @param shardTotal 总片数
     * @param shardIndex 分片
     * @return 未分配运输任务的车次计划列表
     */
    @Override
    public List<TruckPlanDto> pullUnassignedPlan(Integer shardTotal, Integer shardIndex) {
        //调度时间配置
        Integer dispatchTime = null;
        DispatchConfiguration configuration = dispatchConfigurationService.findConfiguration();
        if (ObjectUtil.isNotEmpty(configuration)) {
            dispatchTime = configuration.getDispatchTime();
        }
        int time = dispatchTime != null ? dispatchTime : 2;

        // 分片拉取 保证多个调度器均衡负载
        LambdaQueryWrapper<TruckPlan> lambdaQueryWrapper = new LambdaQueryWrapper<TruckPlan>()
                // 车辆状态是待分配
                .eq(TruckPlan::getScheduleStatus, TruckPlanScheduleStatusEnum.UNASSIGNED.getCode())
                // 后俩小时之前
                .le(TruckPlan::getPlanDepartureTime, LocalDateTime.now().plusHours(time))
                // 状态正常
                .eq(TruckPlan::getStatus, TruckPlanStatusEnum.NORMAL.getCode())
                // ID取模分片
                .apply(" MOD(id, {0}) = {1}", shardTotal, shardIndex)
                // 增序
                .orderByAsc(TruckPlan::getPlanDepartureTime)
                // 最多1000条
                .last("limit 1000");
        // 查询
        List<TruckPlan> records = super.list(lambdaQueryWrapper);
        if (CollectionUtils.isEmpty(records)) {
            log.error("获取未分配运输任务的车次计划列表 暂无满足条件计划 shardTotal {} shardIndex {}", shardTotal, shardIndex);
            return new ArrayList<>();
        }

        // 补充数据
        List<TruckPlanDto> truckPlanDtos = toUnassignedDto(records, shardTotal, shardIndex);
        if (CollectionUtils.isEmpty(truckPlanDtos)) {
            return new ArrayList<>();
        }

        // 修改计划调度状态
        LambdaUpdateWrapper<TruckPlan> updateWrapper = Wrappers.lambdaUpdate(TruckPlan.class)
                // 设置为已分配
                .set(TruckPlan::getScheduleStatus, TruckPlanScheduleStatusEnum.ASSIGNED.getCode())
                // 条件为待分配 保证不会因为同一shardIndex并发导致数据不一致
                .eq(TruckPlan::getScheduleStatus, TruckPlanScheduleStatusEnum.UNASSIGNED.getCode())
                // 修改范围
                .in(TruckPlan::getId, records.stream().map(TruckPlan::getId).collect(Collectors.toSet()));
        // 修改
        if (update(updateWrapper)) {
            // 返回修改后的数据
            return truckPlanDtos;
        }

        return new ArrayList<>();
    }

    /**
     * 转换结构
     *
     * @param records    计划数据
     * @param shardTotal 总片数
     * @param shardIndex 当前片数
     * @return 车辆计划DTO
     */
    private List<TruckPlanDto> toUnassignedDto(List<TruckPlan> records, Integer shardTotal, Integer shardIndex) {
        // 转换为dto
        List<TruckPlanDto> truckPlanDtos = records.stream()
                .map(truckPlanEntity -> {
                    TruckPlanDto truckPlanDto = BeanUtil.toBean(truckPlanEntity, TruckPlanDto.class);
                    String[] split = truckPlanEntity.getDriverIds().split(",");
                    if (ObjectUtil.isEmpty(split)) {
                        List<Long> list = Arrays.stream(split).map(Long::valueOf).collect(Collectors.toList());
                        truckPlanDto.setDriverIds(list);
                    }
                    return truckPlanDto;
                })
                .collect(Collectors.toList());

        // 根据车辆ID查询车辆
        List<Long> truckIds = truckPlanDtos.parallelStream().map(TruckPlanDto::getTruckId).collect(Collectors.toList());
        Map<Long, Truck> longTruckEntityMap = SimpleQuery.keyMap(
                Wrappers.<Truck>lambdaQuery().in(Truck::getId, truckIds),
                Truck::getId);
        if (CollectionUtils.isEmpty(longTruckEntityMap)) {
            log.error("获取未分配运输任务的车次计划列表 暂无满足条件车辆 shardTotal {} shardIndex {}", shardTotal, shardIndex);
            return new ArrayList<>();
        }

        // 根据车次ID获取<车次ID,线路ID>map
        List<Long> transportTripsIds = truckPlanDtos.parallelStream().map(TruckPlanDto::getTransportTripsId).collect(Collectors.toList());
        Map<Long, Long> truckTripsLineMap = SimpleQuery.map(
                Wrappers.<TruckTrips>lambdaQuery().in(TruckTrips::getId, transportTripsIds),
                TruckTrips::getId,
                TruckTrips::getTransportLineId);
        if (CollectionUtils.isEmpty(truckTripsLineMap)) {
            log.error("获取未分配运输任务的车次计划列表 暂无满足条件线路 shardTotal {} shardIndex {}", shardTotal, shardIndex);
            return new ArrayList<>();
        }

        // 根据线路ID查询起始位置
        Map<Long, OrganIdsDto> organIdsMap = truckTripsService.getOrganIdsByTripsLineId(new HashSet<>(truckTripsLineMap.values()));
        if (CollectionUtils.isEmpty(organIdsMap)) {
            log.error("获取未分配运输任务的车次计划列表 远端暂无满足条件机构ids shardTotal {} shardIndex {}", shardTotal, shardIndex);
            return new ArrayList<>();
        }

        // 合并
        truckPlanDtos.parallelStream().forEach(v -> {
            // 车辆
            Truck truck = longTruckEntityMap.get(v.getTruckId());
            v.setTruckDto(BeanUtil.toBean(truck, TruckDto.class));
            // 线路ID
            Long truckTripsLineId = truckTripsLineMap.get(v.getTransportTripsId());
            // 起始位置
            OrganIdsDto organIdsDto = organIdsMap.get(truckTripsLineId);
            v.setTransportLineId(truckTripsLineId);
            if (ObjectUtil.isNotEmpty(organIdsDto)) {
                BeanUtil.copyProperties(organIdsDto, v);
            }
        });

        return truckPlanDtos;
    }

    /**
     * 更新计划状态为已调度 消费MQ更新调度状态
     *
     * @param planIds 计划ID
     */
    @Override
    public void scheduledPlan(Set<Long> planIds) {
        // 修改计划调度状态
        LambdaUpdateWrapper<TruckPlan> updateWrapper = Wrappers.lambdaUpdate(TruckPlan.class)
                // 设置为已调度
                .set(TruckPlan::getScheduleStatus, TruckPlanScheduleStatusEnum.SCHEDULED.getCode())
                // 条件为已分配
                .eq(TruckPlan::getScheduleStatus, TruckPlanScheduleStatusEnum.ASSIGNED.getCode())
                // 修改范围
                .in(TruckPlan::getId, planIds);
        // 修改
        update(updateWrapper);
    }

    /**
     * 计划完成
     *
     * @param currentOrganId 结束机构id
     * @param planId         计划ID
     * @param truckId        车辆ID
     * @param statusEnum     车辆状态枚举
     */
    @Transactional
    @Override
    public void finishedPlan(Long currentOrganId, Long planId, Long truckId, StatusEnum statusEnum) {
        log.info("计划完成 currentOrganId {} planId {} ", currentOrganId, planId);
        TruckPlan truckPlanEntity = getById(planId);
        if (ObjectUtil.isEmpty(truckPlanEntity)) {
            throw new BusinessException(StrUtil.format("计划不存在currentOrganId {} planId {} ", currentOrganId, planId));
        }
        // 本次计划完成
        truckPlanEntity.setStatus(TruckPlanStatusEnum.ARRIVED.getCode());
        boolean updateById = updateById(truckPlanEntity);
        if (!updateById) {
            log.error("更新计划状态失败 currentOrganId {} planId {} ", currentOrganId, planId);
            throw new BusinessException(StrUtil.format("更新计划状态失败currentOrganId {} planId {} ", currentOrganId, planId));
        }

        // 设置车辆位置 可能回车登记的车辆 并不是计划中的车辆
        Boolean updateCurrentOrganId = truckService.updateCurrentOrganId(truckId, currentOrganId, statusEnum);
        if (!updateCurrentOrganId) {
            log.error("更新车辆位置失败 currentOrganId {} planId {} ", currentOrganId, planId);
            throw new BusinessException(StrUtil.format("更新车辆位置失败currentOrganId {} planId {} ", currentOrganId, planId));
        }

        // 构建下一次计划 异步
        // 司机应该是和该车关联的所有司机
        List<TruckDriver> truckDriverEntities = truckDriverService.findByTruckId(truckPlanEntity.getTruckId());
        List<Long> driverIds = truckDriverEntities.stream().map(TruckDriver::getDriverId).collect(Collectors.toList());
        CompletableFuture<String> nextPlans = truckPlanCreateService.createNextPlans(truckPlanEntity.getTruckId(), driverIds, currentOrganId);
        nextPlans.whenComplete((s, throwable) -> {
            if (!s.equals("ok")) {
                log.info("构建下一次计划 失败 truckPlanEntity {}", truckPlanEntity);
            }
        });
    }

    @Override
    public TruckPlanDto findById(Long truckPlanId) {
        TruckPlan truckPlanEntity = this.getById(truckPlanId);
        return BeanUtil.toBean(truckPlanEntity, TruckPlanDto.class);
    }
}
