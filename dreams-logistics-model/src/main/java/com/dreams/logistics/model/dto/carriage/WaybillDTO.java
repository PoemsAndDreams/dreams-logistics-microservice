package com.dreams.logistics.model.dto.carriage;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;

/**
 * 运费计算参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WaybillDTO {

    @Min(value = 1, message = "长度最小为1")
    @Max(value = 999, message = "长度最大为999")
    private Integer measureLong;

    @Min(value = 1, message = "宽度最小为1")
    @Max(value = 999, message = "宽度最大为999")
    private Integer measureWidth;

    @Min(value = 1, message = "高度最小为1")
    @Max(value = 999, message = "高度最大为999")
    private Integer measureHigh;

    @NotNull(message = "收件地址id不能为空")
    private Long receiverCityId;

    @NotNull(message = "寄件地址id不能为空")
    private Long senderCityId;

    @DecimalMin(value = "0.1", message = "重量必须大于等于0.1")
    @DecimalMax(value = "9999", message = "重量必须小于等于9999")
    @NotNull(message = "重量不能为空")
    private Double weight;

    @Min(value = 1, message = "体积最小为1cm^3")
    @Max(value = 99000000, message = "体积最大为99m^3")
    private Integer volume;
}
