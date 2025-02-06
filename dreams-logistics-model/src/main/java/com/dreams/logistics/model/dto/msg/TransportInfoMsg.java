package com.dreams.logistics.model.dto.msg;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 运单跟踪消息
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TransportInfoMsg extends BaseMsg implements Serializable {
    private static final long serialVersionUID = 2386845612549524970L;

    /**
     * 机构id
     */
    private Long organId;

    /**
     * 运单id
     */
    private String transportOrderId;

    /**
     * 消息状态
     */
    private String status;

    /**
     * 消息详情
     */
    private String info;

}
