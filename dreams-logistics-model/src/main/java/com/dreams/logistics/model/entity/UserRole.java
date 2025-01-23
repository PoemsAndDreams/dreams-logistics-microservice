package com.dreams.logistics.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName user_role
 */
@TableName(value ="user_role")
@Data
public class UserRole implements Serializable {
    /**
     * 
     */
    @TableId
    private String id;

    /**
     * 
     */
    private String userId;

    /**
     * 
     */
    private String roleId;

    /**
     * 
     */
    private Date createTime;

    /**
     * 
     */
    private String creator;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}