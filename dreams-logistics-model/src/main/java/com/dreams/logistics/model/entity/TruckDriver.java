package com.dreams.logistics.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 车辆和司机关联表
 * @TableName truck_driver
 */
@TableName(value ="truck_driver")
@Data
public class TruckDriver implements Serializable {
    /**
     * id
     */
    @TableId
    private Long id;

    /**
     * 车辆id
     */
    private Long truckId;

    /**
     * 司机id
     */
    private Long driverId;

    /**
     * 创建时间
     */
    private Date created;

    /**
     * 更新时间
     */
    private Date updated;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}