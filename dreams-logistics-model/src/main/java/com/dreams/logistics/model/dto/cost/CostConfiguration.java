package com.dreams.logistics.model.dto.cost;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CostConfiguration {
    /**
     * 线路类型
     * 1, 干线,一级转运中心到一级转运中心
     * 2, 支线, 一级转运中心与二级转运中心之间线路
     * 3, 接驳路线, 二级转运中心到网点
     */

    private Integer transportLineType;

    /**"
     * 成本，只支持输入数字，小数点后2位，不能为空
     * 默认值：干线0.8元、支线1.2元、接驳1.5
     */
    private Double cost;
}
