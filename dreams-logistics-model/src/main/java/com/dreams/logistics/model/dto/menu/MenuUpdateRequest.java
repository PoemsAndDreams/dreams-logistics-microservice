package com.dreams.logistics.model.dto.menu;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * 
 * @TableName menu
 */

@Data
public class MenuUpdateRequest implements Serializable {

    private String id;

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
     * 菜单层级
     */
    private String level;

    /**
     * 请求地址
     */
    private String url;

    /**
     * 是否是菜单
     */
    private Boolean isMenu;

    /**
     * 菜单排序
     */
    private Integer sort;

    /**
     * 
     */
    private String status;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}