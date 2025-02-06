package com.dreams.logistics.model.dto.transport.response;

import com.dreams.logistics.enums.TransportOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransportOrderStatusCountDTO {

    /**
     * 状态枚举
     */
    private TransportOrderStatus status;
    /**
     * 状态编码
     */
    private Integer statusCode;

    /**
     * 数量
     */
    private Long count;

}
