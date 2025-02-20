package com.dreams.logistics.controller;

import com.dreams.logistics.common.BaseResponse;
import com.dreams.logistics.common.ResultUtils;
import com.dreams.logistics.enums.ServiceTypeEnum;
import com.dreams.logistics.model.dto.serviceScope.ServiceScopeDTO;
import com.dreams.logistics.model.entity.ServiceScope;
import com.dreams.logistics.service.ScopeService;
import com.dreams.logistics.utils.EntityUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 服务范围
 */
@Api(tags = "服务范围")
@RestController
@RequestMapping("scopes")
@Validated
public class ScopeController {

    @Resource
    private ScopeService scopeService;

    /**
     * 新增或更新服务服务范围
     */
    @PostMapping("/add")
    public BaseResponse<Boolean> saveScope(@RequestBody ServiceScopeDTO serviceScopeDTO) {
        ServiceScope serviceScopeEntity = EntityUtils.toEntity(serviceScopeDTO);
        Long bid = serviceScopeEntity.getBid();
        ServiceTypeEnum type = ServiceTypeEnum.codeOf(serviceScopeEntity.getType());
        Boolean result = this.scopeService.saveOrUpdate(bid, type, serviceScopeEntity.getPolygon());
        return ResultUtils.success(result);
    }

    /**
     * 删除服务范围
     */
    @DeleteMapping("{bid}/{type}")
    public BaseResponse<Boolean> delete(@PathVariable("bid") Long bid,
                                       @PathVariable("type") Integer type) {
        Boolean result = this.scopeService.delete(bid, ServiceTypeEnum.codeOf(type));
        return ResultUtils.success(result);
    }

    /**
     * 查询服务范围
     */
    @ApiOperation(value = "查询", notes = "查询服务范围")
    @GetMapping("{bid}/{type}")
    public BaseResponse<ServiceScopeDTO> queryServiceScope(@PathVariable("bid") Long bid,
                                                             @PathVariable("type") Integer type) {
        ServiceScope serviceScope= this.scopeService.queryByBidAndType(bid, ServiceTypeEnum.codeOf(type));
        return ResultUtils.success(EntityUtils.toDTO(serviceScope));
    }

    /**
     * 地址查询服务范围
     */
    @GetMapping("address")
    public BaseResponse<List<ServiceScopeDTO>> queryListByAddress(@RequestParam("type") Integer type,
                                                                    @RequestParam("address") String address) {
        List<ServiceScope> serviceScopeEntityList = this.scopeService.queryListByPoint(ServiceTypeEnum.codeOf(type), address);
        return ResultUtils.success(EntityUtils.toDTOList(serviceScopeEntityList));
    }

    /**
     * 位置查询服务范围
     */
    @GetMapping("location")
    public BaseResponse<List<ServiceScopeDTO>> queryListByAddress(@RequestParam("type") Integer type,
                                                                    @RequestParam("longitude") Double longitude,
                                                                    @RequestParam("latitude") Double latitude) {
        List<ServiceScope> serviceScopeEntityList = this.scopeService.queryListByPoint(ServiceTypeEnum.codeOf(type), new GeoJsonPoint(longitude, latitude));
        return ResultUtils.success(EntityUtils.toDTOList(serviceScopeEntityList));
    }
}
