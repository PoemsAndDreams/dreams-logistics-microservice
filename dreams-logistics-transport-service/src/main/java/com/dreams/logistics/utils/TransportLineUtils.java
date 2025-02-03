package com.dreams.logistics.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.NumberUtil;
import com.dreams.logistics.entity.line.TransportLine;
import com.dreams.logistics.enums.OrganTypeEnum;
import com.dreams.logistics.model.dto.line.Organ;
import com.dreams.logistics.model.dto.line.TransportLineDTO;
import com.dreams.logistics.model.dto.line.TransportLineNode;
import org.neo4j.driver.internal.value.PathValue;
import org.neo4j.driver.types.Path;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 对象转化工具类
 */
public class TransportLineUtils {

    public static TransportLine toEntity(TransportLineDTO transportLineDTO) {
        return BeanUtil.toBeanIgnoreError(transportLineDTO, TransportLine.class);
    }

    public static TransportLineDTO toDTO(TransportLine transportLine) {
        return BeanUtil.toBeanIgnoreError(transportLine, TransportLineDTO.class);
    }

    public static List<TransportLineDTO> toDTOList(List<TransportLine> transportLine) {
        return transportLine.stream()
                .map(TransportLineUtils::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * PathValue对象转化成TransferLineDTO对象
     *
     * @param pathValue Neo4j对象
     * @return TransferLineDTO对象
     */
    public static TransportLineNode convert(PathValue pathValue) {
        TransportLineNode transportLineNode = new TransportLineNode();
        Path path = pathValue.asPath();

        //提取node中的数据，封装成 NodeDTO 对象
        path.nodes().forEach(node -> {
            Map<String, Object> map = node.asMap();
            Organ organ = BeanUtil.toBeanIgnoreError(map, Organ.class);
            //取第一个标签作为类型
            organ.setType(OrganTypeEnum.valueOf(CollUtil.getFirst(node.labels())).getCode());
            //查询出来的数据，x：经度，y：纬度
            organ.setLatitude(BeanUtil.getProperty(map.get("location"), "y"));
            organ.setLongitude(BeanUtil.getProperty(map.get("location"), "x"));
            transportLineNode.getNodeList().add(organ);
        });

        //提取关系中的 cost 数据，进行求和计算，算出该路线的总成本
        path.relationships().forEach(relationship -> {
            Map<String, Object> objectMap = relationship.asMap();
            double cost = Convert.toDouble(objectMap.get("cost"), 0d);
            transportLineNode.setCost(NumberUtil.add(cost, transportLineNode.getCost().doubleValue()));
        });

        //取2位小数
        transportLineNode.setCost(NumberUtil.round(transportLineNode.getCost(), 2).doubleValue());
        return transportLineNode;
    }
}
