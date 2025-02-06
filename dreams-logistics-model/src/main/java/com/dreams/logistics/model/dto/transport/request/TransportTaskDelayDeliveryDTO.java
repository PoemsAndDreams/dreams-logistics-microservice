package com.dreams.logistics.model.dto.transport.request;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 延迟提货对象
 */
@Data
public class TransportTaskDelayDeliveryDTO {
    /**
     * 运输任务id
     */
    private String transportTaskId;

    /**
     * 延迟时间
     */
    private String delayTime;

    /**
     * 延迟原因
     */
    private String delayReason;
}
