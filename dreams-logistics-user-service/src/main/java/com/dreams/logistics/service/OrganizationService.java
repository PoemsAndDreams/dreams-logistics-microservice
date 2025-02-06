package com.dreams.logistics.service;

import com.dreams.logistics.model.entity.Organization;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author xiayutian
* @description 针对表【organization(组织)】的数据库操作Service
* @createDate 2025-01-23 09:52:50
*/
public interface OrganizationService extends IService<Organization> {

    List<Organization> listWithTree();

    List<Organization> getChildrens(Organization root, List<Organization> all);

    Boolean deleteOrganization(Long deleteRequestId);

    void removeOrganizationByIds(List<Long> asList);

    Long[] findOrganizationPath(String organizationId);

    List<String> findParentPath(String organizationId, List<String> paths);


}
