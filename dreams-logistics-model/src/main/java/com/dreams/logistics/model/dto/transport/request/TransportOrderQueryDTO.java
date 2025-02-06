package com.dreams.logistics.model.dto.transport.request;

import com.dreams.logistics.enums.TransportOrderSchedulingStatus;
import com.dreams.logistics.enums.TransportOrderStatus;
import lombok.Data;

@Data
public class TransportOrderQueryDTO {
    private String id;

    private Long orderId;

    /**
     * 运单状态(1.新建 2.已装车，发往x转运中心 3.到达 4.到达终端网点 5.已签收 6.拒收))
     */
    private TransportOrderStatus status;

    /**
     * 调度状态调度状态(1.待调度2.未匹配线路3.已调度)
     */
    private TransportOrderSchedulingStatus schedulingStatus;

    /**
     * 起始网点id
     */
    private Long startAgencyId;

    /**
     *终点网点id
     */
    private Long endAgencyId;

    /**
     * 当前所在机构id
     */
    private Long currentAgencyId;

    private Integer page;

    private Integer pageSize;

}
