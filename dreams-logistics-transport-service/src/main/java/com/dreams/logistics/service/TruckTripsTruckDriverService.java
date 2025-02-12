package com.dreams.logistics.service;

import com.dreams.logistics.model.dto.truckPlan.TransportTripsTruckDriverDto;
import com.dreams.logistics.model.entity.TruckTripsTruckDriver;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Collection;
import java.util.List;

/**
* @author xiayutian
* @description 针对表【truck_trips_truck_driver(车次与车辆和司机关联表)】的数据库操作Service
* @createDate 2025-02-05 15:19:37
*/
public interface TruckTripsTruckDriverService extends IService<TruckTripsTruckDriver> {


    @Transactional
    void batchSave(Long transportTripsId,List<String> licensePlateList);

    List<TruckTripsTruckDriver> findAll(Long transportTripsId, Long truckId, Long userId);

    Boolean canRemove(Long transportTripsId, Long truckId, Long userId);

    @Transactional
    void delete(Long transportTripsId, Long truckId);

    List<String> findTruckDriverLicensePlateList(String transportTripsId);
}
