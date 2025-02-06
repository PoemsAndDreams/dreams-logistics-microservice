package com.dreams.logistics.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreams.logistics.model.entity.TransportOrderTask;
import com.dreams.logistics.service.TransportOrderTaskService;
import com.dreams.logistics.mapper.TransportOrderTaskMapper;
import org.springframework.stereotype.Service;

/**
* @author xiayutian
* @description 针对表【transport_order_task(运单与运输任务关联表)】的数据库操作Service实现
* @createDate 2025-02-05 15:19:37
*/
@Service
public class TransportOrderTaskServiceImpl extends ServiceImpl<TransportOrderTaskMapper, TransportOrderTask>
    implements TransportOrderTaskService{

}




