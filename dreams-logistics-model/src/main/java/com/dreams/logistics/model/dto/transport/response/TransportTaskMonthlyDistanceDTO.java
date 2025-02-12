package com.dreams.logistics.model.dto.transport.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class TransportTaskMonthlyDistanceDTO {
    /**
     * 日期，格式：2022-07-16 00:00:00
     */
   @ApiModelProperty(example = "2022-08-15 00:00:00", dataType = "string")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateTime;

    /**
     * 里程
     */
    private Double mileage;
}
