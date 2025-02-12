package com.dreams.logistics.model.dto.truckTrips;

import lombok.Data;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;
import java.util.List;

/**
 * @author PoemsAndDreams
 * @date 2025-02-12 00:01
 * @description //TODO
 */
@Data
public class BatchSaveTruckDriver implements Serializable {

    private Long transportTripsId;

    private List<String> licensePlateList;
}
