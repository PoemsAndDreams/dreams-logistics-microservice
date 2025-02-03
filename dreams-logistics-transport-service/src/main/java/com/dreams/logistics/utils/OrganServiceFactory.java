package com.dreams.logistics.utils;

import cn.hutool.extra.spring.SpringUtil;
import com.dreams.logistics.exception.BusinessException;
import com.dreams.logistics.enums.ExceptionEnum;
import com.dreams.logistics.enums.OrganTypeEnum;
import com.dreams.logistics.service.AgencyService;
import com.dreams.logistics.service.IService;
import com.dreams.logistics.service.OLTService;
import com.dreams.logistics.service.TLTService;

/**
 * 根据type选择对应的service返回
 */
public class OrganServiceFactory {

    public static IService getBean(Integer type) {
        OrganTypeEnum organTypeEnum = OrganTypeEnum.codeOf(type);
        if (null == organTypeEnum) {
            throw new BusinessException(ExceptionEnum.ORGAN_TYPE_ERROR);
        }

        switch (organTypeEnum) {
            case AGENCY: {
                return SpringUtil.getBean(AgencyService.class);
            }
            case OLT: {
                return SpringUtil.getBean(OLTService.class);
            }
            case TLT: {
                return SpringUtil.getBean(TLTService.class);
            }
        }
        return null;
    }

}
