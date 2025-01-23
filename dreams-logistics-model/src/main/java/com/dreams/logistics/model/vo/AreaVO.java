package com.dreams.logistics.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dreams.logistics.model.entity.Menu;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 
 * @TableName area
 */
@Data
public class AreaVO implements Serializable {
    /**
     * 行政id
     */
    private String id;

    /**
     * 行政名称
     */
    private String name;


    List<AreaVO> children;


    private Integer level;


    private Boolean isLeaf ;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}