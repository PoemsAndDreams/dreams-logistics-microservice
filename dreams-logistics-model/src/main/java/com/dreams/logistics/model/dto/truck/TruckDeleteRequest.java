package com.dreams.logistics.model.dto.truck;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 车辆信息表
 * @TableName truck
 */
@Data
public class TruckDeleteRequest implements Serializable {
    /**
     * id
     */
    private Long id;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}