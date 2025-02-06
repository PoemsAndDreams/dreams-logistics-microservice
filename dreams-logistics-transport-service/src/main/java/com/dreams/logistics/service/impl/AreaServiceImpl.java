package com.dreams.logistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreams.logistics.common.ErrorCode;
import com.dreams.logistics.constant.CommonConstant;
import com.dreams.logistics.exception.BusinessException;
import com.dreams.logistics.model.dto.area.AreaQueryRequest;
import com.dreams.logistics.model.entity.Area;
import com.dreams.logistics.model.vo.AreaVO;
import com.dreams.logistics.service.AreaService;
import com.dreams.logistics.mapper.AreaMapper;
import com.dreams.logistics.utils.SqlUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author xiayutian
* @description 针对表【area】的数据库操作Service实现
* @createDate 2025-01-24 01:47:45
*/
@Service
public class AreaServiceImpl extends ServiceImpl<AreaMapper, Area>
    implements AreaService{
    @Override
    public Wrapper<Area> getQueryWrapper(AreaQueryRequest areaQueryRequest) {

        if (areaQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        Integer parentId = areaQueryRequest.getParentId();
        String name = areaQueryRequest.getName();
        String areaCode = areaQueryRequest.getAreaCode();
        String cityCode = areaQueryRequest.getCityCode();
        String mergerName = areaQueryRequest.getMergerName();
        String shortName = areaQueryRequest.getShortName();
        String zipCode = areaQueryRequest.getZipCode();
        Integer level = areaQueryRequest.getLevel();
        String lng = areaQueryRequest.getLng();
        String lat = areaQueryRequest.getLat();
        String pinyin = areaQueryRequest.getPinyin();
        String first = areaQueryRequest.getFirst();

        String sortField = areaQueryRequest.getSortField();
        String sortOrder = areaQueryRequest.getSortOrder();

        QueryWrapper<Area> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(parentId != null,"parent_id", parentId);
        queryWrapper.like(StringUtils.isNotEmpty(name),"name", name);
        queryWrapper.like(StringUtils.isNotEmpty(areaCode),"area_code", areaCode);
        queryWrapper.like(StringUtils.isNotEmpty(cityCode),"city_code", cityCode);
        queryWrapper.like(StringUtils.isNotEmpty(mergerName),"merger_name", mergerName);
        queryWrapper.like(StringUtils.isNotEmpty(shortName),"short_name", shortName);
        queryWrapper.like(StringUtils.isNotEmpty(zipCode),"zip_code", zipCode);
        queryWrapper.like(StringUtils.isNotEmpty(lng),"lng", lng);
        queryWrapper.like(StringUtils.isNotEmpty(lat),"lat", lat);
        queryWrapper.like(StringUtils.isNotEmpty(pinyin),"pinyin", pinyin);
        queryWrapper.like(StringUtils.isNotEmpty(first),"first", first);
        queryWrapper.like(level != null,"level", level);


        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;

    }

    @Override
    public Area getProvinceId(Long receiverCityId) {
        LambdaQueryWrapper<Area> areaLambdaQueryWrapper = new LambdaQueryWrapper<>();
        areaLambdaQueryWrapper.eq(Area::getParentId,receiverCityId);
        return this.getOne(areaLambdaQueryWrapper);
    }

    @Override
    public List<Area> listWithTree() {
        //1、查出所有分类
        List<Area> entities = baseMapper.selectList(null);

        //2、组装成父子的树形结构
        // 2.1）、找到所有的一级分类
        List<Area> levelAreas = entities.stream().filter(areaTree ->
                Objects.equals(areaTree.getParentId(), 0)
        ).map((area) -> {
            area.setChildren(getChildrens(area,entities));
            return area;
        }).collect(Collectors.toList());

        return levelAreas;
    }


    @Override
    public List<Area> getChildrens(Area root, List<Area> all){
        List<Area> children = all.stream().filter(areaTree -> {
            return Objects.equals(areaTree.getParentId(), root.getId());
        }).map(areaTree -> {
            //1、找到子菜单
            areaTree.setChildren(getChildrens(areaTree, all));
            return areaTree;
        }).collect(Collectors.toList());
        return children;
    }

    @Override
    public List<AreaVO> findChildren(Long parentId) {
        // 只查询 id 和 name 字段，减少数据量
        List<Area> list = this.baseMapper.selectList(new LambdaQueryWrapper<Area>()
                .eq(Area::getParentId, parentId)
                        // 只查出县级
                        .le(Area::getLevel,2)
                .select(Area::getId,Area::getName,Area::getLevel, Area::getName));  // 只查询 id 和 name

        if (list.isEmpty()){
            return null;
        }

        List<AreaVO> areaVOS = list.stream()
                .map(area -> {
                    AreaVO areaVO = new AreaVO();
                    areaVO.setId(area.getId());
                    areaVO.setName(area.getName());
                    areaVO.setIsLeaf(false);
                    areaVO.setLevel(area.getLevel());
                    return areaVO;
                })
                .collect(Collectors.toList());

        return areaVOS;
    }

    @Override
    public Area findById(Long id) {
        LambdaQueryWrapper<Area> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Area::getId,id);
        return this.getOne(wrapper);
    }

}




