package com.dreams.logistics.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreams.logistics.common.BaseResponse;
import com.dreams.logistics.common.DeleteRequest;
import com.dreams.logistics.common.ErrorCode;
import com.dreams.logistics.common.ResultUtils;
import com.dreams.logistics.exception.BusinessException;
import com.dreams.logistics.exception.ThrowUtils;
import com.dreams.logistics.model.dto.role.RoleAddRequest;
import com.dreams.logistics.model.dto.role.RoleQueryRequest;
import com.dreams.logistics.model.dto.role.RoleUpdateRequest;
import com.dreams.logistics.model.dto.roleMenu.RoleMenuBatchAddRequest;
import com.dreams.logistics.model.entity.Menu;
import com.dreams.logistics.model.entity.Role;
import com.dreams.logistics.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 角色接口
 */
@RestController
@RequestMapping("/role")
@Slf4j
public class RoleController {


    @Resource
    private UserService userService;

    @Resource
    private MenuService menuService;

    @Resource
    private RoleService roleService;

    @Resource
    private UserRoleService userRoleService;

    @Resource
    private RoleMenuService roleMenuService;
    
    // endregion

    // region 增删改查

    /**
     * 创建角色
     *
     * @param roleAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<String> addRole(@RequestBody RoleAddRequest roleAddRequest, HttpServletRequest request) {
        if (roleAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Role role = new Role();
        BeanUtils.copyProperties(roleAddRequest, role);


        boolean result = roleService.save(role);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(role.getId());
    }

    /**
     * 删除角色
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteRole(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = roleService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新角色
     *
     * @param roleUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateRole(@RequestBody RoleUpdateRequest roleUpdateRequest,
            HttpServletRequest request) {
        if (roleUpdateRequest == null || roleUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Role role = roleService.getById(roleUpdateRequest.getId());
        BeanUtils.copyProperties(roleUpdateRequest, role);

        boolean result = roleService.updateById(role);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取角色
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Role> getRoleById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Role role = roleService.getById(id);
        ThrowUtils.throwIf(role == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(role);
    }


    /**
     * 分页获取角色列表
     *
     * @param roleQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<Role>> listRoleByPage(@RequestBody RoleQueryRequest roleQueryRequest,
                                                     HttpServletRequest request) {
        long current = roleQueryRequest.getCurrent();
        long size = roleQueryRequest.getPageSize();
        Page<Role> rolePage = roleService.page(new Page<>(current, size));
        return ResultUtils.success(rolePage);
    }

    /**
     * 获取所有角色信息并返回
     *
     * @param request HttpServletRequest 请求对象
     * @return List<AllRoleVO> 所有角色信息列表
     */
    @GetMapping("/get/all")
    public BaseResponse<List<Role>> getAllRole(HttpServletRequest request) {
        // 获取所有角色实体列表
        List<Role> list = roleService.list();
        return ResultUtils.success(list);
    }


    /**
     * 根据 id 获取角色菜单
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get/menu")
    public BaseResponse<List<Menu>> getRoleMenuById(String id, HttpServletRequest request) {


        List<Menu> currentUserMenu = menuService.getCurrentUserMenu(id);

        return ResultUtils.success(currentUserMenu);
    }


    @PostMapping("/add/batch")
    public BaseResponse<Boolean> batchAddRoleMenuToBank(
            @RequestBody RoleMenuBatchAddRequest roleMenuBatchAddRequest,
            HttpServletRequest request
    ) {
        // 参数校验
        ThrowUtils.throwIf(roleMenuBatchAddRequest == null, ErrorCode.PARAMS_ERROR);


        String roleId = roleMenuBatchAddRequest.getRoleId();
        List<String> roleMenuIds = roleMenuBatchAddRequest.getRoleMenuIds();


        Boolean bool = roleMenuService.batchAddRoleMenuToBank(roleId,roleMenuIds);

        return ResultUtils.success(bool);
    }


}
