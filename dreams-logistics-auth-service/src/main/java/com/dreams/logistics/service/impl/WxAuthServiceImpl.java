//package com.dreams.logistics.service.impl;
//
//import com.alibaba.fastjson.JSON;
//import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
//import com.dreams.logistics.model.dto.user.AuthParamsDto;
//import com.dreams.logistics.model.dto.user.DcUserExt;
//import com.dreams.logistics.model.entity.DcUser;
//import com.dreams.logistics.service.AuthService;
//import org.springframework.beans.BeanUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.client.RestTemplate;
//
//import java.nio.charset.StandardCharsets;
//import java.time.LocalDateTime;
//import java.util.Map;
//import java.util.UUID;
//
///**
// * 微信扫码认证
// */
//@Service("wx_authservice")
//public class WxAuthServiceImpl implements AuthService, WxAuthService {
//
//    @Autowired
//    UserMapper userMapper;
//
//    @Autowired
//    UserRoleMapper userRoleMapper;
//
//    @Autowired
//    WxAuthServiceImpl currentPorxy;
//
//    @Autowired
//    RestTemplate restTemplate;
//
//
//    @Value("${weixin.appid}")
//    String appid;
//    @Value("${weixin.secret}")
//    String secret;
//
//    @Override
//    public DcUserExt execute(AuthParamsDto authParamsDto) {
//        //得到账号
//        String userAccount = authParamsDto.getUserAccount();
//        //查询数据库
//        DcUser dcUser = userMapper.selectOne(new LambdaQueryWrapper<DcUser>().eq(DcUser::getUserAccount, userAccount));
//        if(dcUser == null){
//            throw new RuntimeException("用户不存在");
//        }
//
//        DcUserExt dcUserExt = new DcUserExt();
//        BeanUtils.copyProperties(dcUser, dcUserExt);
//        return dcUserExt;
//    }
//
//    @Override
//    public DcUser wxAuth(String code) {
//        //申请令牌
//        Map<String, String> access_token_map = getAccess_token(code);
//        //访问令牌
//        String access_token = access_token_map.get("access_token");
//        String openid = access_token_map.get("openid");
//
//        //携带令牌查询用户信息
//        Map<String, String> userinfo = getUserinfo(access_token, openid);
//
//        // 保存用户信息到数据库
//        DcUser dcUser = currentPorxy.addWxUser(userinfo);
//
//
//        return dcUser;
//    }
//
//    @Transactional
//    public DcUser addWxUser(Map<String,String> userInfo_map){
//        String unionid = userInfo_map.get("unionid");
//        String nickname = userInfo_map.get("nickname");
//        //根据unionid查询用户信息
//        DcUser dcUser = userMapper.selectOne(new LambdaQueryWrapper<DcUser>().eq(DcUser::getWxUnionid, unionid));
//        if(dcUser !=null){
//            return dcUser;
//        }
//        //向数据库新增记录
//        dcUser = new DcUser();
//        String userId= UUID.randomUUID().toString();
//        dcUser.setId(userId);//主键
//        dcUser.setUsername(unionid);
//        dcUser.setPassword(unionid);
//        dcUser.setWxUnionid(unionid);
//        dcUser.setNickname(nickname);
//        dcUser.setName(nickname);
//        dcUser.setUtype("101001");//学生类型
//        dcUser.setStatus("1");//用户状态
//        dcUser.setCreateTime(LocalDateTime.now());
//        //插入
//        int insert = userMapper.insert(dcUser);
//
//        //向用户角色关系表新增记录
//        DcUserRole dcUserRole = new DcUserRole();
//        dcUserRole.setId(UUID.randomUUID().toString());
//        dcUserRole.setUserId(userId);
//        dcUserRole.setRoleId("17");//学生角色
//        dcUserRole.setCreateTime(LocalDateTime.now());
//        dcUserRoleMapper.insert(dcUserRole);
//        return dcUser;
//
//    }
//
//    /**
//     * 携带授权码申请令牌
//     * https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code
//     *
//     * {
//     * "access_token":"ACCESS_TOKEN",
//     * "expires_in":7200,
//     * "refresh_token":"REFRESH_TOKEN",
//     * "openid":"OPENID",
//     * "scope":"SCOPE",
//     * "unionid": "o6_bmasdasdsad6_2sgVt7hMZOPfL"
//     * }
//     * @param code 授权
//     * @return
//     */
//    private Map<String,String> getAccess_token(String code){
//
//        String url_template = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
//        //最终的请求路径
//        String url = String.format(url_template, appid, secret, code);
//
//        //远程调用此url
//        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.POST, null, String.class);
//        //获取响应的结果
//        String result = exchange.getBody();
//        //将result转成map
//        Map<String,String> map = JSON.parseObject(result, Map.class);
//        return map;
//
//
//    }
//
//    /**
//     * 携带令牌查询用户信息
//     *
//     * https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID
//     *
//     * {
//     * "openid":"OPENID",
//     * "nickname":"NICKNAME",
//     * "sex":1,
//     * "province":"PROVINCE",
//     * "city":"CITY",
//     * "country":"COUNTRY",
//     * "headimgurl": "https://thirdwx.qlogo.cn/mmopen/g3MonUZtNHkdmzicIlibx6iaFqAc56vxLSUfpb6n5WKSYVY0ChQKkiaJSgQ1dZuTOgvLLrhJbERQQ4eMsv84eavHiaiceqxibJxCfHe/0",
//     * "privilege":[
//     * "PRIVILEGE1",
//     * "PRIVILEGE2"
//     * ],
//     * "unionid": " o6_bmasdasdsad6_2sgVt7hMZOPfL"
//     *
//     * }
//     * @param access_token
//     * @param openid
//     * @return
//     */
//    private Map<String,String> getUserinfo(String access_token,String openid){
//
//        String url_template = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";
//        String url = String.format(url_template, access_token, openid);
//
//        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
//
//        //获取响应的结果
//        String result = new String(exchange.getBody().getBytes(StandardCharsets.ISO_8859_1),StandardCharsets.UTF_8);
//        //将result转成map
//        Map<String,String> map = JSON.parseObject(result, Map.class);
//        return map;
//
//    }
//}
