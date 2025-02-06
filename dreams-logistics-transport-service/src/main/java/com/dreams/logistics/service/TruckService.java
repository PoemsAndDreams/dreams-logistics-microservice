package com.dreams.logistics.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.dreams.logistics.model.dto.truck.TruckQueryRequest;
import com.dreams.logistics.model.entity.Truck;
import com.dreams.logistics.model.entity.Truck;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dreams.logistics.model.vo.TruckVO;

import java.util.List;

/**
* @author xiayutian
* @description 针对表【truck(车辆信息表)】的数据库操作Service
* @createDate 2025-02-04 08:47:38
*/
public interface TruckService extends IService<Truck> {
    Wrapper<Truck> getQueryWrapper(TruckQueryRequest truckQueryRequest);

    TruckVO getTruckVO(Truck truck);

    List<TruckVO> getTruckVO(List<Truck> records);

}
