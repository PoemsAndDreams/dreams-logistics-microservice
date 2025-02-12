package com.dreams.logistics.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 
 * @TableName work_schedule
 */
@Data
public class WorkScheduleVO implements Serializable {
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
    private List<Integer> weekSchedule;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}