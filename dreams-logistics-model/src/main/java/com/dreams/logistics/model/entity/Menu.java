package com.dreams.logistics.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;

/**
 * 
 * @TableName menu
 */
@TableName(value ="menu")
@Data
public class Menu implements Serializable {
    /**
     * 
     */
    @TableId
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
     * 请求地址
     */
    private String url;

    /**
     * 是否是菜单
     */
    private Boolean isMenu;

    /**
     * 菜单层级
     */
    private String level;

    /**
     * 菜单排序
     */
    private Integer sort;

    /**
     * 
     */
    private String status;

    /**
     * 
     */
    private String icon;

    /**
     * 
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

    @TableField(exist = false)
    List<Menu> children;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}