package com.dreams.logistics.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName work_schedule
 */
@TableName(value ="work_schedule")
@Data
public class WorkSchedule implements Serializable {
    /**
     * ID
     */
    private Long id;

    /**
     * 
     */
    private Long userId;

    /**
     * 
     */
    private Integer weekSchedule;

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