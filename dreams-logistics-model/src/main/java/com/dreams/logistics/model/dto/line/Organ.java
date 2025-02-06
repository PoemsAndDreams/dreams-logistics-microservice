package com.dreams.logistics.model.dto.line;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

/**
 * 机构数据对象
 */
@Data
public class Organ {
    /**
     * 机构id
     */
    @Alias("bid")
    private Long id;


    /**
     * 名称
     */
    private String name;


    /**
     * 类型，1:一级转运，2：二级转运，3:网点
     */
    private Integer type;


    /**
     * 电话
     */
    private String phone;


    /**
     * 地址
     */
    private String address;


    /**
     * 纬度
     */
    private Double latitude;


    /**
     * 经度
     */
    private Double longitude;


    /**
     * 父节点id
     */
    private Long parentId;


    /**
     * 负责人
     */
    private String managerName;


    /**
     * 扩展字段，以json格式存储
     */
    private String extra;


    /**
     * 是否可用
     */
    private Boolean status;

}
