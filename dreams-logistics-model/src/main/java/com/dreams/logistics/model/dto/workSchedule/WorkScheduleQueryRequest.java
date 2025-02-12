package com.dreams.logistics.model.dto.workSchedule;

import com.baomidou.mybatisplus.annotation.TableField;
import com.dreams.logistics.common.PageRequest;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 
 * @TableName work_schedule
 */
@Data
public class WorkScheduleQueryRequest extends PageRequest implements Serializable {


    /**
     * 
     */
    private Long userId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}