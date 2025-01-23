package com.dreams.logistics.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @description 安全管理配置
 */
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Autowired
    DaoAuthenticationProviderCustom daoAuthenticationProviderCustom;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(daoAuthenticationProviderCustom);
    }


//    //配置用户信息服务
//    @Bean
//    public UserDetailsService userDetailsService() {
//        //这里配置用户信息,这里暂时使用这种方式将用户存储在内存中
//        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
//        manager.createUser(User.withUsername("dreams").password("123").authorities("d1").build());
//        manager.createUser(User.withUsername("admin").password("123").authorities("d2").build());
//        return manager;
//    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        //密码为明文方式
        //return NoOpPasswordEncoder.getInstance();
        //
        return new BCryptPasswordEncoder();
    }

    //配置安全拦截机制
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
//                .csrf()
//                .ignoringAntMatchers("/user/**")  // 使用 ignoringAntMatchers 来禁用 /api/** 路径的 CSRF 保护
////                .ignoringAntMatchers("/checkCode/**")  // 使用 ignoringAntMatchers 来禁用 /api/** 路径的 CSRF 保护
//                .and()
                .authorizeRequests()
//                .antMatchers("/**").authenticated()//访问/r开始的请求需要认证通过
                .anyRequest().permitAll();//其它请求全部放行
    }


}
