package com.dreams.logistics.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 
 * @TableName pay_channel
 */
@TableName(value ="pay_channel")
@Data
public class PayChannel implements Serializable {
    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 通道名称
     */
    private String channelName;

    /**
     * 通道唯一标记
     */
    private String channelLabel;

    /**
     * 域名
     */
    private String domain;

    /**
     * 商户appid
     */
    private String appId;

    /**
     * 支付公钥
     */
    private String publicKey;

    /**
     * 商户私钥
     */
    private String merchantPrivateKey;

    /**
     * 其他配置
     */
    private String otherConfig;

    /**
     * AES混淆密钥
     */
    private String encryptKey;

    /**
     * 说明
     */
    private String remark;

    /**
     * 回调地址
     */
    private String notifyUrl;

    /**
     * 是否有效
     */
    private String enableFlag;

    /**
     * 商户ID
     */
    private Long enterpriseId;

   @ApiModelProperty(example = "2022-08-15 00:00:00", dataType = "string")
    @TableField(fill = FieldFill.INSERT) //MP自动填充
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;

   @ApiModelProperty(example = "2022-08-15 00:00:00", dataType = "string")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updated;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}