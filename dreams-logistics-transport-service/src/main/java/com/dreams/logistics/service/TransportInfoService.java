package com.dreams.logistics.service;


import com.dreams.logistics.model.entity.TransportInfo;
import com.dreams.logistics.model.entity.TransportInfoDetail;

/**
 * 物流信息服务
 */
public interface TransportInfoService {


    /**
     * 如果运单数据不存在，就新增，否则更新数据
     *
     * @param transportOrderId 运单id
     * @param infoDetail       信息详情
     * @return 运输信息数据
     */
    TransportInfo saveOrUpdate(String transportOrderId, TransportInfoDetail infoDetail);

    /**
     * 根据运单id查询运输信息
     *
     * @param transportOrderId 运单id
     * @return 运输信息数据
     */
    TransportInfo queryByTransportOrderId(String transportOrderId);

    TransportInfo queryByOrderId(String orderId);
}
