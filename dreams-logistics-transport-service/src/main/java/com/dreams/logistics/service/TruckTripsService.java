package com.dreams.logistics.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.dreams.logistics.model.dto.truckPlan.OrganIdsDto;
import com.dreams.logistics.model.dto.truckTrips.TruckTripsAddRequest;
import com.dreams.logistics.model.dto.truckTrips.TruckTripsQueryRequest;
import com.dreams.logistics.model.dto.truckTrips.TruckTripsUpdateRequest;
import com.dreams.logistics.model.entity.TruckTrips;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dreams.logistics.model.vo.TruckTripsVO;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
* @author xiayutian
* @description 针对表【truck_trips(车次信息表)】的数据库操作Service
* @createDate 2025-02-05 15:19:37
*/
public interface TruckTripsService extends IService<TruckTrips> {

    List<TruckTrips> findAll(Long transportLineId, List<Long> ids);

    void disable(Long id);

    Map<Long, OrganIdsDto> getOrganIdsByTripsLineId(HashSet<Long> values);

    boolean saveTruckTrips(TruckTripsAddRequest truckTripsAddRequest);

    boolean updateTruckTrips(TruckTripsUpdateRequest truckTripsUpdateRequest);

    TruckTripsVO getTruckTrips(long id);

    Wrapper<TruckTrips> getQueryWrapper(TruckTripsQueryRequest truckTripsQueryRequest);

    List<TruckTripsVO> getTruckTripsVO(List<TruckTrips> records);

    TruckTripsVO getTruckTripsVO(TruckTrips truckTrips);
}
