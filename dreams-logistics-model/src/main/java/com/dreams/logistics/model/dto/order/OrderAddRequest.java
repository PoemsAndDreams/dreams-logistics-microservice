package com.dreams.logistics.model.dto.order;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 订单
 * @TableName order
 */
@Data
public class OrderAddRequest implements Serializable {

    /**
     * 货物名称
     */
    private String name;

    /**
     * 货品体积
     */
    private BigDecimal volume;

    /**
     * 货品重量
     */
    private BigDecimal weight;

    /**
     * 金额
     */
    private BigDecimal amount;


    /**
     * 收件人省区县id
     */
    private List<Long> receiverAddressId;

    /**
     * 收件人详细地址
     */
    private String receiverAddress;

    /**
     * 收件人姓名
     */
    private String receiverName;

    /**
     * 收件人电话
     */
    private String receiverPhone;

    /**
     * 发件人省区县id
     */
    private List<Long> senderAddressId;

    /**
     * 发件人详细地址
     */
    private String senderAddress;

    /**
     * 发件人姓名
     */
    private String senderName;

    /**
     * 发件人电话
     */
    private String senderPhone;

    /**
     * 备注
     */
    private String mark;


    /**
     * 预计取件开始到达时间
     */
//    @ApiModelProperty(example = "2022-08-15 00:00:00", dataType = "string")
//    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private List<Date> pickupTimeRange;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}