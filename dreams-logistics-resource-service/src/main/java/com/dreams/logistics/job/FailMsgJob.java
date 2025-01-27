package com.dreams.logistics.job;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dreams.logistics.entity.FailMsg;
import com.dreams.logistics.service.FailMsgService;
import com.dreams.logistics.service.MQService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 失败消息的处理任务
 */
@Slf4j
@Component
@ConditionalOnBean({MQService.class, FailMsgService.class})
public class FailMsgJob {

    @Resource
    private FailMsgService failMsgService;
    @Resource
    private MQService mqService;

    @XxlJob("failMsgJob")
    public void execute() {
        //查询失败的数据，每次最多处理100条错误消息
        LambdaQueryWrapper<FailMsg> queryWrapper = new LambdaQueryWrapper<FailMsg>()
                .orderByAsc(FailMsg::getCreated)
                .last("limit 100");
        List<FailMsg> failMsgList = this.failMsgService.list(queryWrapper);
        if (CollUtil.isEmpty(failMsgList)) {
            return;
        }

        for (FailMsg failMsg : failMsgList) {
            try {
                //发送消息
                this.mqService.sendMsg(failMsg.getExchange(), failMsg.getRoutingKey(), failMsg.getMsg());
                //删除数据
                this.failMsgService.removeById(failMsg.getId());
            } catch (Exception e) {
                log.error("处理错误消息失败, failMsg = {}", failMsg);
            }
        }
    }
}
