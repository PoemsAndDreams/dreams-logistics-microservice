package com.dreams.logistics.controller;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreams.logistics.common.BaseResponse;
import com.dreams.logistics.common.ResultUtils;
import com.dreams.logistics.enums.TransportOrderStatus;
import com.dreams.logistics.enums.WorkExceptionEnum;
import com.dreams.logistics.exception.BusinessException;
import com.dreams.logistics.model.dto.transport.TransportOrderDTO;
import com.dreams.logistics.model.dto.transport.request.TransportOrderQueryDTO;
import com.dreams.logistics.model.dto.transport.response.OrderToTransportOrderDTO;
import com.dreams.logistics.model.entity.TransportOrder;
import com.dreams.logistics.service.TransportOrderService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 运单表
 */
@RestController
@RequestMapping("transport-order")
public class TransportOrderController {

    @Resource
    private TransportOrderService transportOrderService;

    /**
     * 新增运单（订单转运单）
     * @param orderId
     * @return
     */
    @PostMapping
    public BaseResponse<OrderToTransportOrderDTO> orderToTransportOrder(@RequestParam("orderId") Long orderId) {
        TransportOrder transportOrderEntity = this.transportOrderService.orderToTransportOrder(orderId);
        return ResultUtils.success(BeanUtil.toBean(transportOrderEntity, OrderToTransportOrderDTO.class));
    }

    /**
     * 更新状态，不允许 CREATED 状态
     * @param id
     * @param status
     * @return
     */
    @PutMapping
    public BaseResponse<Boolean> updateStatus(@RequestParam("id") String id,
                                @RequestParam("status") TransportOrderStatus status) {
        return ResultUtils.success(this.transportOrderService.updateStatus(Arrays.asList(id), status));
    }

    /**
     * 获取运单分页数据
     * @param transportOrderQueryDTO
     * @return
     */
    @PostMapping("page")
    public BaseResponse<Page<TransportOrder>> findByPage(@RequestBody TransportOrderQueryDTO transportOrderQueryDTO) {
        Page<TransportOrder> pageResult = this.transportOrderService.findByPage(transportOrderQueryDTO);


        return ResultUtils.success(pageResult);
    }

    /**
     * 根据id获取运单信息
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public BaseResponse<TransportOrderDTO> findById(@PathVariable("id") String id) {
        TransportOrder transportOrder = this.transportOrderService.getById(id);
        if (ObjectUtil.isEmpty(transportOrder)) {
            throw new BusinessException(WorkExceptionEnum.TRANSPORT_ORDER_NOT_FOUND);
        }
        return ResultUtils.success(BeanUtil.toBean(transportOrder, TransportOrderDTO.class));
    }


    /**
     * 根据运单ids批量获取订单id
     * @param ids
     * @return
     */
    @GetMapping("/batch")
    public BaseResponse<List<TransportOrderDTO>> findByIds(@RequestParam("ids") String[] ids) {
        List<TransportOrder> list = this.transportOrderService.findByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BusinessException(WorkExceptionEnum.TRANSPORT_ORDER_NOT_FOUND);
        }
        return ResultUtils.success(BeanUtil.copyToList(list, TransportOrderDTO.class));
    }


    /**
     * 根据订单id获取运单信息
     * @param orderId
     * @return
     */
    @GetMapping("orderId/{orderId}")
    public BaseResponse<TransportOrderDTO> findByOrderId(@PathVariable("orderId") Long orderId) {
        TransportOrder transportOrder = this.transportOrderService.findByOrderId(orderId);
        if (ObjectUtil.isEmpty(transportOrder)) {
            throw new BusinessException(WorkExceptionEnum.TRANSPORT_ORDER_NOT_FOUND);
        }
        return ResultUtils.success(BeanUtil.toBean(transportOrder, TransportOrderDTO.class));
    }

    /**
     * 根据多个订单id查询运单信息
     * @param orderIds
     * @return
     */
    @PostMapping("list")
    public BaseResponse<List<TransportOrderDTO>> findByOrderIds(@RequestParam("orderIds") Long[] orderIds) {
        List<TransportOrder> list = this.transportOrderService.findByOrderIds(orderIds);
        if (CollUtil.isEmpty(list)) {
            return ResultUtils.success(new ArrayList<>());
        }
        return ResultUtils.success(BeanUtil.copyToList(list, TransportOrderDTO.class));
    }

    /**
     * 根据运单号搜索运单
     * @param id
     * @return
     */
    @GetMapping("search")
    public BaseResponse<List<TransportOrderDTO>> searchById(@RequestParam("id") String id) {
        List<TransportOrder> entityList = this.transportOrderService.searchById(id);
        if (CollUtil.isEmpty(entityList)) {
            return ResultUtils.success(ListUtil.empty());
        }
        return ResultUtils.success(BeanUtil.copyToList(entityList, TransportOrderDTO.class));
    }



    /**
     * 根据运输任务id批量修改运单，其中会涉及到下一个节点的流转，已经发送消息的业务
     * @param taskId
     * @return
     */
    @PutMapping("updateByTaskId/{taskId}")
    public BaseResponse<Boolean> updateByTaskId(@PathVariable("taskId") String taskId) {
        return ResultUtils.success(transportOrderService.updateByTaskId(Long.valueOf(taskId)));
    }

    /**
     * 批量更新状态，不允许 CREATED 状态
     * @param ids
     * @param status
     * @return
     */
    @PutMapping("batchUpdate")
    public BaseResponse<Boolean> updateStatus(@RequestParam("ids") List<String> ids,
                                @RequestParam("status") TransportOrderStatus status) {
        return ResultUtils.success(this.transportOrderService.updateStatus(ids, status));
    }


    /**
     * 据运输任务id分页查询运单列表，并模糊查询运单id
     * @param page
     * @param pageSize
     * @param taskId
     * @param transportOrderId
     * @return
     */
    @GetMapping("pageQueryByTaskId")
    public BaseResponse<Page<TransportOrderDTO>> pageQueryByTaskId(@RequestParam(name = "page", defaultValue = "1") Integer page,
                                                             @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                             @RequestParam("taskId") String taskId,
                                                             @RequestParam(name = "transportOrderId", required = false) String transportOrderId) {
        return ResultUtils.success(transportOrderService.pageQueryByTaskId(page, pageSize, taskId, transportOrderId));
    }


}
