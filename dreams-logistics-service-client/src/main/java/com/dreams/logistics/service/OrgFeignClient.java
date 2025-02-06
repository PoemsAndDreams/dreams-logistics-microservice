package com.dreams.logistics.service;

import com.dreams.logistics.enums.UserRoleEnum;
import com.dreams.logistics.model.entity.DcUser;
import com.dreams.logistics.model.entity.Menu;
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
 * 服务
 *
 */
@FeignClient(name = "dreams-logistics-user-service-org",path = "/api/user/org/inner")
public interface OrgFeignClient {

    @PostMapping("/getOrganizationById")
    Organization getOrganizationById(@RequestParam("id") String id);

    @PostMapping("/queryByIds")
    List<Organization> queryByIds(@RequestParam("nextAgencyIds") List<Long> nextAgencyIds);
}


