package com.dreams.logistics.model.dto.role;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * 
 * @TableName role
 */
@Data
public class RoleRemoveRequest implements Serializable {
    /**
     * 
     */
    private String id;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}