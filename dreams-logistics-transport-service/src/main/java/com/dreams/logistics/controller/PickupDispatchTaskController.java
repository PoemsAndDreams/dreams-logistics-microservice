package com.dreams.logistics.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreams.logistics.common.BaseResponse;
import com.dreams.logistics.common.ErrorCode;
import com.dreams.logistics.common.ResultUtils;
import com.dreams.logistics.enums.PickupDispatchTaskType;
import com.dreams.logistics.exception.BusinessException;
import com.dreams.logistics.exception.ThrowUtils;
import com.dreams.logistics.model.dto.pickupDispatchTask.PickupDispatchTaskDTO;
import com.dreams.logistics.model.dto.pickupDispatchTask.PickupDispatchTaskQueryRequest;
import com.dreams.logistics.model.dto.transport.CourierTaskCountDTO;
import com.dreams.logistics.model.dto.user.UserQueryRequest;
import com.dreams.logistics.model.entity.DcUser;
import com.dreams.logistics.model.entity.PickupDispatchTask;
import com.dreams.logistics.model.vo.PickupDispatchTaskVO;
import com.dreams.logistics.model.vo.UserVO;
import com.dreams.logistics.service.PickupDispatchTaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 取件、派件任务信息表 前端控制器
 */
@RestController
@Api(tags = "取派件任务")
@RequestMapping("pickup-dispatch-task")
public class PickupDispatchTaskController {

    @Resource
    private PickupDispatchTaskService pickupDispatchTaskService;


    /**
     * 按照当日快递员id列表查询每个快递员的取派件任务数
     *
     * @param courierIds
     * @param taskType
     * @param date
     * @return
     */
    @GetMapping("count")
    public List<CourierTaskCountDTO> findCountByCourierIds(@RequestParam("courierIds") List<Long> courierIds,
                                                           @RequestParam("taskType") PickupDispatchTaskType taskType,
                                                           @RequestParam("date") String date) {
        return this.pickupDispatchTaskService.findCountByCourierIds(courierIds, taskType, date);
    }

    /**
     * 查询当日任务
     *
     * @param courierId
     * @return
     */
    @GetMapping("todayTasks/{courierId}")
    public List<PickupDispatchTaskDTO> findTodayTasks(@PathVariable("courierId") Long courierId) {
        return pickupDispatchTaskService.findTodayTaskByCourierId(courierId);
    }

    /**
     * 根据订单id获取取派件任务信息
     *
     * @param orderId
     * @param taskType
     * @return
     */
    @GetMapping("/orderId/{orderId}/{taskType}")
    public List<PickupDispatchTaskDTO> findByOrderId(@PathVariable("orderId") Long orderId,
                                                     @PathVariable("taskType") PickupDispatchTaskType taskType) {
        List<PickupDispatchTask> entities = pickupDispatchTaskService.findByOrderId(orderId, taskType);
        return BeanUtil.copyToList(entities, PickupDispatchTaskDTO.class);
    }


    /**
     * 根据id批量删除取派件任务信息（逻辑删除）
     *
     * @param ids
     * @return
     */
    @DeleteMapping("ids")
    public boolean deleteByIds(@RequestParam("ids") List<Long> ids) {
        return this.pickupDispatchTaskService.deleteByIds(ids);
    }


    /**
     * 改派快递员
     *
     * @param id
     * @param originalCourierId
     * @param targetCourierId
     * @return
     */
    @PutMapping("courier")
    public Boolean updateCourierId(@RequestParam("id") Long id,
                                   @RequestParam("originalCourierId") Long originalCourierId,
                                   @RequestParam("targetCourierId") Long targetCourierId) {
        return this.pickupDispatchTaskService.updateCourierId(id, originalCourierId, targetCourierId);
    }

    /**
     * 更新取派件任务状态
     * @param pickupDispatchTask
     * @return
     */
    @PutMapping
    public Boolean updateStatus(@RequestBody PickupDispatchTask pickupDispatchTask) {
        return this.pickupDispatchTaskService.updateStatus(pickupDispatchTask);
    }



    /**
     * 分页获取封装列表
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PickupDispatchTaskVO>> listPickupDispatchTaskVOByPage(@RequestBody PickupDispatchTaskQueryRequest pickupDispatchTaskQueryRequest,
                                                                                   HttpServletRequest request) {
        if (pickupDispatchTaskQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = pickupDispatchTaskQueryRequest.getCurrent();
        long size = pickupDispatchTaskQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<PickupDispatchTask> pickupDispatchTaskPage = pickupDispatchTaskService.page(new Page<>(current, size),
                pickupDispatchTaskService.getQueryWrapper(pickupDispatchTaskQueryRequest));
        Page<PickupDispatchTaskVO> pickupDispatchTaskVOPage = new Page<>(current, size, pickupDispatchTaskPage.getTotal());
        List<PickupDispatchTaskVO> pickupDispatchTaskVO = pickupDispatchTaskService.getPickupDispatchTaskVO(pickupDispatchTaskPage.getRecords());
        pickupDispatchTaskVOPage.setRecords(pickupDispatchTaskVO);
        return ResultUtils.success(pickupDispatchTaskVOPage);
    }


}