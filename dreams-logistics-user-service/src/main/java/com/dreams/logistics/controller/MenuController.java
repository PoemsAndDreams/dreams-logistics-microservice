package com.dreams.logistics.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreams.logistics.common.BaseResponse;
import com.dreams.logistics.common.DeleteRequest;
import com.dreams.logistics.common.ErrorCode;
import com.dreams.logistics.common.ResultUtils;
import com.dreams.logistics.exception.BusinessException;
import com.dreams.logistics.exception.ThrowUtils;
import com.dreams.logistics.model.dto.menu.MenuAddRequest;
import com.dreams.logistics.model.dto.menu.MenuQueryRequest;
import com.dreams.logistics.model.dto.menu.MenuUpdateRequest;
import com.dreams.logistics.model.entity.*;
import com.dreams.logistics.model.entity.Menu;
import com.dreams.logistics.model.vo.Route;
import com.dreams.logistics.service.*;
import com.dreams.logistics.utils.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;

/**
 * @author PoemsAndDreams
 * @date 2025-01-20 11:47
 * @description //TODO
 */
@RestController
@RequestMapping("/menu")
@Slf4j
public class MenuController {
    
    @Resource
    private MenuService menuService;

    @Resource
    private RoleService roleService;

    @Resource
    private RoleMenuService roleMenuService;

    @Resource
    private UserRoleService userRoleService;


    /**
     * 查出所有分类以及子分类,以树形结构组装起来
     */
    @GetMapping("/list/tree")
    public BaseResponse<List<Menu>> listMenuList(){
        List<Menu> entities = menuService.listWithTree();
        return ResultUtils.success(entities);
    }
    /**
     * 创建用户
     *
     * @param menuAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<String> addMenu(@RequestBody MenuAddRequest menuAddRequest, HttpServletRequest request) {
        if (menuAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Menu menu = new Menu();
        if (Objects.isNull(menuAddRequest.getParentId())){
            menuAddRequest.setParentId("0");
        }
        BeanUtils.copyProperties(menuAddRequest, menu);
        boolean result = menuService.save(menu);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(menu.getId());
    }

    /**
     * 删除用户
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteMenu(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long deleteRequestId = deleteRequest.getId();
        Boolean bool = menuService.deleteMenu(deleteRequestId);

        return ResultUtils.success(bool);
    }

    /**
     * 更新用户
     *
     * @param menuUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateMenu(@RequestBody MenuUpdateRequest menuUpdateRequest,
                                            HttpServletRequest request) {
        if (menuUpdateRequest == null || menuUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Menu menu = menuService.getById(menuUpdateRequest.getId());
        BeanUtils.copyProperties(menuUpdateRequest, menu);

        boolean result = menuService.updateById(menu);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取用户
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Menu> getMenuById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Menu menu = menuService.getById(id);
        ThrowUtils.throwIf(menu == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(menu);
    }

//    /**
//     * 根据 id 获取包装类
//     *
//     * @param id
//     * @param request
//     * @return
//     */
//    @GetMapping("/get/vo")
//    public BaseResponse<MenuVO> getMenuVOById(long id, HttpServletRequest request) {
//        BaseResponse<Menu> response = getMenuById(id, request);
//        Menu menu = response.getData();
//        return ResultUtils.success(menuService.getMenuVO(menu));
//    }

    /**
     * 分页获取用户列表
     *
     * @param menuQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<Menu>> listMenuByPage(@RequestBody MenuQueryRequest menuQueryRequest,
                                                   HttpServletRequest request) {
        long current = menuQueryRequest.getCurrent();
        long size = menuQueryRequest.getPageSize();
        Page<Menu> menuPage = menuService.page(new Page<>(current, size));
        return ResultUtils.success(menuPage);
    }

    /**
     * 获取所有用户信息并返回 AllMenuVO 列表
     *
     * @param request HttpServletRequest 请求对象
     * @return List<AllMenuVO> 所有用户的 VO 列表
     */
    @GetMapping("/get/all")
    public BaseResponse<List<Menu>> getAllMenu(HttpServletRequest request) {
        // 获取所有用户实体列表
        List<Menu> list = menuService.list();
        return ResultUtils.success(list);
    }



    /**
     * 获取所有菜单信息并返回 RoleMenuVO 列表
     *
     * @param request HttpServletRequest 请求对象
     * @return List<Role> 所有用户的 VO 列表
     */
    @GetMapping("/get/user/menu")
    public BaseResponse<List<Route>> getUserMenu(HttpServletRequest request) {

        // 获取所有用户实体列表
        DcUser user = SecurityUtil.getUser();
        Long userId = user.getId();

        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId,userId);
        UserRole userRole = userRoleService.getOne(wrapper);
        String roleId = userRole.getRoleId();

        List<Route> CurrentUserRoute =  menuService.getCurrentUserRoute(roleId);
        return ResultUtils.success(CurrentUserRoute);
    }
}
