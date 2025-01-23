package com.dreams.logistics.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.dreams.logistics.model.dto.carriage.CarriageQueryRequest;
import com.dreams.logistics.model.dto.carriage.WaybillDTO;
import com.dreams.logistics.model.entity.Carriage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dreams.logistics.model.vo.CarriageVO;

/**
* @author xiayutian
* @description 针对表【carriage(运费模板表)】的数据库操作Service
* @createDate 2025-01-23 14:42:15
*/
public interface CarriageService extends IService<Carriage> {

    Wrapper<Carriage> getQueryWrapper(CarriageQueryRequest carriageQueryRequest);


    boolean saveOrUpdateCarriage(Carriage carriage);

    CarriageVO compute(WaybillDTO waybillDTO);


    Carriage findByTemplateType(Integer templateType);
}
