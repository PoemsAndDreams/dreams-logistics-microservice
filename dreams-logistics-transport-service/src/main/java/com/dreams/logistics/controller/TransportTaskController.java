package com.dreams.logistics.controller;

import com.dreams.logistics.common.BaseResponse;
import com.dreams.logistics.common.ResultUtils;
import com.dreams.logistics.enums.TransportTaskStatus;
import com.dreams.logistics.model.dto.transport.TransportTaskDTO;
import com.dreams.logistics.model.dto.transport.request.TransportTaskCompleteDTO;
import com.dreams.logistics.model.dto.transport.request.TransportTaskDelayDeliveryDTO;
import com.dreams.logistics.model.dto.transport.request.TransportTaskPageQueryDTO;
import com.dreams.logistics.model.dto.transport.request.TransportTaskStartDTO;
import com.dreams.logistics.model.dto.transport.response.TransportTaskMonthlyDistanceDTO;
import com.dreams.logistics.service.TransportTaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 运输任务表
 */
@RestController
@RequestMapping("transport-task")
public class TransportTaskController {

    @Resource
    private TransportTaskService transportTaskService;

    /**
     * 更新状态，不允许 CREATED 状态
     * @param id
     * @param status
     * @return
     */
    @PutMapping
    public BaseResponse<Boolean> updateStatus(@RequestParam("id") Long id,
                                @RequestParam("status") TransportTaskStatus status) {
        return ResultUtils.success(this.transportTaskService.updateStatus(id, status));
    }

//    /**
//     * 获取运输任务分页数据
//     *
//     * @return 运输任务分页数据
//     */
//    @PostMapping("page")
//    public BaseResponse<Page<TransportTaskDTO>> findByPage(@RequestBody TransportTaskPageQueryDTO pageQueryDTO) {
//        return ResultUtils.success(this.transportTaskService.findByPage(pageQueryDTO));
//    }

    /**
     * 根据id获取运输任务信息
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public BaseResponse<TransportTaskDTO> findById(@PathVariable("id") Long id) {
        return ResultUtils.success(transportTaskService.findById(id));
    }


    /**
     * 根据运单id或运输任务id获取运输任务列表
     * @param transportOrderId
     * @param taskTransportId
     * @return
     */
    @GetMapping("listByOrderIdOrTaskId")
    public BaseResponse<List<TransportTaskDTO>> findAllByOrderIdOrTaskId(@RequestParam(name = "transportOrderId", required = false) String transportOrderId,
                                                           @RequestParam(name = "taskTransportId", required = false) Long taskTransportId) {
        return ResultUtils.success(this.transportTaskService.findAllByOrderIdOrTaskId(transportOrderId, taskTransportId));
    }


    /**
     * 开始运输任务
     * @param transportTaskStartDTO
     */
    @PutMapping("startTransportTask")
    public BaseResponse<Boolean> startTransportTask(@RequestBody TransportTaskStartDTO transportTaskStartDTO) {
        transportTaskService.startTransportTask(transportTaskStartDTO);
        return ResultUtils.success(true);
    }

    /**
     * 完成运输任务
     * @param transportTaskCompleteDTO
     */
    @PutMapping("completeTransportTask")
    public BaseResponse<Boolean> completeTransportTask(@RequestBody TransportTaskCompleteDTO transportTaskCompleteDTO) {
        transportTaskService.completeTransportTask(transportTaskCompleteDTO);
        return ResultUtils.success(true);
    }

    /**
     * 根据运输任务id查询运单id列表
     * @param id
     * @return
     */
    @GetMapping("queryTransportOrderIdListById/{id}")
    public BaseResponse<List<String>> queryTransportOrderIdListById(@PathVariable("id") Long id) {
        return ResultUtils.success(transportTaskService.queryTransportOrderIdListById(id));
    }

    /**
     * 根据起始机构查询运输任务id列表
     * @param startAgencyId
     * @param endAgencyId
     * @return
     */
    @GetMapping("findByAgencyId")
    public BaseResponse<List<Long>> findByAgencyId(@RequestParam(name = "startAgencyId", required = false) Long startAgencyId,
                                     @RequestParam(name = "endAgencyId", required = false) Long endAgencyId) {
        return ResultUtils.success(transportTaskService.findByAgencyId(startAgencyId, endAgencyId));
    }


}
