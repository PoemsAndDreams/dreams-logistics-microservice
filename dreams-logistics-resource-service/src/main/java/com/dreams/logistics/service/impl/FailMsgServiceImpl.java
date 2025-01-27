package com.dreams.logistics.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.dreams.logistics.entity.FailMsg;
import com.dreams.logistics.mapper.FailMsgMapper;
import com.dreams.logistics.service.FailMsgService;
import com.dreams.logistics.service.MQService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

/**
 * 失败消息处理服务
 */
@Service
@ConditionalOnBean(MQService.class)
public class FailMsgServiceImpl extends ServiceImpl<FailMsgMapper, FailMsg>
        implements FailMsgService {
}
