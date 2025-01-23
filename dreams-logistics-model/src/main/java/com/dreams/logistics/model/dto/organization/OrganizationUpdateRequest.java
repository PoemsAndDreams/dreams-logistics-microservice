package com.dreams.logistics.model.dto.organization;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 组织
 * @TableName organization
 */
@Data
public class OrganizationUpdateRequest implements Serializable {


    /**
     * ID
     */
    private String id;

    /**
     * 名称
     */
    private String name;

    /**
     * 简称
     */
    private String abbreviation;

    /**
     * 父ID
     */
    private String parentId;

    /**
     * 部门类型 1为分公司，2为一级转运中心 3为二级转运中心 4为网点
     */
    private Integer orgType;

    /**
     * 省
     */
    private Long provinceId;

    /**
     * 市
     */
    private Long cityId;

    /**
     * 区
     */
    private Long countyId;

    /**
     * 地址
     */
    private String address;

    /**
     * 联系电话
     */
    private String contractNumber;

    /**
     * 负责人id
     */
    private Long managerId;


    /**
     * 排序
     */
    private Integer sortValue;

    /**
     * 状态
     */
    private Boolean status;

    /**
     * 描述
     */
    private String description;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}