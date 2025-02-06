package com.dreams.logistics.controller.inner;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dreams.logistics.mapper.MenuMapper;
import com.dreams.logistics.model.entity.DcUser;
import com.dreams.logistics.model.entity.Menu;
import com.dreams.logistics.model.entity.Organization;
import com.dreams.logistics.service.CheckCodeService;
import com.dreams.logistics.service.OrganizationService;
import com.dreams.logistics.service.UserFeignClient;
import com.dreams.logistics.service.UserService;
import com.dreams.logistics.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

/**
 * 用户接口
 */
@RestController
@RequestMapping("/user/inner")
@Slf4j
public class UserInnerController implements UserFeignClient {

    @Resource
    private UserService userService;

    @Resource
    private MenuMapper menuMapper;

    @Resource
    private OrganizationService organizationService;

    @Resource(name = "PicCheckCodeService")
    private CheckCodeService picCheckCodeService;

    @Override
    @GetMapping("/get/id")
    public DcUser getById(long userId) {
        DcUser dcUser = userService.getById(userId);
        return dcUser;
    }

    @Override
    @GetMapping("/get/ids")
    public List<DcUser> listByIds(Collection<Long> idList) {
        List<DcUser> dcUsers = userService.listByIds(idList);
        return dcUsers;
    }

    @Override
    @GetMapping("/get/loginUser")
    public DcUser getLoginUser() {
        return SecurityUtil.getUser();
    }

    @Override
    @GetMapping("/get/list")
    public List<DcUser> list() {
        return userService.list();
    }

    @Override
    public Long count() {
        return userService.count();

    }


    @Override
    @PostMapping(value = "/checkCode/verify/code")
    public Boolean verify(@RequestParam("key") String key, @RequestParam("code") String code) {
        return picCheckCodeService.verify(key,code);
    }

    @Override
    @PostMapping("/getOne")
    public DcUser getOne(@RequestParam("userAccount") String userAccount) {
        LambdaQueryWrapper<DcUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DcUser::getUserAccount, userAccount);
        return userService.getOne(wrapper);
    }

    @Override
    @PostMapping("/selectPermission")
    public List<Menu> selectPermissionByUserId(@RequestParam("userId") String userId) {
        return menuMapper.selectPermissionByUserId(userId);
    }


    @Override
    @PostMapping("/addUser")
    public Boolean addUser(@RequestBody DcUser user) {
        return userService.save(user);
    }


}
