package com.dreams.logistics.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 发送消息记录表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("msg")
public class Msg implements Serializable {
    @TableId
    private Long id; //主键id
    private String msgId; //消息id
    private String exchange; //交换机
    private String routingKey; //路由key
    private String msg; //消息内容
    private String extend; //扩展
    @TableField(fill = FieldFill.INSERT) //MP自动填充
    private LocalDateTime created;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updated;
}
