package com.dreams.logistics.model.dto.role;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName role
 */
@Data
public class RoleAddRequest implements Serializable {


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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}