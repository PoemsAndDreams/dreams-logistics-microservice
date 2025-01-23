package com.dreams.logistics.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 运费模板表
 * @TableName carriage
 */
@TableName(value ="carriage")
@Data
public class Carriage implements Serializable {
    /**
     * 运费模板id
     */
    @TableId
    private Long id;

    /**
     * 模板类型，1-同城寄 2-省内寄 3-经济区互寄 4-跨省
     */
    private String templateType;

    /**
     * 运送类型，1-普快 2-特快
     */
    private String transportType;

    /**
     * 关联城市，1-全国 2-京津冀 3-江浙沪 4-川渝 5-黑吉辽
     */
    private String associatedCity;

    /**
     * 首重价格
     */
    private Double firstWeight;

    /**
     * 续重价格
     */
    private Double continuousWeight;

    /**
     * 轻抛系数
     */
    private Integer lightThrowingCoefficient;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}