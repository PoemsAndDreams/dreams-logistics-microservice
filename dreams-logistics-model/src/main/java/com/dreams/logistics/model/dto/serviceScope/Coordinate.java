package com.dreams.logistics.model.dto.serviceScope;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Coordinate {
    private Double longitude; //经度
    private Double latitude; //纬度
}