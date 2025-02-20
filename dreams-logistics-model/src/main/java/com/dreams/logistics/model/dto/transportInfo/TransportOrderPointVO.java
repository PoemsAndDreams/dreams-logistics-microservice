package com.dreams.logistics.model.dto.transportInfo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单跟踪
 */
@Data
public class TransportOrderPointVO {
    /**
     * 创建时间
     */
    private LocalDateTime created;

    /**
     * 详细信息 您的快件已到达【北京通州分拣中心】
     */
    private String info;

    /**
     * 状态 运输中
     */
    private String status;

}
