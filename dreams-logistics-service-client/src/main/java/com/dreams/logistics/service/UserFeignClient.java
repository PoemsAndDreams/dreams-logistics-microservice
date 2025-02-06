package com.dreams.logistics.service;

import com.dreams.logistics.model.entity.DcUser;
import com.dreams.logistics.model.entity.Menu;
import com.dreams.logistics.enums.UserRoleEnum;
import com.dreams.logistics.model.entity.Organization;
import com.dreams.logistics.model.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;

import static com.dreams.logistics.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务
 *
 */
@FeignClient(name = "dreams-logistics-user-service-use",path = "/api/user/user/inner")
public interface UserFeignClient {


    /**
     * 根据用户获取id
     * @param userId
     * @return
     */
    @GetMapping("/get/id")
    DcUser getById(@RequestParam("userId") long userId);

    /**
     * 根据id获取用户列表
     * @param idList
     * @return
     */
    @GetMapping("/get/ids")
    List<DcUser> listByIds(@RequestParam("idList") Collection<Long> idList);

    /**
     * 获取当前登录用户
     *
     * @return
     */
    @GetMapping("/get/loginUser")
    DcUser getLoginUser();


    /**
     * 是否为管理员
     *
     * @param dcUser
     * @return
     */
    default boolean isAdmin(DcUser dcUser){
        return dcUser != null && UserRoleEnum.ADMIN.getValue().equals(dcUser.getUserRole());
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    default boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        DcUser dcUser = (DcUser) userObj;
        return isAdmin(dcUser);
    }

    /**
     * 获取脱敏的用户信息
     *
     * @param dcUser
     * @return
     */
    default UserVO getUserVO(DcUser dcUser){
        if (dcUser == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(dcUser, userVO);
        return userVO;
    }

//    /**
//     * 获取查询条件
//     *
//     * @param userQueryRequest
//     * @return
//     */
//    @PostMapping ("/query/wrapper")
//    QueryWrapper<DcUser> getQueryWrapper(@RequestBody UserQueryRequest userQueryRequest);
//


    /**
     * 获取所有用户
     * @return
     */
    @GetMapping("/get/list")
    List<DcUser> list();

    @GetMapping("/get/count")
    Long count();

    @PostMapping(value = "/checkCode/verify/code")
    public Boolean verify(@RequestParam("key") String key, @RequestParam("code") String code);

    @PostMapping("/getOne")
    DcUser getOne(@RequestParam("userAccount") String userAccount);


    @PostMapping("/selectPermission")
    List<Menu> selectPermissionByUserId(@RequestParam("userId") String userId);


    @PostMapping("/addUser")
    Boolean addUser(@RequestBody DcUser user);
}


