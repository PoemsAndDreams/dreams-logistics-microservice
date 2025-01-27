package com.dreams.logistics.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreams.logistics.entity.Msg;
import com.dreams.logistics.mapper.MsgMapper;
import com.dreams.logistics.service.MsgService;
import com.dreams.logistics.service.MQService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

/**
 * 发送消息处理服务
 */
@Service
@ConditionalOnBean(MQService.class)
public class MsgServiceImpl extends ServiceImpl<MsgMapper, Msg>
        implements MsgService {
}
