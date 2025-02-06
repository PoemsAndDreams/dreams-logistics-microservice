package com.dreams.logistics.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreams.logistics.common.BaseResponse;
import com.dreams.logistics.common.DeleteRequest;
import com.dreams.logistics.common.ErrorCode;
import com.dreams.logistics.common.ResultUtils;
import com.dreams.logistics.exception.BusinessException;
import com.dreams.logistics.exception.ThrowUtils;
import com.dreams.logistics.model.dto.truck.TruckAddRequest;
import com.dreams.logistics.model.dto.truck.TruckQueryRequest;
import com.dreams.logistics.model.dto.truck.TruckUpdateRequest;
import com.dreams.logistics.model.entity.DcUser;
import com.dreams.logistics.model.entity.Truck;
import com.dreams.logistics.model.vo.TruckVO;
import com.dreams.logistics.service.TruckDriverService;
import com.dreams.logistics.service.TruckService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 车辆接口
 */
@RestController
@RequestMapping("/truck")
@Slf4j
public class TruckController {

    @Resource
    private TruckService truckService;

    @Resource
    private TruckDriverService truckDriverService;

    /**
     * 创建车辆
     *
     * @param truckAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addTruck(@RequestBody TruckAddRequest truckAddRequest, HttpServletRequest request) {
        if (truckAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Truck truck = new Truck();
        BeanUtils.copyProperties(truckAddRequest, truck);

        truckService.save(truck);

        return ResultUtils.success(truck.getId());
    }

    /**
     * 删除车辆
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTruck(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = truckService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新车辆
     *
     * @param truckUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateTruck(@RequestBody TruckUpdateRequest truckUpdateRequest,
            HttpServletRequest request) {
        if (truckUpdateRequest == null || truckUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Truck truck = truckService.getById(truckUpdateRequest.getId());
        BeanUtils.copyProperties(truckUpdateRequest, truck);

        boolean result = truckService.updateById(truck);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取车辆
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Truck> getTruckById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Truck truck = truckService.getById(id);
        ThrowUtils.throwIf(truck == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(truck);
    }

    /**
     * 根据 id 获取包装类
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<TruckVO> getTruckVOById(long id, HttpServletRequest request) {
        BaseResponse<Truck> response = getTruckById(id, request);
        Truck truck = response.getData();
        return ResultUtils.success(truckService.getTruckVO(truck));
    }

    /**
     * 分页获取车辆封装列表
     *
     * @param truckQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<TruckVO>> listTruckVOByPage(@RequestBody TruckQueryRequest truckQueryRequest,
            HttpServletRequest request) {
        if (truckQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = truckQueryRequest.getCurrent();
        long size = truckQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Truck> truckPage = truckService.page(new Page<>(current, size),
                truckService.getQueryWrapper(truckQueryRequest));
        Page<TruckVO> truckVOPage = new Page<>(current, size, truckPage.getTotal());
        List<TruckVO> truckVO = truckService.getTruckVO(truckPage.getRecords());
        truckVOPage.setRecords(truckVO);
        return ResultUtils.success(truckVOPage);
    }

    /**
     * 根据当前车辆司机
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get/driver")
    public BaseResponse<List<DcUser>> getCarAllDriver(long id, HttpServletRequest request) {
        List<DcUser> list = truckDriverService.getCarAllDriver(id);
        return ResultUtils.success(list);
    }


    /**
     * 删除当前车辆司机
     *
     * @param id
     * @param request
     * @return
     */
    @PostMapping("/delete/driver")
    public BaseResponse<Boolean> deleteDriver(long id, long driverId,  HttpServletRequest request) {

        return ResultUtils.success(truckDriverService.deleteDriver(id,driverId));
    }


    /**
     * 添加当前车辆司机
     *
     * @param id
     * @param request
     * @return
     */
    @PostMapping("/add/driver")
    public BaseResponse<Boolean> addDriver(long id, long driverId,  HttpServletRequest request) {
        return ResultUtils.success(truckDriverService.addDriver(id,driverId));
    }

}
