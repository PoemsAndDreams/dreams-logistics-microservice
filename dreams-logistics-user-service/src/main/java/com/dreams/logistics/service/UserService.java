package com.dreams.logistics.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dreams.logistics.model.dto.user.UserQueryRequest;
import com.dreams.logistics.model.dto.user.UserUpdateRequest;
import com.dreams.logistics.model.entity.DcUser;
import com.dreams.logistics.model.vo.LoginUserVO;
import com.dreams.logistics.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户服务
 */
public interface UserService extends IService<DcUser> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    DcUser getLoginUser(HttpServletRequest request);

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request
     * @return
     */
    DcUser getLoginUserPermitNull(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param dcUser
     * @return
     */
    boolean isAdmin(DcUser dcUser);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取脱敏的已登录用户信息
     *
     * @return
     */
    LoginUserVO getLoginUserVO(DcUser dcUser);

    /**
     * 获取脱敏的用户信息
     *
     * @param dcUser
     * @return
     */
    UserVO getUserVO(DcUser dcUser);

    /**
     * 获取脱敏的用户信息
     *
     * @param dcUserList
     * @return
     */
    List<UserVO> getUserVO(List<DcUser> dcUserList);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<DcUser> getQueryWrapper(UserQueryRequest userQueryRequest);

    DcUser saveUser(DcUser dcUser);

    boolean updateUser(UserUpdateRequest userUpdateRequest);
}
