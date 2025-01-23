package com.dreams.logistics.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户视图（脱敏）
 */
@Data
public class AllUserVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    private static final long serialVersionUID = 1L;
}