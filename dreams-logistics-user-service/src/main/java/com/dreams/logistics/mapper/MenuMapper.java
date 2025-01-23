package com.dreams.logistics.mapper;

import com.dreams.logistics.model.entity.Menu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author xiayutian
* @description 针对表【menu】的数据库操作Mapper
* @createDate 2025-01-20 11:44:00
* @Entity com.dreams.logistics.model.entity.Menu
*/
public interface MenuMapper extends BaseMapper<Menu> {

    @Select("SELECT	* FROM menu WHERE id IN (SELECT menu_id FROM role_menu WHERE role_id IN ( SELECT role_id FROM user_role WHERE user_id = #{userId} ))")
    List<Menu> selectPermissionByUserId(@Param("userId") String userId);


}




