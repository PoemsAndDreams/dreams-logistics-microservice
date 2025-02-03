package com.dreams.logistics.model.dto.line;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 运输路线对象
 */
@Data
public class TransportLineNode {

    /**
     * 节点列表
     */
    private List<Organ> nodeList = new ArrayList<>();

    /**
     * 路线成本
     */
    private Double cost = 0d;

}
