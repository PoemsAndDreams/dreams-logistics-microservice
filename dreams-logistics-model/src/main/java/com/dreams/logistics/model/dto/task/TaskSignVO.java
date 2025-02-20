package com.dreams.logistics.model.dto.task;

import lombok.Data;


/**
 * 签收模型
 */
@Data
public class TaskSignVO {

    /**
     * 派件任务id
     */
    private String id;

    /**
     * 签收人，1本人，2代收
     */
    private Integer signRecipient;

}
