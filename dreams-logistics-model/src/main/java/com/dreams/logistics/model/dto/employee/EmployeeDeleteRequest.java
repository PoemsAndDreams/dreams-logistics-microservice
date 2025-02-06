package com.dreams.logistics.model.dto.employee;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 员工信息表
 * @TableName employee
 */
@Data
public class EmployeeDeleteRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}