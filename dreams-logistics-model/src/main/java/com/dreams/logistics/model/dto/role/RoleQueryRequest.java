package com.dreams.logistics.model.dto.role;

import com.baomidou.mybatisplus.annotation.TableField;
import com.dreams.logistics.common.PageRequest;
import lombok.Data;

import java.io.Serializable;

/**
 * 
 * @TableName role
 */
@Data
public class RoleQueryRequest extends PageRequest implements Serializable {

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
    private String status;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}