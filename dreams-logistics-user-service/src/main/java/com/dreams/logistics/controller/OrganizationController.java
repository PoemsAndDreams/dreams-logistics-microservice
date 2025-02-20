package com.dreams.logistics.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreams.logistics.common.BaseResponse;
import com.dreams.logistics.common.DeleteRequest;
import com.dreams.logistics.common.ErrorCode;
import com.dreams.logistics.common.ResultUtils;
import com.dreams.logistics.exception.BusinessException;
import com.dreams.logistics.exception.ThrowUtils;
import com.dreams.logistics.model.dto.organization.OrganizationAddRequest;
import com.dreams.logistics.model.dto.organization.OrganizationQueryRequest;
import com.dreams.logistics.model.dto.organization.OrganizationUpdateRequest;
import com.dreams.logistics.model.entity.Organization;
import com.dreams.logistics.service.OrganizationService;
import com.dreams.logistics.service.UserRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author PoemsAndDreams
 * @date 2025-01-20 11:47
 * @description //TODO
 */
@RestController
@RequestMapping("/organization")
@Slf4j
public class OrganizationController {
    
    @Resource
    private OrganizationService organizationService;
    
    @Resource
    private UserRoleService userRoleService;


    /**
     * 查出所有分类以及子分类,以树形结构组装起来
     */
    @GetMapping("/list/tree")
    public BaseResponse<List<Organization>> listOrganizationList(){
        List<Organization> entities = organizationService.listWithTree();
        return ResultUtils.success(entities);
    }
    /**
     * 创建机构
     *
     * @param organizationAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<String> addOrganization(@RequestBody OrganizationAddRequest organizationAddRequest, HttpServletRequest request) {
        if (organizationAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Organization organization = organizationService.saveOrg(organizationAddRequest);
        return ResultUtils.success(organization.getId().toString());
    }

    /**
     * 删除机构
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteOrganization(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long deleteRequestId = deleteRequest.getId();
        Boolean bool = organizationService.deleteOrganization(deleteRequestId);

        return ResultUtils.success(bool);
    }

    /**
     * 更新机构
     *
     * @param organizationUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateOrganization(@RequestBody OrganizationUpdateRequest organizationUpdateRequest,
                                            HttpServletRequest request) {
        if (organizationUpdateRequest == null || organizationUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        boolean result = organizationService.updateOrg(organizationUpdateRequest);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取机构
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Organization> getOrganizationById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Organization organization = organizationService.getById(id);
        ThrowUtils.throwIf(organization == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(organization);
    }

    /**
     * 分页获取机构列表
     *
     * @param organizationQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<Organization>> listOrganizationByPage(@RequestBody OrganizationQueryRequest organizationQueryRequest,
                                                   HttpServletRequest request) {
        long current = organizationQueryRequest.getCurrent();
        long size = organizationQueryRequest.getPageSize();
        Page<Organization> organizationPage = organizationService.page(new Page<>(current, size));
        return ResultUtils.success(organizationPage);
    }

    /**
     * 获取所有机构信息并返回 AllOrganizationVO 列表
     *
     * @param request HttpServletRequest 请求对象
     * @return List<AllOrganizationVO> 所有机构的 VO 列表
     */
    @GetMapping("/get/all")
    public BaseResponse<List<Organization>> getAllOrganization(HttpServletRequest request) {
        // 获取所有机构实体列表
        List<Organization> list = organizationService.list();
        return ResultUtils.success(list);
    }


    
}
