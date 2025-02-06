package com.dreams.logistics.model.dto.transport.response;

import lombok.Data;

import java.time.LocalDateTime;


@Data
public class OrderToTransportOrderDTO {

    private String id; //运单号
    private Long orderId; //订单id
    private LocalDateTime created;//时间

}
