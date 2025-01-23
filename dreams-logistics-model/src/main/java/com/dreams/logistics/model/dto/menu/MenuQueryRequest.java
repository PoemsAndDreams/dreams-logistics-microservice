package com.dreams.logistics.model.dto.menu;

import com.baomidou.mybatisplus.annotation.TableField;
import com.dreams.logistics.common.PageRequest;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 
 * @TableName menu
 */

@Data
public class MenuQueryRequest extends PageRequest implements Serializable {


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
     *
     */
    private String status;

    /**
     * 菜单排序
     */
    private Integer sort;

    @TableField(exist = false)
    List<MenuQueryRequest> children;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}