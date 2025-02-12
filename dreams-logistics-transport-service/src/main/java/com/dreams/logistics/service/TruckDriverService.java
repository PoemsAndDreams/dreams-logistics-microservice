package com.dreams.logistics.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dreams.logistics.model.entity.DcUser;
import com.dreams.logistics.model.entity.TruckDriver;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author xiayutian
* @description 针对表【truck_driver(车辆和司机关联表)】的数据库操作Service
* @createDate 2025-02-04 08:47:38
*/
public interface TruckDriverService extends IService<TruckDriver> {

    List<DcUser> getCarAllDriver(long id);

    Boolean deleteDriver(long id, long driverId);

    Boolean addDriver(long id, long driverId);

    List<TruckDriver> findAll(List<Long> userIds);

    TruckDriver findOne(Long userId);

    List<TruckDriver> findByTruckId(Long truckId);

    void disableTruckId(Long truckId);

}
