package com.dreams.logistics.model.entity;

import com.dreams.logistics.enums.PayChannelEnum;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented //标记注解
public @interface PayChannelInter {

    PayChannelEnum type();

}