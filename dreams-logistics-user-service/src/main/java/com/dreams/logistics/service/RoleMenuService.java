package com.dreams.logistics.service;

import com.dreams.logistics.model.entity.RoleMenu;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author xiayutian
* @description 针对表【role_menu】的数据库操作Service
* @createDate 2025-01-20 11:44:00
*/
public interface RoleMenuService extends IService<RoleMenu> {

    Boolean batchAddRoleMenuToBank(String roleId, List<String> roleMenuIds);
}
