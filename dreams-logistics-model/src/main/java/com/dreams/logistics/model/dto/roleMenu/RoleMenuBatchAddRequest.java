package com.dreams.logistics.model.dto.roleMenu;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 
 * @TableName menu
 */

@Data
public class RoleMenuBatchAddRequest implements Serializable {

    /**
     *
     */
    private String roleId;

    private List<String> RoleMenuIds;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}