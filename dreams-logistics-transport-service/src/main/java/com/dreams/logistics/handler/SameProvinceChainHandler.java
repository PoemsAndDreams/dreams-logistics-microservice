package com.dreams.logistics.handler;


import cn.hutool.core.util.ObjectUtil;
import com.dreams.logistics.constant.CarriageConstant;
import com.dreams.logistics.model.dto.carriage.WaybillDTO;
import com.dreams.logistics.model.entity.Carriage;
import com.dreams.logistics.service.AreaService;
import com.dreams.logistics.service.CarriageService;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 省内寄
 */
@Order(200) //定义顺序
@Component
public class SameProvinceChainHandler extends AbstractCarriageChainHandler {

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

        if (ObjectUtil.equal(receiverProvinceId, senderProvinceId)) {
            //省内
            carriage = this.carriageService.findByTemplateType(CarriageConstant.SAME_PROVINCE);
        }
        return doNextHandler(waybillDTO, carriage);
    }
}
