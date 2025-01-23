package com.dreams.logistics.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.dreams.logistics.model.entity.Menu;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 
 * @TableName role
 */
@Data
public class RoleMenuVo implements Serializable {
    /**
     * 
     */
    private String id;

    /**
     * 
     */
    private String roleName;

    /**
     * 
     */
    private String roleCode;

    /**
     * 
     */
    private String description;

    /**
     * 
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

    /**
     * 
     */
    private String status;

    List<Menu> children;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}