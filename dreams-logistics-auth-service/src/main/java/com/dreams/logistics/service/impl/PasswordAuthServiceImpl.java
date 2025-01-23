package com.dreams.logistics.service.impl;

import com.dreams.logistics.common.ErrorCode;
import com.dreams.logistics.exception.BusinessException;
import com.dreams.logistics.model.dto.user.AuthParamsDto;
import com.dreams.logistics.model.dto.user.DcUserExt;
import com.dreams.logistics.model.entity.DcUser;
import com.dreams.logistics.service.AuthService;
import com.dreams.logistics.service.UserFeignClient;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 账号名密码方式
 */
@Service("password_authService")
public class PasswordAuthServiceImpl implements AuthService {

    @Autowired
    UserFeignClient userFeignClient;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserFeignClient checkCodeClient;

    @Override
    public DcUserExt execute(AuthParamsDto authParamsDto) {
        //账号
        String userAccount = authParamsDto.getUserAccount();

        //输入的验证码
        String checkcode = authParamsDto.getCheckCode();
        //验证码对应的key
        String checkcodekey = authParamsDto.getCheckCodeKey();

        if (StringUtils.isEmpty(checkcode) || StringUtils.isEmpty(checkcodekey)) {
            throw new BusinessException(ErrorCode.CHECK_CODE_ERROR,"请输入正确的验证码");
        }

        //远程调用验证码服务接口去校验验证码
        Boolean verify = checkCodeClient.verify(checkcodekey, checkcode);
        if (verify == null || !verify) {
            throw new BusinessException(ErrorCode.CHECK_CODE_ERROR,"验证码输入错误");
        }


        //账号是否存在
        //根据username账号查询数据库
        DcUser dcUser = userFeignClient.getOne(userAccount);

        //查询到用户不存在，要返回null即可，spring security框架抛出异常用户不存在
        if (dcUser == null) {
            throw new RuntimeException("账号不存在");
        }

        //验证密码是否正确
        //如果查到了用户拿到正确的密码
        String passwordDb = dcUser.getUserPassword();
        //拿 到用户输入的密码
        String passwordForm = authParamsDto.getUserPassword();
        //校验密码
        boolean matches = passwordEncoder.matches(passwordForm, passwordDb);
        if (!matches) {
            throw new RuntimeException("账号或密码错误");
        }
        DcUserExt dcUserExt = new DcUserExt();
        BeanUtils.copyProperties(dcUser, dcUserExt);

        return dcUserExt;
    }
}
