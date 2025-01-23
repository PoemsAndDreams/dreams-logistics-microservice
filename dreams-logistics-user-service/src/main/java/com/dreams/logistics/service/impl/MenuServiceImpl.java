package com.dreams.logistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreams.logistics.common.ErrorCode;
import com.dreams.logistics.exception.ThrowUtils;
import com.dreams.logistics.model.entity.Menu;
import com.dreams.logistics.model.entity.RoleMenu;
import com.dreams.logistics.model.vo.Route;
import com.dreams.logistics.service.*;
import com.dreams.logistics.mapper.MenuMapper;
import org.springframework.stereotype.Service;


import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author xiayutian
* @description 针对表【menu】的数据库操作Service实现
* @createDate 2025-01-20 11:44:00
*/
@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu>
    implements MenuService{

    @Resource
    private UserRoleService userRoleService;

    @Resource
    private RoleMenuService roleMenuService;

    @Override
    public List<Menu> listWithTree() {
        //1、查出所有分类
        List<Menu> entities = baseMapper.selectList(null);

        //2、组装成父子的树形结构
        // 2.1）、找到所有的一级分类
        List<Menu> levelMenus = entities.stream().filter(menuTree ->
                Objects.equals(menuTree.getParentId(), "0")
        ).map((menu) -> {
            menu.setChildren(getChildrens(menu,entities));
            return menu;
        }).sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort()))).collect(Collectors.toList());

        return levelMenus;
    }


    @Override
    public List<Menu> getCurrentUserMenu(String roleId) {

        List<Menu> list = baseMapper.selectList(null);

        LambdaQueryWrapper<RoleMenu> roleMenuWrapper = new LambdaQueryWrapper<>();
        roleMenuWrapper.eq(RoleMenu::getRoleId,roleId);
        List<RoleMenu> roleMenus = roleMenuService.list(roleMenuWrapper);

        List<String> roleMenusIds = roleMenus.stream().map(roleMenu -> {
            return roleMenu.getMenuId();
        }).collect(Collectors.toList());

        //2、组装成父子的树形结构
        // 2.1）、找到所有的一级分类
        List<Menu> levelMenus = list.stream().filter(menuTree ->
              roleMenusIds.contains(menuTree.getId())
        ).map((menu) -> {
            menu.setChildren(getChildrens(menu,list));
            return menu;
        }).sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort()))).collect(Collectors.toList());

        return levelMenus;
    }

    @Override
    public List<Route> getCurrentUserRoute(String roleId) {

        List<Menu> list = baseMapper.selectList(null);

        LambdaQueryWrapper<RoleMenu> roleMenuWrapper = new LambdaQueryWrapper<>();
        roleMenuWrapper.eq(RoleMenu::getRoleId,roleId);
        List<RoleMenu> roleMenus = roleMenuService.list(roleMenuWrapper);

        List<String> roleMenusIds = roleMenus.stream().map(roleMenu -> {
            return roleMenu.getMenuId();
        }).collect(Collectors.toList());

        List<Menu> collect = list.stream().filter(Menu::getIsMenu).collect(Collectors.toList());

        //2、组装成父子的树形结构
        // 2.1）、找到所有的一级分类
        List<Menu> menus = collect.stream()
                .filter(menu -> menu.getIsMenu() && roleMenusIds.contains(menu.getId()))
                .map(menu -> {
            menu.setChildren(getChildrens(menu,collect));
            return menu;
        }).sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort()))).collect(Collectors.toList());

        return convertToRoutes(menus);
    }

    @Override
    public List<Menu> getChildrens(Menu root, List<Menu> all){
        List<Menu> children = all.stream().filter(menuTree -> {
            return Objects.equals(menuTree.getParentId(), root.getId());
        }).map(menuTree -> {
            //1、找到子菜单
            menuTree.setChildren(getChildrens(menuTree, all));
            return menuTree;
        }).sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort()))).collect(Collectors.toList());
        return children;
    }

    // 转换函数
    public static List<Route> convertToRoutes(List<Menu> menus) {
        return menus.stream()
                .map(menu -> {
                    Route route = new Route();
                    route.setPath(menu.getUrl());
                    route.setName(menu.getMenuName());
                    route.setIcon(menu.getIcon());
                    route.setComponent("./" + menu.getLevel());
                    route.setLayout("top");
                    route.setHideInMenu(false);

                    // Recursively convert children if any, using Stream
                    if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
                        route.setRoutes(convertToRoutes(menu.getChildren()));
                    }

                    return route;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Boolean deleteMenu(Long deleteRequestId) {

        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Menu::getParentId,deleteRequestId);
        List<Menu> list = baseMapper.selectList(wrapper);
        ThrowUtils.throwIf(!list.isEmpty(), ErrorCode.SUBMENU_NOT_NULL_ERROR);

        int delete = baseMapper.deleteById(deleteRequestId);

        LambdaQueryWrapper<RoleMenu> menuWrapper = new LambdaQueryWrapper<>();
        menuWrapper.eq(RoleMenu::getMenuId,deleteRequestId);
        boolean remove = roleMenuService.remove(menuWrapper);

        return remove;

    }


    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO 1、检查当前删除的菜单,是否被别的地方引用
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findMenuPath(String menuId) {
        List<String> paths = new ArrayList<>();
        List<String> parentPath = findParentPath(menuId, paths);
        Collections.reverse(parentPath);

        return parentPath.toArray(new Long[parentPath.size()]);
    }
    @Override
    public List<String> findParentPath(String menuId, List<String> paths){
        paths.add(menuId);
        Menu byId = this.getById(menuId);
        if (!Objects.equals(byId.getParentId(), "0")){
            findParentPath(byId.getParentId(),paths);
        }
        return paths;
    }
}





