package com.dreams.logistics.model.dto.line;

import lombok.Data;

/**
 * 路线对象
 */
@Data
public class TransportLineDTO {

    /**
     * 名称
     */
    private Long id;


    /**
     * 编号
     */
    private String number;


    /**
     * 成本
     */
    private Double cost;


    /**
     * 类型，1:干线，2：支线，3:接驳路线
     */
    private Integer type;


    /**
     * 路线名称
     */
    private String name;

    /**
     * 距离，单位：米
     */
    private Double distance;


    /**
     * 时间，单位：秒
     */
    private Double time;


    /**
     * 创建时间
     */
    private Long created;


    /**
     * 修改时间
     */
    private Long updated;


    /**
     * 扩展字段，以json格式存储
     */
    private String extra;


    /**
     * 起点机构id
     */
    private Long startOrganId;


    /**
     * 起点机构名称，只有在查询时返回
     */
    private String startOrganName;


    /**
     * 终点机构id
     */
    private Long endOrganId;


    /**
     * 终点机构名称，只有在查询时返回
     */
    private String endOrganName;

}
