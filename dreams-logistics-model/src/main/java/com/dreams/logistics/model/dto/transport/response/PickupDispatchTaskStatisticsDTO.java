package com.dreams.logistics.model.dto.transport.response;

import lombok.Data;

/**
 * 各类任务数量统计
 */
@Data
public class PickupDispatchTaskStatisticsDTO {
    /**
     * 取件任务数量
     */
    private Integer pickupNum = 0;

    /**
     * 取件--待取件数量
     */
    private Integer newPickUpNum = 0;

    /**
     * 取件--已取件数量
     */
    private Integer completePickUpNum = 0;

    /**
     * 取件--取消数量
     */
    private Integer cancelPickUpNum = 0;

    /**
     * 派件任务数量
     */
    private Integer dispatchNum = 0;

    /**
     * 派件--待派件数量
     */
    private Integer newDispatchNum = 0;

    /**
     * 派件--已签收数量
     */
    private Integer signedNum = 0;

    /**
     * 派件--取消数量
     */
    private Integer cancelDispatchNum = 0;
}
