package com.dreams.logistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreams.logistics.common.ErrorCode;
import com.dreams.logistics.exception.ThrowUtils;
import com.dreams.logistics.model.entity.Organization;

import com.dreams.logistics.service.OrganizationService;
import com.dreams.logistics.mapper.OrganizationMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

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

    @Override
    public List<Organization> listWithTree() {
        //1、查出所有分类
        List<Organization> entities = baseMapper.selectList(null);

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

}




