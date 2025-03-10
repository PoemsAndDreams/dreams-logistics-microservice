package com.dreams.logistics.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 失败消息记录表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("fail_msg")
public class FailMsg implements Serializable {
    @TableId
    private Long id; //主键id
    private String msgId; //消息id
    private String exchange; //交换机
    private String routingKey; //路由key
    private String msg; //消息内容
    private String reason; //失败原因

    private LocalDateTime created;

    private LocalDateTime updated;
}
