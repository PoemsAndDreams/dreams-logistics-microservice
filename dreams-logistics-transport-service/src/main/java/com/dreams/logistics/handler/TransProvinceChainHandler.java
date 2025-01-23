package com.dreams.logistics.handler;


import com.dreams.logistics.constant.CarriageConstant;
import com.dreams.logistics.model.dto.carriage.WaybillDTO;
import com.dreams.logistics.model.entity.Carriage;
import com.dreams.logistics.service.CarriageService;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 跨省
 */
@Order(400) //定义顺序
@Component
public class TransProvinceChainHandler extends AbstractCarriageChainHandler {

    @Resource
    private CarriageService carriageService;

    @Override
    public Carriage doHandler(WaybillDTO waybillDTO) {
        Carriage carriage = this.carriageService.findByTemplateType(CarriageConstant.TRANS_PROVINCE);
        return doNextHandler(waybillDTO, carriage);
    }
}
