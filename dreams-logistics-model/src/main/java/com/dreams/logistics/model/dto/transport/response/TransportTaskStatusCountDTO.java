package com.dreams.logistics.model.dto.transport.response;

import com.dreams.logistics.enums.TransportTaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransportTaskStatusCountDTO {

    /**
     * 状态枚举
     */
    private TransportTaskStatus status;
    /**
     * 状态编码
     */
    private Integer statusCode;
    /**
     * 数量
     */
    private Long count;

}
