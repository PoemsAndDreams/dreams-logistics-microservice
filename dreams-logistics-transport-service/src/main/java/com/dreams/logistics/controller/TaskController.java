package com.dreams.logistics.controller;

import com.dreams.logistics.common.BaseResponse;
import com.dreams.logistics.common.ResultUtils;
import com.dreams.logistics.model.dto.task.TaskPickupVO;
import com.dreams.logistics.model.dto.task.TaskSignVO;
import com.dreams.logistics.service.TaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;


@Api(tags = "任务（取件和派件）相关接口")
@RequestMapping("/tasks")
@RestController
@Validated
public class TaskController {

    @Resource
    private TaskService taskService;

    /**
     * 取件
     *
     * @param taskPickupVO
     * @return
     */
    @PutMapping("/pickup")
    public BaseResponse<Boolean> pickup(@RequestBody TaskPickupVO taskPickupVO) {
        return ResultUtils.success(taskService.pickup(taskPickupVO));
    }

    /**
     * 用户拒收
     *
     * @param id
     * @return
     */
    @PutMapping("/reject/{id}")
    public BaseResponse<Boolean> reject(@PathVariable("id") String id) {
        taskService.reject(id);
        return ResultUtils.success(true);

    }

    /**
     * 签收
     *
     * @param taskSignVO
     * @return
     */
    @PutMapping("sign")
    public BaseResponse<Boolean> sign(@RequestBody TaskSignVO taskSignVO) {
        taskService.sign(taskSignVO);
        return ResultUtils.success(true);
    }

}
