package com.dreams.logistics.service;
import com.dreams.logistics.model.dto.user.AuthParamsDto;
import com.dreams.logistics.model.dto.user.DcUserExt;

/**
 * @description 统一的认证接口
 */
public interface AuthService {

 /**
  * @description 认证方法
  * @param authParamsDto 认证参数
  * @return com.dreams.logistics.model.dto.user.DcUser 用户信息
  */
 DcUserExt execute(AuthParamsDto authParamsDto);

}
