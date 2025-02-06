package com.dreams.logistics.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.dreams.logistics.model.dto.area.AreaQueryRequest;
import com.dreams.logistics.model.entity.Area;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dreams.logistics.model.vo.AreaVO;

import java.util.List;

/**
* @author xiayutian
* @description 针对表【area】的数据库操作Service
* @createDate 2025-01-24 01:47:45
*/
public interface AreaService extends IService<Area> {

    Wrapper<Area> getQueryWrapper(AreaQueryRequest areaQueryRequest);


    Area getProvinceId(Long receiverCityId);

    List<Area> listWithTree();

    List<Area> getChildrens(Area root, List<Area> all);

    List<AreaVO> findChildren(Long parentId);

    Area findById(Long id);
}
