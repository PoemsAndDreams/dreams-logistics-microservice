package com.dreams.logistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreams.logistics.model.entity.RoleMenu;
import com.dreams.logistics.service.RoleMenuService;
import com.dreams.logistics.mapper.RoleMenuMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author xiayutian
 * @description 针对表【role_menu】的数据库操作Service实现
 * @createDate 2025-01-20 11:44:00
 */
@Service
public class RoleMenuServiceImpl extends ServiceImpl<RoleMenuMapper, RoleMenu>
        implements RoleMenuService {

    @Override
    public Boolean batchAddRoleMenuToBank(String roleId, List<String> roleMenuIds) {

        //批量添加该角色的菜单
        LambdaQueryWrapper<RoleMenu> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(RoleMenu::getRoleId, roleId);
        deleteWrapper.notIn(RoleMenu::getMenuId, roleMenuIds);
        baseMapper.delete(deleteWrapper);

        LambdaQueryWrapper<RoleMenu> roleMenuWrapper = new LambdaQueryWrapper<>();
        roleMenuWrapper.eq(RoleMenu::getRoleId, roleId);
        List<RoleMenu> menus = baseMapper.selectList(roleMenuWrapper);

        Set<String> menuSet = menus.stream().map(RoleMenu::getMenuId).collect(Collectors.toSet());

        List<RoleMenu> menuList = roleMenuIds.stream()
                .filter(menuId -> {
                    return !menuSet.contains(menuId);
                })
                .map(menuId -> {
                    RoleMenu roleMenu = new RoleMenu();
                    roleMenu.setRoleId(roleId);
                    roleMenu.setMenuId(menuId);
                    return roleMenu;
                }).collect(Collectors.toList());
        return this.saveBatch(menuList);

    }
}




