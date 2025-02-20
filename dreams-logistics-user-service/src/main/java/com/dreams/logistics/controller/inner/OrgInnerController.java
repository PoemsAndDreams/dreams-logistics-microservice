package com.dreams.logistics.controller.inner;

import com.dreams.logistics.model.entity.Organization;
import com.dreams.logistics.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户接口
 */
@RestController
@RequestMapping("/org/inner")
@Slf4j
public class OrgInnerController implements OrgFeignClient {


    @Resource
    private OrganizationService organizationService;

    @Override
    @PostMapping("/getOrganizationById")
    public Organization getOrganizationById(@RequestParam("id") String id){
        if (StringUtils.isNotEmpty(id)){
            return organizationService.getById(id);
        }
        return null;
    }

    @Override
    @PostMapping("/queryByIds")
    public List<Organization> queryByIds(@RequestParam("nextAgencyIds") List<Long> nextAgencyIds){
        return organizationService.listByIds(nextAgencyIds);

    }

}
