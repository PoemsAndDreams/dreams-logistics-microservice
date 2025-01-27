package com.dreams.logistics.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreams.logistics.common.BaseResponse;
import com.dreams.logistics.common.DeleteRequest;
import com.dreams.logistics.common.ErrorCode;
import com.dreams.logistics.common.ResultUtils;
import com.dreams.logistics.exception.BusinessException;
import com.dreams.logistics.exception.ThrowUtils;
import com.dreams.logistics.model.dto.carriage.CarriageAddRequest;
import com.dreams.logistics.model.dto.carriage.CarriageQueryRequest;
import com.dreams.logistics.model.dto.carriage.CarriageUpdateRequest;
import com.dreams.logistics.model.dto.carriage.WaybillDTO;
import com.dreams.logistics.model.entity.Carriage;
import com.dreams.logistics.model.vo.CarriageVO;
import com.dreams.logistics.service.CarriageService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 运费模板接口
 */
@RestController
@RequestMapping("/carriage")
@Slf4j
public class CarriageController {

    @Resource
    private CarriageService carriageService;
    /**
     * 创建运费模板
     *
     * @param carriageAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
//    @PreAuthorize("hasAuthority('admin_carriage_add')")
    public BaseResponse<Long> addCarriage(@RequestBody CarriageAddRequest carriageAddRequest, HttpServletRequest request) {
        if (carriageAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Carriage carriage = new Carriage();
        BeanUtils.copyProperties(carriageAddRequest, carriage);
        carriageService.saveOrUpdateCarriage(carriage);
        return ResultUtils.success(carriage.getId());
    }

    /**
     * 删除运费模板
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteCarriage(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = carriageService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新运费模板
     *
     * @param carriageUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateCarriage(@RequestBody CarriageUpdateRequest carriageUpdateRequest,
            HttpServletRequest request) {
        if (carriageUpdateRequest == null || carriageUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Carriage carriage = new Carriage();
        BeanUtils.copyProperties(carriageUpdateRequest, carriage);

        boolean result = carriageService.saveOrUpdateCarriage(carriage);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取运费模板
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Carriage> getCarriageById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Carriage carriage = carriageService.getById(id);
        ThrowUtils.throwIf(carriage == null, ErrorCode.NOT_FOUND_ERROR);

        return ResultUtils.success(carriage);
    }


    /**
     * 分页获取运费模板列表
     *
     * @param carriageQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<Carriage>> listCarriageByPage(@RequestBody CarriageQueryRequest carriageQueryRequest,
                                                     HttpServletRequest request) {
        long current = carriageQueryRequest.getCurrent();
        long size = carriageQueryRequest.getPageSize();
        Page<Carriage> carriagePage = carriageService.page(new Page<>(current, size),
                carriageService.getQueryWrapper(carriageQueryRequest));
        return ResultUtils.success(carriagePage);
    }

    /**
     * 获取全部运费模板列表
     * @return
     */
    @PostMapping("/list/all")
    public BaseResponse<List<Carriage>> listAllCarriageByPage() {

        List<Carriage> list = carriageService.list();
        return ResultUtils.success(list);
    }


    @PostMapping("/compute")
    public BaseResponse<CarriageVO> compute(@RequestBody WaybillDTO waybillDTO) {
        return ResultUtils.success(carriageService.compute(waybillDTO));
    }



}
