package com.dreams.logistics.model.dto.transport.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 运输任务完成
 */
@Data
public class TransportTaskCompleteDTO {
    /**
     * 运输任务id
     */
    private String transportTaskId;

    /**
     * 交付凭证,多个图片url以逗号分隔
     */
    private String transportCertificate;

    /**
     * 交付图片,多个图片url以逗号分隔
     */
    private String deliverPicture;
}
