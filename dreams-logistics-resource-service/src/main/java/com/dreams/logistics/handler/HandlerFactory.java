package com.dreams.logistics.handler;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.dreams.logistics.enums.PayChannelEnum;
import com.dreams.logistics.model.entity.PayChannelInter;

import java.util.Map;

/**
 * Handler工厂，用于获取指定类型的具体渠道的实例对象
 */
public class HandlerFactory {

    private HandlerFactory() {

    }

    public static <T> T get(PayChannelEnum payChannel, Class<T> handler) {
        Map<String, T> beans = SpringUtil.getBeansOfType(handler);
        for (Map.Entry<String, T> entry : beans.entrySet()) {
            PayChannelInter payChannelAnnotation = entry.getValue().getClass().getAnnotation(PayChannelInter.class);
            if (ObjectUtil.isNotEmpty(payChannelAnnotation) && ObjectUtil.equal(payChannel, payChannelAnnotation.type())) {
                return entry.getValue();
            }
        }
        return null;
    }

    public static <T> T get(String payChannel, Class<T> handler) {
        return get(PayChannelEnum.valueOf(payChannel), handler);
    }
}
