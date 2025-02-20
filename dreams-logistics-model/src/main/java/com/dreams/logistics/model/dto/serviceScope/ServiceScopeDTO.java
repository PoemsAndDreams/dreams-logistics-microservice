package com.dreams.logistics.model.dto.serviceScope;

import lombok.Data;

import java.util.List;

@Data
public class ServiceScopeDTO {

    /**
     * 业务id，可以是机构或快递员
     */
    private Long bid;

    /**
     * 类型，1-机构，2-快递员
     */
    private Integer type;

    /**
     * 多边形坐标点，至少3个坐标点，首尾坐标必须相同
     */
    private List<Coordinate> polygon;

    private Long created;

    private Long updated;

}
