package com.dreams.logistics.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.dreams.logistics.common.BaseResponse;
import com.dreams.logistics.common.ErrorCode;
import com.dreams.logistics.common.ResultUtils;
import com.dreams.logistics.exception.BusinessException;
import com.dreams.logistics.model.dto.transportInfo.TransportInfoDTO;
import com.dreams.logistics.model.entity.TransportInfo;
import com.dreams.logistics.service.TransportInfoService;
import com.github.benmanes.caffeine.cache.Cache;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Api(tags = "物流信息")
@RestController
@RequestMapping("infos")
public class TransportInfoController {

    @Resource
    private TransportInfoService transportInfoService;

    /**
     * 根据运单id查询物流信息
     * @param orderId
     * @return
     */
    @GetMapping("/query")
    public BaseResponse<TransportInfoDTO> queryByTransportOrderId(@RequestParam("orderId") Long orderId) {
        TransportInfo transportInfo = this.transportInfoService.queryByOrderId(String.valueOf(orderId));
        return ResultUtils.success(BeanUtil.toBean(transportInfo, TransportInfoDTO.class));
    }

}
