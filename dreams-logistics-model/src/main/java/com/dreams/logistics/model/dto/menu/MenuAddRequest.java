package com.dreams.logistics.model.dto.menu;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 
 * @TableName menu
 */

@Data
public class MenuAddRequest implements Serializable {


    /**
     * 菜单编码
     */
    private String code;

    /**
     * 父菜单ID
     */
    private String parentId;

    /**
     * 名称
     */
    private String menuName;

    /**
     * 请求地址
     */
    private String url;
    /**
     * 菜单层级
     */
    private String level;
    /**
     * 是否是菜单
     */
    private Boolean isMenu;
    /**
     *
     */
    private String status;

    /**
     * 菜单排序
     */
    private Integer sort;

    @TableField(exist = false)
    List<MenuAddRequest> children;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}