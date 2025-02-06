package com.dreams.logistics.model.dto.employee;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dreams.logistics.common.PageRequest;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 员工信息表
 * @TableName employee
 */
@Data
public class EmployeeQueryRequest extends PageRequest implements Serializable {

    /**
     * 员工姓名
     */
    private String name;

    /**
     * 员工编号
     */
    private String employeeNumber;

    /**
     * 1:员工，2：快递员，3：司机
     */
    private Integer userType;

    /**
     * 所属机构id
     */
    private Long orgId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}