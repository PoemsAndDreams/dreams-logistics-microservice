package com.dreams.logistics.config;

import cn.hutool.core.util.StrUtil;

import com.alibaba.fastjson.JSON;
import com.dreams.logistics.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MessageConfig implements ApplicationContextAware {

    /**
     * 发送者回执 没有路由到队列的情况
     *
     * @param applicationContext 应用上下文
     * @throws BeansException 异常
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 获取RabbitTemplate
        RabbitTemplate rabbitTemplate = applicationContext.getBean(RabbitTemplate.class);
        // 设置ReturnCallback
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {

            if (StrUtil.contains(exchange, Constants.MQ.DELAYED_KEYWORD)) {
                //延迟消息没有发到队列是正常情况，无需记录日志
                return;
            }
            // 投递失败，记录日志
            log.info("消息发送失败，应答码{}，原因{}，交换机{}，路由键{},消息{}",
                    replyCode, replyText, exchange, routingKey, message.toString());

        });
    }

}
