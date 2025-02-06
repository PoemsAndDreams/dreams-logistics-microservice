package com.dreams.logistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreams.logistics.model.entity.DcUser;
import com.dreams.logistics.model.entity.TruckDriver;
import com.dreams.logistics.service.TruckDriverService;
import com.dreams.logistics.mapper.TruckDriverMapper;
import com.dreams.logistics.service.UserFeignClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
        return this.save(truckDriver);
    }
}




