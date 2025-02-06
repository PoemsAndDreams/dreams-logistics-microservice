package com.dreams.logistics.test;

import cn.hutool.json.JSONUtil;
import com.dreams.logistics.model.dto.msg.CourierMsg;
import com.dreams.logistics.mq.CourierMQListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.*;


/**
 * 增量同步帖子到 es
 */
// todo 取消注释开启任务
@Component
public class SaveArticleFile {

    @Resource
    private CourierMQListener courierMQListener;
    /**
     * todo -----2.需更改为合理时间 每3小时执行一次
     */
    @Scheduled(fixedRate = 3 * 60 * 60 * 1000)  // 3小时，单位为毫秒
    public void run() {

        CourierMsg courierMsg = new CourierMsg();
        //目前只用到订单id
        courierMsg.setOrderId(1590586236289646594L);
        String msg2 = JSONUtil.toJsonStr(courierMsg);
        this.courierMQListener.listenCourierPickupMsg(msg2);


        courierMsg.setOrderId(1590587504731062273L);
        String msg = JSONUtil.toJsonStr(courierMsg);
        this.courierMQListener.listenCourierPickupMsg(msg);

        courierMsg.setOrderId(1590587504731062273L);
        String msg1 = JSONUtil.toJsonStr(courierMsg);
        this.courierMQListener.listenCourierPickupMsg(msg1);
    }
}
