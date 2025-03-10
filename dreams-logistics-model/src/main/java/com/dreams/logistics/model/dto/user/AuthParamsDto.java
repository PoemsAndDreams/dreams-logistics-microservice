package com.dreams.logistics.model.dto.user;

import lombok.Data;
import java.util.HashMap;
import java.util.Map;

@Data
public class AuthParamsDto {

    private String userAccount; //用户账户
    private String userPassword; //域  用于扩展
    private String cellphone;//手机号
    private String checkCode;//验证码
    private String checkCodeKey;//验证码key
    private String authType; // 认证的类型   password:用户名密码模式类型    sms:短信模式类型
    private Map<String, Object> payload = new HashMap<>();//附加数据，作为扩展，不同认证类型可拥有不同的附加数据。如认证类型为短信时包含smsKey : sms:3d21042d054548b08477142bbca95cfa; 所有情况下都包含clientId

}
