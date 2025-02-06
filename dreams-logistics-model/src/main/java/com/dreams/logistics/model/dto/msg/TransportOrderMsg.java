package com.dreams.logistics.model.dto.msg;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 运单消息
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TransportOrderMsg extends BaseMsg {

    /**
     * 运单号
     */
    private String id;

    /**
     * 订单id
     */
    private Long orderId;

}
