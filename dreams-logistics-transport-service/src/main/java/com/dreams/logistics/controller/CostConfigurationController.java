package com.dreams.logistics.controller;

import com.dreams.logistics.common.BaseResponse;
import com.dreams.logistics.common.ResultUtils;
import com.dreams.logistics.model.dto.cost.CostConfiguration;
import com.dreams.logistics.service.CostConfigurationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 成本配置相关业务对外提供接口服务
 */
@RequestMapping("/cost")
@RestController
public class CostConfigurationController {
    @Resource
    private CostConfigurationService costConfigurationService;

    /**
     *     查询成本配置
     */
    @GetMapping
    public BaseResponse<List<CostConfiguration>> findConfiguration() {
        return ResultUtils.success(costConfigurationService.findConfiguration());
    }

    /**
     *     保存成本配置
     */
    @PostMapping
    public BaseResponse<Boolean> saveConfiguration(@RequestBody List<CostConfiguration> dto) {
        costConfigurationService.saveConfiguration(dto);
        return ResultUtils.success(true);
    }
}
