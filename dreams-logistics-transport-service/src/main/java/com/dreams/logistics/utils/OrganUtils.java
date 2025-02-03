package com.dreams.logistics.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.ObjectUtil;
import com.dreams.logistics.exception.BusinessException;
import com.dreams.logistics.entity.node.AgencyEntity;
import com.dreams.logistics.entity.node.BaseEntity;
import com.dreams.logistics.entity.node.OLTEntity;
import com.dreams.logistics.entity.node.TLTEntity;
import com.dreams.logistics.enums.ExceptionEnum;
import com.dreams.logistics.enums.OrganTypeEnum;
import com.dreams.logistics.model.dto.line.Organ;
import org.springframework.data.geo.Point;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 机构工具类
 */
public class OrganUtils {

    public static Organ toAgencyDTO(BaseEntity baseEntity) {
        Organ organ = BeanUtil.toBean(baseEntity, Organ.class, CopyOptions.create().setIgnoreProperties("id", "location"));
        //数据库中的bid是对外的id
        organ.setId(baseEntity.getBid());
        organ.setLatitude(BeanUtil.getProperty(baseEntity.getLocation(), "x"));
        organ.setLongitude(BeanUtil.getProperty(baseEntity.getLocation(), "y"));
        organ.setType(baseEntity.getAgencyType().getCode());
        return organ;
    }

    public static BaseEntity toEntity(Organ organ) {
        BaseEntity baseEntity;
        OrganTypeEnum organType = OrganTypeEnum.codeOf(organ.getType());
        switch (organType) {
            case OLT: {
                baseEntity = BeanUtil.toBean(organ, OLTEntity.class, CopyOptions.create().ignoreNullValue());
                break;
            }
            case TLT: {
                baseEntity = BeanUtil.toBean(organ, TLTEntity.class, CopyOptions.create().ignoreNullValue());
                break;
            }
            case AGENCY: {
                baseEntity = BeanUtil.toBean(organ, AgencyEntity.class, CopyOptions.create().ignoreNullValue());
                break;
            }
            default: {
                throw new BusinessException(ExceptionEnum.ORGAN_TYPE_ERROR);
            }
        }

        baseEntity.setId(null);
        baseEntity.setBid(organ.getId());
        if (ObjectUtil.isAllNotEmpty(organ.getLatitude(), organ.getLongitude())) {
            baseEntity.setLocation(new Point(organ.getLatitude(), organ.getLongitude()));
        }
        return baseEntity;
    }

    public static List<Organ> toAgencyDTOList(List<? extends BaseEntity> list) {
        return list.stream()
                .map(OrganUtils::toAgencyDTO)
                .collect(Collectors.toList());
    }

}
