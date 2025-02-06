package com.dreams.logistics.model.dto.transport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourierTaskCountDTO {

    /**
     * 快递员
     */
    private Long courierId;
    /**
     * 数量
     */
    private Long count;

}
