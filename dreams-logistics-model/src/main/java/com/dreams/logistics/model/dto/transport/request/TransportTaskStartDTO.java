package com.dreams.logistics.model.dto.transport.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 运输任务开始
 */
@Data
public class TransportTaskStartDTO {
    /**
     * 运输任务id
     */
    private String transportTaskId;

    /**
     * 提货凭证,多个图片url以逗号分隔
     */
    private String cargoPickUpPicture;

    /**
     * 货物照片,多个图片url以逗号分隔
     */
    private String cargoPicture;
}
