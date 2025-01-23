package com.dreams.logistics.model.dto.menu;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * 
 * @TableName menu
 */

@Data
public class MenuRemoveRequest implements Serializable {

    private String id;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}