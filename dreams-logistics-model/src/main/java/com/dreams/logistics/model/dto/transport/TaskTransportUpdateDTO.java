package com.dreams.logistics.model.dto.transport;

import lombok.Data;

import java.util.List;

/**
 * 手动调整DTO
 */
@Data
public class TaskTransportUpdateDTO {

    /**
     * id
     */
    private Long id;

    /**
     * 车次Id
     */
    private Long transportTripsId;

    /**
     * 车辆Id
     */
    private Long truckId;

    /**
     * 司机Id
     */
    private List<Long> driverIds;

}
