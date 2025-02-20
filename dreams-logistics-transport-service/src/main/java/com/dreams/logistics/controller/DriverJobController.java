package com.dreams.logistics.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreams.logistics.common.BaseResponse;
import com.dreams.logistics.common.ErrorCode;
import com.dreams.logistics.common.ResultUtils;
import com.dreams.logistics.enums.DriverExceptionEnum;
import com.dreams.logistics.enums.DriverJobStatus;
import com.dreams.logistics.exception.BusinessException;
import com.dreams.logistics.exception.ThrowUtils;
import com.dreams.logistics.model.dto.driverJob.*;
import com.dreams.logistics.model.dto.driverJob.DriverJobQueryRequest;
import com.dreams.logistics.model.entity.DriverJob;
import com.dreams.logistics.model.entity.DriverJob;
import com.dreams.logistics.model.vo.DriverJobVO;
import com.dreams.logistics.service.DriverJobService;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 司机作业接口
 */
@RestController
@RequestMapping("/driverJob")
@Slf4j
public class DriverJobController {

    @Resource
    private DriverJobService driverJobService;

    /**
     * 更新司机作业状态，不允许 PENDING 状态，PROCESSING：出库业务，COMPLETED：入库业务
     * @param id
     * @param status
     * @return
     */
    @PutMapping
    public boolean updateStatus(@RequestParam("id") Long id,
                                @RequestParam("status") DriverJobStatus status) {
        return this.driverJobService.updateStatus(id, status);
    }

    /**
     * 根据id获取司机作业单信息
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public DriverJobDTO findById(@PathVariable("id") Long id) {
        DriverJob driverJob = this.driverJobService.getById(id);
        if (ObjectUtil.isEmpty(driverJob)) {
            throw new BusinessException(DriverExceptionEnum.DRIVER_JOB_NOT_FOUND);
        }
        return BeanUtil.toBean(driverJob, DriverJobDTO.class);
    }


    /**
     * 根据运输任务删除司机作业单
     * @param transportTaskId
     * @return
     */
    @DeleteMapping("removeByTransportTaskId/{transportTaskId}")
    public boolean removeByTransportTaskId(@PathVariable("transportTaskId") Long transportTaskId) {
        return driverJobService.removeByTransportTaskId(transportTaskId);
    }


    /**
     * 根据运输任务生成司机作业单
     * @param transportTaskId
     * @param driverId
     * @return
     */
    @PostMapping("createDriverJob/{transportTaskId}/{driverId}")
    public Long createDriverJob(@PathVariable("transportTaskId") Long transportTaskId, @PathVariable("driverId") Long driverId) {
        return driverJobService.createDriverJob(transportTaskId, driverId);
    }

    /**
     * 司机入库，修改运单的当前节点和下个节点 以及 修改运单为待调度状态，结束运输任务
     * @param driverDeliverDTO
     */
    @PostMapping("intoStorage")
    public void intoStorage(@RequestBody DriverDeliverDTO driverDeliverDTO) {
        driverJobService.intoStorage(driverDeliverDTO);
    }


    /**
     * 司机出库，修改运单为运输中状态，开始运输任务
     * @param driverPickUpDTO
     */
    @PostMapping("outStorage")
    public void outStorage(@RequestBody DriverPickUpDTO driverPickUpDTO) {
        driverJobService.outStorage(driverPickUpDTO);
    }


    /**
     * 回车登记
     * @param driverReturnRegisterDTO
     */
    @PostMapping("returnRegister")
    public void returnRegister(@RequestBody DriverReturnRegisterDTO driverReturnRegisterDTO) {
        driverJobService.returnRegister(driverReturnRegisterDTO);
    }


    /**
     * 分页获取封装列表
     *
     * @param driverJobQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<DriverJobVO>> listDriverJobVOByPage(@RequestBody DriverJobQueryRequest driverJobQueryRequest,
                                                       HttpServletRequest request) {
        if (driverJobQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = driverJobQueryRequest.getCurrent();
        long size = driverJobQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<DriverJob> driverJobPage = driverJobService.page(new Page<>(current, size),
                driverJobService.getQueryWrapper(driverJobQueryRequest));
        Page<DriverJobVO> driverJobVOPage = new Page<>(current, size, driverJobPage.getTotal());
        List<DriverJobVO> driverJobVO = driverJobService.getDriverJobVO(driverJobPage.getRecords());
        driverJobVOPage.setRecords(driverJobVO);
        return ResultUtils.success(driverJobVOPage);
    }

}
