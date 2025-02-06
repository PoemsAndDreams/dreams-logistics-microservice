package com.dreams.logistics.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 运单与运输任务关联表
 * @TableName transport_order_task
 */
@TableName(value ="transport_order_task")
@Data
public class TransportOrderTask implements Serializable {
    /**
     * id
     */
    @TableId
    private Long id;

    /**
     * 运单id
     */
    private String transportOrderId;

    /**
     * 运输任务id
     */
    private Long transportTaskId;

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