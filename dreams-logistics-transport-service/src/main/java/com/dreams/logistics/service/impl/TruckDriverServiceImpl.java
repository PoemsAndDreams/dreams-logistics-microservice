package com.dreams.logistics.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreams.logistics.entity.line.TransportLine;
import com.dreams.logistics.exception.BusinessException;
import com.dreams.logistics.model.entity.DcUser;
import com.dreams.logistics.model.entity.TruckDriver;
import com.dreams.logistics.model.entity.TruckTrips;
import com.dreams.logistics.model.entity.TruckTripsTruckDriver;
import com.dreams.logistics.service.*;
import com.dreams.logistics.mapper.TruckDriverMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author xiayutian
* @description 针对表【truck_driver(车辆和司机关联表)】的数据库操作Service实现
* @createDate 2025-02-04 08:47:38
*/
@Service
public class TruckDriverServiceImpl extends ServiceImpl<TruckDriverMapper, TruckDriver>
    implements TruckDriverService{
    @Resource
    private TruckTripsTruckDriverService truckTripsTruckDriverService;

    @Resource
    private TransportLineService transportLineService;

    @Resource
    private TruckTripsService truckTripsService;
    @Resource
    private UserFeignClient userFeignClient;

    @Override
    public List<DcUser> getCarAllDriver(long id) {
        LambdaQueryWrapper<TruckDriver> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TruckDriver::getTruckId,id);
        List<TruckDriver> list = this.list(wrapper);
        List<DcUser> collect = list.stream().map(truckDriver -> {
            Long driverId = truckDriver.getDriverId();
            return userFeignClient.getById(driverId);
        }).collect(Collectors.toList());
        return collect;
    }

    @Override
    public Boolean deleteDriver(long id, long driverId) {
        LambdaQueryWrapper<TruckDriver> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TruckDriver::getTruckId,id);
        wrapper.eq(TruckDriver::getDriverId,driverId);
        return this.remove(wrapper);
    }

    @Override
    public Boolean addDriver(long id, long driverId) {
        TruckDriver truckDriver = new TruckDriver();
        truckDriver.setTruckId(id);
        truckDriver.setDriverId(driverId);
        TruckDriver one = findOne(driverId);
        if (ObjectUtil.isNotEmpty(one)) {
            // 检查是否能够解除原有绑定车辆
            checkCanBingingTruck(one);
            truckDriver.setId(one.getId());
            updateById(truckDriver);
        }

        return this.save(truckDriver);
    }


    /**
     * 获取司机基本信息列表
     *
     * @param userIds 司机id列表
     * @return 司机基本信息列表
     */
    @Override
    public List<TruckDriver> findAll(List<Long> userIds) {
        if (ObjectUtil.isAllEmpty(userIds)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<TruckDriver> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (CollUtil.isNotEmpty(userIds)) {
            lambdaQueryWrapper.in(TruckDriver::getDriverId, userIds);
        }
        lambdaQueryWrapper.orderByDesc(TruckDriver::getCreated);
        return super.list(lambdaQueryWrapper);
    }

    /**
     * 获取司机基本信息
     *
     * @param userId 司机id
     * @return 司机基本信息
     */
    @Override
    public TruckDriver findOne(Long userId) {
        LambdaQueryWrapper<TruckDriver> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TruckDriver::getDriverId, userId);
        return super.getOne(lambdaQueryWrapper);
    }

    /**
     * 绑定司机列表
     *

     * @return 司机数量
     */
    @Override
    public List<TruckDriver> findByTruckId(Long truckId) {
        LambdaQueryWrapper<TruckDriver> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TruckDriver::getTruckId,truckId);
        return list(lambdaQueryWrapper);
    }

    /**
     * 解除车辆和司机的绑定关系
     * @param truckId 车辆ID
     */
    @Override
    public void disableTruckId(Long truckId) {
        LambdaUpdateWrapper<TruckDriver> lambdaQueryWrapper = new LambdaUpdateWrapper<>();
        lambdaQueryWrapper.eq(TruckDriver::getTruckId,truckId)
                .set(TruckDriver::getTruckId, null);
        update(lambdaQueryWrapper);

    }

    /**
     * check 是否有车次绑定关系
     * @param oldDriver 旧数据
     */
    private void checkCanBingingTruck(TruckDriver oldDriver) {
        // 如果原有绑定车辆不为空 则需要判断是否能够解除原有车辆绑定关系
        if (ObjectUtil.isNotEmpty(oldDriver.getTruckId())) {
            // 检查车次绑定关系
            List<TruckTripsTruckDriver> all = truckTripsTruckDriverService.findAll(null, oldDriver.getTruckId(), oldDriver.getDriverId());
            if (all.size() > 0) {
                // 不能解除
                List<Long> tripsIds = all.parallelStream().map(TruckTripsTruckDriver::getTransportTripsId).distinct().collect(Collectors.toList());
                List<TruckTrips> truckTripsEntities = truckTripsService.findAll(null, tripsIds);
                List<String> tripsNames = truckTripsEntities.parallelStream().map(TruckTrips::getName).distinct().collect(Collectors.toList());
                List<Long> lineIds = truckTripsEntities.parallelStream().map(TruckTrips::getTransportLineId).distinct().collect(Collectors.toList());
                List<TransportLine> lineDTOS = transportLineService.queryByIds(lineIds.toArray(new Long[0]));
                List<String> names = lineDTOS.parallelStream().map(TransportLine::getName).collect(Collectors.toList());
                throw new BusinessException(StrUtil.format("请先解除原有车辆对应的车次绑定关系 详细: 线路名称 {} 车次名称{}", names, tripsNames));
            }
        }
    }

}




