package com.dreams.logistics.service.impl;

import com.alibaba.fastjson.JSON;
import com.dreams.logistics.model.dto.user.AuthParamsDto;
import com.dreams.logistics.model.dto.user.DcUserExt;
import com.dreams.logistics.model.entity.Menu;
import com.dreams.logistics.service.AuthService;
import com.dreams.logistics.service.UserFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author PoemsAndDreams
 * @date 2025-01-16 18:57
 * @description //TODO
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    ApplicationContext applicationContext;

    @Resource
    private UserFeignClient userFeignClient;

    @Override
    public UserDetails loadUserByUsername(String authParams) throws UsernameNotFoundException {


        AuthParamsDto authParamsDto = null;
        try {
            //将认证参数转为AuthParamsDto类型
            authParamsDto = JSON.parseObject(authParams, AuthParamsDto.class);
        } catch (Exception e) {
            // todo 日志
            throw new RuntimeException("认证请求数据格式不对");
        }
        //认证方法
        String authType = authParamsDto.getAuthType();
        AuthService authService =  applicationContext.getBean(authType + "_authService",AuthService.class);

        DcUserExt user = authService.execute(authParamsDto);

        return getUserPrincipal(user);
    }

    private UserDetails getUserPrincipal(DcUserExt user) {
        //取出数据库存储的正确密码
        String password  =user.getUserPassword();

        //用户权限,如果不加报Cannot pass a null GrantedAuthority collection
        String[] authorities= {"test"};

        //搜索 数据库 拿到 该用户对应角色拥有的权限菜单
        //......
        //根据用户id查询用户的权限
        List<Menu> menus = userFeignClient.selectPermissionByUserId(user.getId().toString());


        if(!Objects.isNull(menus)){
            List<String> permissions =new ArrayList<>();
            menus.forEach(m->{
                //拿到了用户拥有的权限标识符
                permissions.add(m.getCode());
            });
            //将permissions转成数组
            authorities = permissions.toArray(new String[0]);
        }

        //为了安全在令牌中不放密码
        user.setUserPassword(null);
        //将user对象转json
        String userString = JSON.toJSONString(user);
        //创建UserDetails对象
        UserDetails userDetails = User.withUsername(userString).password(password).authorities(authorities).build();

        return userDetails;
    }
}
