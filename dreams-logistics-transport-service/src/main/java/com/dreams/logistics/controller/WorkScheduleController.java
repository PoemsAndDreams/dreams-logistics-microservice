package com.dreams.logistics.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreams.logistics.common.BaseResponse;
import com.dreams.logistics.common.DeleteRequest;
import com.dreams.logistics.common.ErrorCode;
import com.dreams.logistics.common.ResultUtils;
import com.dreams.logistics.exception.BusinessException;
import com.dreams.logistics.exception.ThrowUtils;
import com.dreams.logistics.model.dto.workSchedule.WorkScheduleAddRequest;
import com.dreams.logistics.model.dto.workSchedule.WorkScheduleQueryRequest;
import com.dreams.logistics.model.dto.workSchedule.WorkScheduleUpdateRequest;
import com.dreams.logistics.model.entity.WorkSchedule;
import com.dreams.logistics.model.vo.WorkScheduleVO;
import com.dreams.logistics.service.WorkScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 排班接口
 */
@RestController
@RequestMapping("/workSchedule")
@Slf4j
public class WorkScheduleController {

    @Resource
    private WorkScheduleService workScheduleService;


    /**
     * 创建排班
     *
     * @param workScheduleAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
//    @PreAuthorize("hasAuthority('admin_workSchedule_add')")
    public BaseResponse<Boolean> addWorkSchedule(@RequestBody WorkScheduleAddRequest workScheduleAddRequest, HttpServletRequest request) {
        if (workScheduleAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(workScheduleService.saveWorkSchedule(workScheduleAddRequest));
    }

    /**
     * 删除排班
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteWorkSchedule(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = workScheduleService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新排班
     *
     * @param workScheduleUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateWorkSchedule(@RequestBody WorkScheduleUpdateRequest workScheduleUpdateRequest,
            HttpServletRequest request) {

        boolean result = workScheduleService.updateWorkSchedule(workScheduleUpdateRequest);

        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取排班
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get/userId")
    public BaseResponse<WorkScheduleVO> getWorkScheduleByUserId(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        WorkScheduleVO workSchedule = workScheduleService.getWorkSchedule(id);
        ThrowUtils.throwIf(workSchedule == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(workSchedule);
    }

    /**
     * 根据 id 获取排班
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<WorkSchedule> getWorkScheduleById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        WorkSchedule workSchedule = workScheduleService.getById(id);
        ThrowUtils.throwIf(workSchedule == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(workSchedule);
    }


    /**
     * 分页获取排班列表
     *
     * @param workScheduleQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<WorkScheduleVO>> listWorkScheduleByPage(@RequestBody WorkScheduleQueryRequest workScheduleQueryRequest,
                                                     HttpServletRequest request) {
        long current = workScheduleQueryRequest.getCurrent();
        long size = workScheduleQueryRequest.getPageSize();
        Page<WorkSchedule> workSchedulePage = workScheduleService.page(new Page<>(current, size),
                workScheduleService.getQueryWrapper(workScheduleQueryRequest));
        Page<WorkScheduleVO> workScheduleVOPage = new Page<>(current, size, workSchedulePage.getTotal());
        List<WorkScheduleVO> workScheduleVO = workScheduleService.getWorkScheduleVO(workSchedulePage.getRecords());
        workScheduleVOPage.setRecords(workScheduleVO);
        return ResultUtils.success(workScheduleVOPage);
    }


}
