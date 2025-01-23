package com.dreams.logistics.handler;

import cn.hutool.core.util.ObjectUtil;
import com.dreams.logistics.constant.CarriageConstant;
import com.dreams.logistics.model.dto.carriage.WaybillDTO;
import com.dreams.logistics.model.entity.Carriage;
import com.dreams.logistics.service.CarriageService;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 同城寄
 */
@Order(100) //定义顺序
@Component
public class SameCityChainHandler extends AbstractCarriageChainHandler {

    @Resource
    private CarriageService carriageService;

    @Override
    public Carriage doHandler(WaybillDTO waybillDTO) {
        Carriage carriage = null;
        if (ObjectUtil.equals(waybillDTO.getReceiverCityId(), waybillDTO.getSenderCityId())) {
            //同城
            carriage = this.carriageService.findByTemplateType(CarriageConstant.SAME_CITY);
        }
        return doNextHandler(waybillDTO, carriage);
    }
}
