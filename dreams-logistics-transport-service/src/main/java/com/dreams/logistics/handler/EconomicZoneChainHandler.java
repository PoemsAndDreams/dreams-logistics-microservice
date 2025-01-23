package com.dreams.logistics.handler;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import com.dreams.logistics.constant.CarriageConstant;
import com.dreams.logistics.enums.EconomicRegionEnum;
import com.dreams.logistics.model.dto.carriage.WaybillDTO;
import com.dreams.logistics.model.entity.Carriage;
import com.dreams.logistics.service.AreaService;
import com.dreams.logistics.service.CarriageService;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.LinkedHashMap;

/**
 * 经济区互寄
 */
@Order(300) //定义顺序
@Component
public class EconomicZoneChainHandler extends AbstractCarriageChainHandler {

    @Resource
    private CarriageService carriageService;
    @Resource
    private AreaService areaService;

    @Override
    public Carriage doHandler(WaybillDTO waybillDTO) {
        Carriage carriage = null;

        // 获取收寄件地址省份id
        Long receiverProvinceId = Long.valueOf(this.areaService.getById(waybillDTO.getReceiverCityId()).getParentId());
        Long senderProvinceId = Long.valueOf(this.areaService.getById(waybillDTO.getSenderCityId()).getParentId());

        //获取经济区城市配置枚举
        LinkedHashMap<String, EconomicRegionEnum> EconomicRegionMap = EnumUtil.getEnumMap(EconomicRegionEnum.class);
        EconomicRegionEnum economicRegionEnum = null;
        for (EconomicRegionEnum regionEnum : EconomicRegionMap.values()) {
            //该经济区是否全部包含收发件省id
            boolean result = ArrayUtil.containsAll(regionEnum.getValue(), receiverProvinceId, senderProvinceId);
            if (result) {
                economicRegionEnum = regionEnum;
                break;
            }
        }

        if (ObjectUtil.isNotEmpty(economicRegionEnum)) {
            //根据类型编码查询
            LambdaQueryWrapper<Carriage> queryWrapper = Wrappers.lambdaQuery(Carriage.class)
                    .eq(Carriage::getTemplateType, CarriageConstant.ECONOMIC_ZONE)
                    .eq(Carriage::getTransportType, CarriageConstant.REGULAR_FAST)
                    .like(Carriage::getAssociatedCity, economicRegionEnum.getCode());
            carriage = this.carriageService.getOne(queryWrapper);
        }

        return doNextHandler(waybillDTO, carriage);
    }
}
