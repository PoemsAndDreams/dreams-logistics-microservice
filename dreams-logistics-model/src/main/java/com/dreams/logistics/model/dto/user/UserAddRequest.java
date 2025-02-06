package com.dreams.logistics.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户创建请求
 */
@Data
public class UserAddRequest implements Serializable {

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 所属机构id
     */
    private Long orgId;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 用户角色
     */
    private String userRole;

    /**
     * 密码
     */
    private String userPassword;

    private static final long serialVersionUID = 1L;
}