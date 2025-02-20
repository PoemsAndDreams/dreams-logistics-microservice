package com.dreams.logistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreams.logistics.common.ErrorCode;
import com.dreams.logistics.exception.BusinessException;
import com.dreams.logistics.exception.ThrowUtils;
import com.dreams.logistics.model.dto.organization.OrganizationAddRequest;
import com.dreams.logistics.model.dto.organization.OrganizationUpdateRequest;
import com.dreams.logistics.model.entity.Organization;

import com.dreams.logistics.service.OrganizationService;
import com.dreams.logistics.mapper.OrganizationMapper;
import com.dreams.logistics.service.TransportFeignClient;
import com.dreams.logistics.utils.BaiduMap;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author xiayutian
* @description 针对表【organization(组织)】的数据库操作Service实现
* @createDate 2025-01-23 09:52:50
*/
@Service
public class OrganizationServiceImpl extends ServiceImpl<OrganizationMapper, Organization>
    implements OrganizationService{

    @Resource
    private BaiduMap baiduMap;


    @Resource
    private TransportFeignClient transportFeignClient;

    @Override
    public List<Organization> listWithTree() {
        //1、查出所有
        List<Organization> list = baseMapper.selectList(null);

        List<Organization> entities = list.stream().map(org -> {
            org.setAddressId(Arrays.asList(org.getProvinceId(), org.getCityId(), org.getCountyId()));
            return org;
        }).collect(Collectors.toList());


        //2、组装成父子的树形结构
        // 2.1）、找到所有的一级分类
        List<Organization> levelOrganizations = entities.stream().filter(organizationTree ->
                Objects.equals(organizationTree.getParentId(), "0")
        ).map((organization) -> {
            organization.setChildren(getChildrens(organization,entities));
            return organization;
        }).sorted(Comparator.comparingInt(organization -> (organization.getSortValue() == null ? 0 : organization.getSortValue()))).collect(Collectors.toList());

        return levelOrganizations;
    }




    @Override
    public List<Organization> getChildrens(Organization root, List<Organization> all){
        List<Organization> children = all.stream().filter(organizationTree -> {
            return Objects.equals(organizationTree.getParentId(), root.getId());
        }).map(organizationTree -> {
            //1、找到子菜单
            organizationTree.setChildren(getChildrens(organizationTree, all));
            return organizationTree;
        }).sorted(Comparator.comparingInt(organization -> (organization.getSortValue() == null ? 0 : organization.getSortValue()))).collect(Collectors.toList());
        return children;
    }


    @Override
    public Boolean deleteOrganization(Long deleteRequestId) {

        LambdaQueryWrapper<Organization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Organization::getParentId,deleteRequestId);
        List<Organization> list = baseMapper.selectList(wrapper);
        ThrowUtils.throwIf(!list.isEmpty(), ErrorCode.SUBMENU_NOT_NULL_ERROR);

        int delete = baseMapper.deleteById(deleteRequestId);
        return delete == 1;
    }


    @Override
    public void removeOrganizationByIds(List<Long> asList) {
        //TODO 1、检查当前删除的菜单,是否被别的地方引用
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findOrganizationPath(String organizationId) {
        List<String> paths = new ArrayList<>();
        List<String> parentPath = findParentPath(organizationId, paths);
        Collections.reverse(parentPath);

        return parentPath.toArray(new Long[parentPath.size()]);
    }
    @Override
    public List<String> findParentPath(String organizationId, List<String> paths){
        paths.add(organizationId);
        Organization byId = this.getById(organizationId);
        if (!Objects.equals(byId.getParentId(), "0")){
            findParentPath(byId.getParentId(),paths);
        }
        return paths;
    }

    @Override
    public Organization saveOrg(OrganizationAddRequest organizationAddRequest) {
        Organization organization = new Organization();
        if (Objects.isNull(organizationAddRequest.getParentId())){
            organizationAddRequest.setParentId("0");
        }
        BeanUtils.copyProperties(organizationAddRequest, organization);
        List<Long> addressId = organizationAddRequest.getAddressId();
        // 确保 addressId 不为空
        if (addressId == null || addressId.isEmpty()) {
            throw new BusinessException("地址 ID 列表不能为空");
        }

        // 获取省、市、县的 ID（根据 addressId 列表的大小来动态处理）
        Long provinceId = addressId.get(0);
        Long cityId = addressId.size() > 1 ? addressId.get(1) : null;
        Long countyId = addressId.size() > 2 ? addressId.get(2) : null;

        // 设置 ID 到 organization
        organization.setProvinceId(provinceId);
        if (cityId != null) {
            organization.setCityId(cityId);
        }
        if (countyId != null) {
            organization.setCountyId(countyId);
        }

        // 获取省、市、县名称，并处理可能的 null 返回值
        String province = transportFeignClient.findAreaById(provinceId.toString()).getName();
        String city = cityId != null ? transportFeignClient.findAreaById(cityId.toString()).getName() : null;
        String county = countyId != null ? transportFeignClient.findAreaById(countyId.toString()).getName() : null;

        // 如果获取到的名称为空，抛出异常或设置默认值
        if (province == null) {
            throw new BusinessException("无法获取省名称");
        }
        if (cityId != null && city == null) {
            throw new BusinessException("无法获取市名称");
        }
        if (countyId != null && county == null) {
            throw new BusinessException("无法获取县名称");
        }

        // 构建完整的地址字符串
        String fullAddress = province + (city != null ? city : "") + (county != null ? county : "");

        // 调用百度地图 geocoding 返回经纬度
        List<Double> list = baiduMap.geocodingReturn(fullAddress);
        // 提取经度和纬度
        double lng = list.get(0);
        double lat =  list.get(1);

        organization.setLongitude(String.valueOf(lng));
        organization.setLatitude(String.valueOf(lat));

        boolean result = this.save(organization);
        return organization;
    }

    @Override
    public boolean updateOrg(OrganizationUpdateRequest organizationUpdateRequest) {
        Organization organization = this.getById(organizationUpdateRequest.getId());
        BeanUtils.copyProperties(organizationUpdateRequest, organization);
        List<Long> addressId = organizationUpdateRequest.getAddressId();
        
        // 确保 addressId 不为空
        if (addressId == null || addressId.isEmpty()) {
            throw new BusinessException("地址 ID 列表不能为空");
        }

        // 获取省、市、县的 ID（根据 addressId 列表的大小来动态处理）
        Long provinceId = addressId.get(0);
        Long cityId = addressId.size() > 1 ? addressId.get(1) : null;
        Long countyId = addressId.size() > 2 ? addressId.get(2) : null;

        // 设置 ID 到 organization
        organization.setProvinceId(provinceId);
        if (cityId != null) {
            organization.setCityId(cityId);
        }
        if (countyId != null) {
            organization.setCountyId(countyId);
        }

        // 获取省、市、县名称，并处理可能的 null 返回值
        String province = transportFeignClient.findAreaById(provinceId.toString()).getName();
        String city = cityId != null ? transportFeignClient.findAreaById(cityId.toString()).getName() : null;
        String county = countyId != null ? transportFeignClient.findAreaById(countyId.toString()).getName() : null;

        // 如果获取到的名称为空，抛出异常或设置默认值
        if (province == null) {
            throw new BusinessException("无法获取省名称");
        }
        if (cityId != null && city == null) {
            throw new BusinessException("无法获取市名称");
        }
        if (countyId != null && county == null) {
            throw new BusinessException("无法获取县名称");
        }

        // 构建完整的地址字符串
        String fullAddress = province + (city != null ? city : "") + (county != null ? county : "");

        // 调用百度地图 geocoding 返回经纬度
        List<Double> list = baiduMap.geocodingReturn(fullAddress);
        
        
        // 提取经度和纬度
        double lng = list.get(0);
        double lat =  list.get(1);

        organization.setLongitude(String.valueOf(lng));
        organization.setLatitude(String.valueOf(lat));
        return this.updateById(organization);
    }

}




