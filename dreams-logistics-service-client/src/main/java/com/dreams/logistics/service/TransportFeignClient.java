package com.dreams.logistics.service;

import com.dreams.logistics.enums.UserRoleEnum;
import com.dreams.logistics.model.dto.workSchedule.WorkScheduleAddRequest;
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
 * 用户服务
 *
 */
@FeignClient(name = "dreams-logistics-transport-service",path = "/api/transport/inner")
public interface TransportFeignClient {

    @PostMapping("/add")
    Boolean add(@RequestBody WorkScheduleAddRequest workScheduleAddRequest);

}


