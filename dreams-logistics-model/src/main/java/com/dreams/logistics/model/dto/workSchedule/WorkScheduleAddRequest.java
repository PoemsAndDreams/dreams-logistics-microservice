package com.dreams.logistics.model.dto.workSchedule;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 
 * @TableName work_schedule
 */
@Data
public class WorkScheduleAddRequest implements Serializable {

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