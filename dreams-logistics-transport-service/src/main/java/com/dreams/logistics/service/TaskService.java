package com.dreams.logistics.service;

import com.dreams.logistics.model.dto.task.TaskPickupVO;
import com.dreams.logistics.model.dto.task.TaskSignVO;
import com.dreams.logistics.model.dto.transportInfo.TransportOrderPointVO;

import java.util.List;

public interface TaskService {

    /**
     * 取件
     *
     * @param taskPickupVO 取件对象
     * @return 是否成功
     */
    boolean pickup(TaskPickupVO taskPickupVO);


    /**
     * 签收任务
     *
     * @param taskSignVO 签收对象
     */
    void sign(TaskSignVO taskSignVO);

    /**
     * 拒收任务
     *
     * @param id 任务id
     */
    void reject(String id);

    /**
     * 运单跟踪
     *
     * @param id 运单id
     * @return 运单跟踪信息
     */
    List<TransportOrderPointVO> tracks(String id);

}
