package com.dreams.logistics.model.dto.transport.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;


@Data
public class OrderToTransportOrderDTO {

    private String id; //运单号
    private Long orderId; //订单id

   @ApiModelProperty(example = "2022-08-15 00:00:00", dataType = "string")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;//时间

}
