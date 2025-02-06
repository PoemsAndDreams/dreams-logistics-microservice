package com.dreams.logistics.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreams.logistics.model.entity.TruckType;
import com.dreams.logistics.service.TruckTypeService;
import com.dreams.logistics.mapper.TruckTypeMapper;
import org.springframework.stereotype.Service;

/**
* @author xiayutian
* @description 针对表【truck_type(车辆类型表)】的数据库操作Service实现
* @createDate 2025-02-04 08:47:38
*/
@Service
public class TruckTypeServiceImpl extends ServiceImpl<TruckTypeMapper, TruckType>
    implements TruckTypeService{

}




