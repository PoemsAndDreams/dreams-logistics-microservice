package com.dreams.logistics.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreams.logistics.constant.TruckPlanScheduleStatusEnum;
import com.dreams.logistics.constant.TruckPlanStatusEnum;
import com.dreams.logistics.exception.BusinessException;
import com.dreams.logistics.model.entity.*;
import com.dreams.logistics.service.TruckDriverService;
import com.dreams.logistics.service.TruckPlanCreateService;
import com.dreams.logistics.service.TruckService;
import com.dreams.logistics.service.TruckTripsTruckDriverService;
import com.dreams.logistics.mapper.TruckTripsTruckDriverMapper;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author xiayutian
* @description 针对表【truck_trips_truck_driver(车次与车辆和司机关联表)】的数据库操作Service实现
* @createDate 2025-02-05 15:19:37
*/
@Service
public class TruckTripsTruckDriverServiceImpl extends ServiceImpl<TruckTripsTruckDriverMapper, TruckTripsTruckDriver>
    implements TruckTripsTruckDriverService{

    @Resource
    TruckDriverService truckDriverService;

    @Resource
    TruckPlanCreateService truckPlanCreateService;


    @Resource
    private TruckService truckService;
    /**
     * 批量保存车次与车辆关联信息
     */
    @Transactional
    @Override
    public void batchSave(Long transportTripsId, List<String> licensePlateList) {
        // 保存车辆和车次关联关系
        // 1,清除关系
        delete(transportTripsId, null);

        List<TruckTripsTruckDriver> saveList = new ArrayList<>();
        //遍历传入数据
        licensePlateList.forEach(licensePlate -> {

            Truck truck = truckService.getTruck(licensePlate);
            List<TruckDriver> driverEntities = truckDriverService.findByTruckId(truck.getId());
            if (CollUtil.isEmpty(driverEntities)) {
                throw new BusinessException(StrUtil.format("请先为该车辆绑定司机"));
            }
            driverEntities.forEach(truckDriver -> {
                TruckTripsTruckDriver saveData = new TruckTripsTruckDriver();
                saveData.setTruckId(truck.getId());
                saveData.setDriverId(truckDriver.getDriverId());
                saveData.setTransportTripsId(transportTripsId);
                saveList.add(saveData);
            });

            // 触发创建首次车辆计划
            List<Long> driverIds = driverEntities.stream().map(TruckDriver::getDriverId).collect(Collectors.toList());

            TruckPlan build = TruckPlan.builder()
                    .truckId(truck.getId())
                    .transportTripsId(transportTripsId)
                    .driverIds(StrUtil.join(",", driverIds))
                    .status(TruckPlanStatusEnum.NORMAL.getCode())
                    .build();
            truckPlanCreateService.createPlan(build);
        });
        saveBatch(saveList);
    }

    /**
     * 获取车次与车辆关联列表
     *
     * @param transportTripsId 车次id
     * @param truckId          车辆Id
     * @param userId           司机id
     * @return 车次与车辆关联列表
     */
    @Override
    public List<TruckTripsTruckDriver> findAll(Long transportTripsId, Long truckId, Long userId) {
        LambdaQueryWrapper<TruckTripsTruckDriver> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (ObjectUtils.isNotEmpty(transportTripsId)) {
            lambdaQueryWrapper.eq(TruckTripsTruckDriver::getTransportTripsId, transportTripsId);
        }
        if (ObjectUtils.isNotEmpty(truckId)) {
            lambdaQueryWrapper.eq(TruckTripsTruckDriver::getTruckId, truckId);
        }
        if (ObjectUtils.isNotEmpty(userId)) {
            lambdaQueryWrapper.eq(TruckTripsTruckDriver::getDriverId, userId);
        }
        return baseMapper.selectList(lambdaQueryWrapper);
    }

    /**
     * 检查是否可以删除
     *
     * @param transportTripsId 车次id
     * @param truckId          车辆Id
     * @param userId           司机id
     * @return 是否可以删除
     */
    @Override
    public Boolean canRemove(Long transportTripsId, Long truckId, Long userId) {
        LambdaQueryWrapper<TruckTripsTruckDriver> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (ObjectUtils.isNotEmpty(transportTripsId)) {
            lambdaQueryWrapper.eq(TruckTripsTruckDriver::getTransportTripsId, transportTripsId);
        }
        if (ObjectUtils.isNotEmpty(truckId)) {
            lambdaQueryWrapper.eq(TruckTripsTruckDriver::getTruckId, truckId);
        }
        if (ObjectUtils.isNotEmpty(userId)) {
            lambdaQueryWrapper.eq(TruckTripsTruckDriver::getDriverId, userId);
        }
        return baseMapper.selectCount(lambdaQueryWrapper) == 0;
    }

    /**
     * 消除绑定关系
     * @param transportTripsId 车次ID
     * @param truckId 车辆ID
     */
    @Transactional
    @Override
    public void delete(Long transportTripsId, Long truckId) {
        // 删除车辆和车次关联关系
        LambdaQueryWrapper<TruckTripsTruckDriver> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ObjectUtils.isNotEmpty(transportTripsId), TruckTripsTruckDriver::getTransportTripsId, transportTripsId)
                .eq(ObjectUtils.isNotEmpty(truckId), TruckTripsTruckDriver::getTruckId, truckId);
        //清除关系
        baseMapper.delete(lambdaQueryWrapper);

        // 删除没有被调度的计划
        truckPlanCreateService.remove(Wrappers.<TruckPlan>lambdaUpdate()
                .eq(ObjectUtils.isNotEmpty(transportTripsId), TruckPlan::getTransportTripsId, transportTripsId)
                .eq(ObjectUtils.isNotEmpty(truckId), TruckPlan::getTruckId, truckId)
                .eq(TruckPlan::getScheduleStatus, TruckPlanScheduleStatusEnum.UNASSIGNED.getCode()));
    }

    @Override
    public List<String> findTruckDriverLicensePlateList(String transportTripsId) {
        LambdaQueryWrapper<TruckTripsTruckDriver> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TruckTripsTruckDriver::getTransportTripsId,transportTripsId);
        wrapper.select(TruckTripsTruckDriver::getTruckId);
        List<TruckTripsTruckDriver> list = this.list(wrapper);

        return  list.stream().map(truckTripsTruckDriver -> {
            Long truckId = truckTripsTruckDriver.getTruckId();
            LambdaQueryWrapper<Truck> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Truck::getId,truckId);
            queryWrapper.select(Truck::getLicensePlate);
            return truckService.getOne(queryWrapper).getLicensePlate();
        })
                .distinct() // 使用distinct去重
                .collect(Collectors.toList());
    }
}




