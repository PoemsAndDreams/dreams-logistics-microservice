package com.dreams.logistics.service;

import com.dreams.logistics.model.entity.Menu;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dreams.logistics.model.vo.Route;

import java.util.List;

/**
* @author xiayutian
* @description 针对表【menu】的数据库操作Service
* @createDate 2025-01-20 11:44:00
*/
public interface MenuService extends IService<Menu> {

    List<Menu> listWithTree();

    void removeMenuByIds(List<Long> asList);

    Long[] findMenuPath(String menuId);

    List<String> findParentPath(String menuId, List<String> paths);

    List<Menu> getChildrens(Menu root, List<Menu> all);

    List<Menu> getCurrentUserMenu(String roleId);

    List<Route> getCurrentUserRoute(String userId);

    Boolean deleteMenu(Long deleteRequestId);
}
