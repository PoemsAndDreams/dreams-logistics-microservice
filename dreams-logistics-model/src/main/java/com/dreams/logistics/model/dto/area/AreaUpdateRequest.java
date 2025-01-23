package com.dreams.logistics.model.dto.area;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName area
 */
@Data
public class AreaUpdateRequest implements Serializable {
    /**
     * 行政id
     */
    private String id;

    /**
     * 父级行政
     */
    private Integer parentId;

    /**
     * 行政名称
     */
    private String name;

    /**
     * 
     */
    private String areaCode;

    /**
     * 
     */
    private String cityCode;

    /**
     * 
     */
    private String mergerName;

    /**
     * 
     */
    private String shortName;

    /**
     * 
     */
    private String zipCode;

    /**
     * 行政区域等级（0: 省级 1:市级 2:县级 3:镇级 4:乡村级）
     */
    private Integer level;

    /**
     * 
     */
    private String lng;

    /**
     * 
     */
    private String lat;

    /**
     * 拼音
     */
    private String pinyin;

    /**
     * 首字母
     */
    private String first;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}