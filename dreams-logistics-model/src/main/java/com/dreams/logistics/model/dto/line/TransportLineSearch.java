package com.dreams.logistics.model.dto.line;


import lombok.Data;

@Data
public class TransportLineSearch {

    /**
     * 路线名称
     */
    private String name;

    /**
     * 编号
     */
    private String number;


    /**
     * 起点机构id
     */
    private Long startOrganId;


    /**
     * 终点机构id
     */
    private Long endOrganId;


    /**
     * 页数
     */
    private Integer page = 1;


    /**
     * 页数大小
     */
    private Integer pageSize = 10;
}
