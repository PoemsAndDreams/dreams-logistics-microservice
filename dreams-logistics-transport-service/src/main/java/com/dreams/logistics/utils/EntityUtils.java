package com.dreams.logistics.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.dreams.logistics.model.dto.serviceScope.Coordinate;
import com.dreams.logistics.model.dto.serviceScope.ServiceScopeDTO;
import com.dreams.logistics.model.entity.ServiceScope;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 实体工具类
 */
public class EntityUtils {

    public static ServiceScopeDTO toDTO(ServiceScope entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return null;
        }
        ServiceScopeDTO serviceScopeDTO = BeanUtil.toBeanIgnoreError(entity, ServiceScopeDTO.class);
        //设置范围
        List<Coordinate> coordinateList = entity.getPolygon().getPoints()
                .stream().map(point -> new Coordinate(point.getX(), point.getY()))
                .collect(Collectors.toList());
        serviceScopeDTO.setPolygon(coordinateList);
        return serviceScopeDTO;
    }

    public static List<ServiceScopeDTO> toDTOList(List<ServiceScope> entityList) {
        if (CollUtil.isEmpty(entityList)) {
            return Collections.emptyList();
        }
        List<ServiceScopeDTO> list = new ArrayList<>();
        entityList.forEach(entity -> list.add(toDTO(entity)));
        return list;
    }

    public static ServiceScope toEntity(ServiceScopeDTO dto) {
        if (ObjectUtil.isEmpty(dto)) {
            return null;
        }
        ServiceScope serviceScopeEntity = BeanUtil.toBeanIgnoreError(dto, ServiceScope.class);
        List<Point> pointList = dto.getPolygon().stream()
                .map(coordinate -> new Point(coordinate.getLongitude(), coordinate.getLatitude()))
                .collect(Collectors.toList());
        serviceScopeEntity.setPolygon(new GeoJsonPolygon(pointList));
        return serviceScopeEntity;
    }

    public static List<ServiceScope> toEntity(List<ServiceScopeDTO> dtoList) {
        if (CollUtil.isEmpty(dtoList)) {
            return Collections.emptyList();
        }
        List<ServiceScope> list = new ArrayList<>();
        dtoList.forEach(dto -> list.add(toEntity(dto)));
        return list;
    }
}
