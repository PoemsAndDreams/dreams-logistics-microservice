package com.dreams.logistics.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreams.logistics.common.BaseResponse;
import com.dreams.logistics.common.DeleteRequest;
import com.dreams.logistics.common.ErrorCode;
import com.dreams.logistics.common.ResultUtils;
import com.dreams.logistics.exception.BusinessException;
import com.dreams.logistics.exception.ThrowUtils;
import com.dreams.logistics.model.dto.truckPlan.TransportTripsTruckDriverDto;
import com.dreams.logistics.model.dto.truckTrips.BatchSaveTruckDriver;
import com.dreams.logistics.model.dto.truckTrips.TruckTripsAddRequest;
import com.dreams.logistics.model.dto.truckTrips.TruckTripsQueryRequest;
import com.dreams.logistics.model.dto.truckTrips.TruckTripsUpdateRequest;
import com.dreams.logistics.model.entity.TruckTrips;
import com.dreams.logistics.model.entity.TruckTripsTruckDriver;
import com.dreams.logistics.model.vo.TruckTripsVO;
import com.dreams.logistics.service.TruckTripsService;
import com.dreams.logistics.service.TruckTripsTruckDriverService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户接口
 */
@RestController
@RequestMapping("/truckTrips")
@Slf4j
public class TruckTripsController {

    @Resource
    private TruckTripsService truckTripsService;

    @Resource
    private TruckTripsTruckDriverService truckTripsTruckDriverService;


    /**
     * 创建用户
     *
     * @param truckTripsAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
//    @PreAuthorize("hasAuthority('admin_truckTrips_add')")
    public BaseResponse<Boolean> addTruckTrips(@RequestBody TruckTripsAddRequest truckTripsAddRequest, HttpServletRequest request) {
        if (truckTripsAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(truckTripsService.saveTruckTrips(truckTripsAddRequest));
    }

    /**
     * 删除用户
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTruckTrips(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = truckTripsService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新用户
     *
     * @param truckTripsUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateTruckTrips(@RequestBody TruckTripsUpdateRequest truckTripsUpdateRequest,
            HttpServletRequest request) {
        if (truckTripsUpdateRequest == null || truckTripsUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        boolean result = truckTripsService.updateTruckTrips(truckTripsUpdateRequest);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取用户
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<TruckTripsVO> getTruckTripsById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        TruckTripsVO truckTrips = truckTripsService.getTruckTrips(id);
        ThrowUtils.throwIf(truckTrips == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(truckTrips);
    }


    /**
     * 分页获取用户列表
     *
     * @param truckTripsQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<TruckTripsVO>> listTruckTripsByPage(@RequestBody TruckTripsQueryRequest truckTripsQueryRequest,
                                                     HttpServletRequest request) {
        long current = truckTripsQueryRequest.getCurrent();
        long size = truckTripsQueryRequest.getPageSize();
        Page<TruckTrips> truckTripsPage = truckTripsService.page(new Page<>(current, size),
                truckTripsService.getQueryWrapper(truckTripsQueryRequest));
        Page<TruckTripsVO> truckTripsVOPage = new Page<>(current, size, truckTripsPage.getTotal());
        List<TruckTripsVO> truckTripsVO = truckTripsService.getTruckTripsVO(truckTripsPage.getRecords());
        truckTripsVOPage.setRecords(truckTripsVO);
        return ResultUtils.success(truckTripsVOPage);
    }


    /**
     * 批量保存车次与车辆和司机关联关系
     */
    @PostMapping("/truckDriver")
    public BaseResponse<Boolean> batchSaveTruckDriver(@RequestBody BatchSaveTruckDriver batchSaveTruckDriver) {
        truckTripsTruckDriverService.batchSave(batchSaveTruckDriver.getTransportTripsId(),batchSaveTruckDriver.getLicensePlateList());
        return ResultUtils.success(true);
    }

    /**
     * 获取车次与车辆车牌
     *
     * @param transportTripsId 车次id
     * @return 获取车次与车辆车牌
     */
    @GetMapping("/find/truckDriver/licensePlateList")
    public BaseResponse<List<String>> findLicensePlateListDriverTransportTrips(@RequestParam(name = "transportTripsId") String transportTripsId) {
        return ResultUtils.success(truckTripsTruckDriverService.findTruckDriverLicensePlateList(transportTripsId));
    }

    /**
     * 获取车次与车辆和司机关联关系列表
     *
     * @param transportTripsId 车次id
     * @param truckId          车辆id
     * @param driverId           司机id
     * @return 车次与车辆和司机关联关系列表
     */
    @GetMapping("/find/truckDriver")
    public BaseResponse<List<TransportTripsTruckDriverDto>> findAllTruckDriverTransportTrips(@RequestParam(name = "transportTripsId", required = false) Long transportTripsId,
                                                                               @RequestParam(name = "truckId", required = false) Long truckId,
                                                                               @RequestParam(name = "driverId", required = false) Long driverId) {
        return ResultUtils.success(truckTripsTruckDriverService.findAll(transportTripsId, truckId, driverId)
                .parallelStream()
                .map(transportTripsTruck -> BeanUtil.toBean(transportTripsTruck, TransportTripsTruckDriverDto.class))
                .collect(Collectors.toList()));
    }


}
