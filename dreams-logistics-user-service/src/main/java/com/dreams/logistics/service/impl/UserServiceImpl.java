package com.dreams.logistics.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreams.logistics.common.ErrorCode;
import com.dreams.logistics.constant.CommonConstant;
import com.dreams.logistics.enums.WorkScheduleEnum;
import com.dreams.logistics.exception.BusinessException;
import com.dreams.logistics.exception.ThrowUtils;
import com.dreams.logistics.mapper.RoleMapper;
import com.dreams.logistics.mapper.UserMapper;
import com.dreams.logistics.model.dto.user.UserQueryRequest;
import com.dreams.logistics.model.dto.user.UserUpdateRequest;
import com.dreams.logistics.model.dto.workSchedule.WorkScheduleAddRequest;
import com.dreams.logistics.model.entity.*;
import com.dreams.logistics.enums.UserRoleEnum;
import com.dreams.logistics.model.vo.LoginUserVO;
import com.dreams.logistics.model.vo.UserVO;
import com.dreams.logistics.service.*;
import com.dreams.logistics.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.dreams.logistics.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, DcUser> implements UserService {

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private OrganizationService organizationService;


    @Resource
    private UserRoleService userRoleService;
    @Resource
    private RoleService roleService;

    @Resource
    private TransportFeignClient transportFeignClient;

    @Override
    public long userRegister(String userAccount, String userPassword) {

        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<DcUser> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_account", userAccount);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 2. 加密
            // 3. 插入数据
            DcUser dcUser = new DcUser();
            dcUser.setUserAccount(userAccount);
            dcUser.setUserPassword(userPassword);
//            dcUser.setUserPassword(userPassword);
            //暂时用户名为用户账号
            dcUser.setUserName(userAccount);
            boolean saveResult = this.save(dcUser);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return dcUser.getId();
        }
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
//        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<DcUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
//        queryWrapper.eq("userPassword", encryptPassword);
        DcUser dcUser = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (dcUser == null) {
            log.info("dcUser login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, dcUser);
        return this.getLoginUserVO(dcUser);
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public DcUser getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        DcUser currentDcUser = (DcUser) userObj;
        if (currentDcUser == null || currentDcUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentDcUser.getId();
        currentDcUser = this.getById(userId);
        if (currentDcUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentDcUser;
    }

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request
     * @return
     */
    @Override
    public DcUser getLoginUserPermitNull(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        DcUser currentDcUser = (DcUser) userObj;
        if (currentDcUser == null || currentDcUser.getId() == null) {
            return null;
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentDcUser.getId();
        return this.getById(userId);
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        DcUser dcUser = (DcUser) userObj;
        return isAdmin(dcUser);
    }

    @Override
    public boolean isAdmin(DcUser dcUser) {
        return dcUser != null && UserRoleEnum.ADMIN.getValue().equals(dcUser.getUserRole());
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public LoginUserVO getLoginUserVO(DcUser dcUser) {
        if (dcUser == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(dcUser, loginUserVO);
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(DcUser dcUser) {
        if (dcUser == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(dcUser, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVO(List<DcUser> dcUserList) {
        if (CollUtil.isEmpty(dcUserList)) {
            return new ArrayList<>();
        }
        return dcUserList.stream().map(dcUser -> {
            dcUser.setUserPassword(null);
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(dcUser, userVO);
            Organization organization = organizationService.getById(dcUser.getOrgId());
            userVO.setOrgName(organization.getName());
            return userVO;
        }).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<DcUser> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        Long orgId = userQueryRequest.getOrgId();
        String phone = userQueryRequest.getPhone();
        QueryWrapper<DcUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(orgId != null, "orgId", orgId);
        queryWrapper.eq(StringUtils.isNotBlank(userAccount), "user_account", userAccount);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "user_role", userRole);
        queryWrapper.eq(StringUtils.isNotBlank(phone), "phone", phone);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "user_role", userRole);
        queryWrapper.like(StringUtils.isNotBlank(userProfile), "user_profile", userProfile);
        queryWrapper.like(StringUtils.isNotBlank(userName), "user_name", userName);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public DcUser saveUser(DcUser dcUser) {
        // 默认密码 12345678
        String defaultPassword = "123456";
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encryptPassword = passwordEncoder.encode(defaultPassword);
        dcUser.setUserPassword(encryptPassword);

        Role role = roleMapper.selectById(dcUser.getUserRole());
        dcUser.setUserRole(role.getRoleCode());

        boolean result = this.save(dcUser);
        UserRole userRole = new UserRole();
        userRole.setUserId(dcUser.getId().toString());
        userRole.setRoleId(role.getId());
        userRoleService.save(userRole);
        if (result && (Objects.equals(dcUser.getUserRole(), UserRoleEnum.COURIER.getValue()) || Objects.equals(dcUser.getUserRole(), UserRoleEnum.DRIVER.getValue() ))){
            WorkScheduleAddRequest workScheduleAddRequest = new WorkScheduleAddRequest();
            workScheduleAddRequest.setUserId(dcUser.getId());
            workScheduleAddRequest.setWeekSchedule(WorkScheduleEnum.YES_STATUS.getStatus());
            transportFeignClient.add(workScheduleAddRequest);
        }
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return dcUser;
    }

    @Override
    public boolean updateUser(UserUpdateRequest userUpdateRequest) {
        if (StringUtils.isNotEmpty(userUpdateRequest.getUserPassword())){
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String encryptPassword = passwordEncoder.encode(userUpdateRequest.getUserPassword());
            userUpdateRequest.setUserPassword(encryptPassword);
        }
        DcUser dcUser = this.getById(userUpdateRequest.getId());

        if (Objects.equals(dcUser.getUserRole(), UserRoleEnum.USER.getValue()) && (Objects.equals(userUpdateRequest.getUserRole(), UserRoleEnum.COURIER.getValue()) || Objects.equals(userUpdateRequest.getUserRole(), UserRoleEnum.DRIVER.getValue() ))){
            WorkScheduleAddRequest workScheduleAddRequest = new WorkScheduleAddRequest();
            workScheduleAddRequest.setUserId(dcUser.getId());
            workScheduleAddRequest.setWeekSchedule(WorkScheduleEnum.YES_STATUS.getStatus());
            transportFeignClient.add(workScheduleAddRequest);
        }

        BeanUtils.copyProperties(userUpdateRequest, dcUser);

        Role role = roleService.getById(dcUser.getUserRole());
        if (!Objects.isNull(role)){
            dcUser.setUserRole(role.getRoleCode());
        }

        UserRole userRole = userRoleService.getByUserId(String.valueOf(dcUser.getId()));
        userRole.setUserId(dcUser.getId().toString());
        userRole.setRoleId(role.getId());
        userRoleService.updateById(userRole);

        return this.updateById(dcUser);
    }
}
