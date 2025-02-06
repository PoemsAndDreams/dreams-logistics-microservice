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
public class EmployeeUpdateRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 员工姓名
     */
    private String name;

    /**
     * 员工手机号
     */
    private String phone;

    /**
     * 上班日 1-31
     */
    private Integer workDay;

    /**
     * 上班月 1-12
     */
    private String workMonth;

    /**
     * 所属机构id
     */
    private Long orgId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}