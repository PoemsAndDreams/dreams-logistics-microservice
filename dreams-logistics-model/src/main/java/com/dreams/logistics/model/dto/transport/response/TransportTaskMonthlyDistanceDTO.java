package com.dreams.logistics.model.dto.transport.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TransportTaskMonthlyDistanceDTO {
    /**
     * 日期，格式：2022-07-16 00:00:00
     */
    private LocalDateTime dateTime;

    /**
     * 里程
     */
    private Double mileage;
}
