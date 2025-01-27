package com.dreams.logistics.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreams.logistics.common.BaseResponse;
import com.dreams.logistics.common.DeleteRequest;
import com.dreams.logistics.common.ErrorCode;
import com.dreams.logistics.common.ResultUtils;
import com.dreams.logistics.exception.BusinessException;
import com.dreams.logistics.exception.ThrowUtils;
import com.dreams.logistics.model.dto.order.OrderAddRequest;
import com.dreams.logistics.model.dto.order.OrderSearchRequest;
import com.dreams.logistics.model.dto.order.OrderUpdateRequest;
import com.dreams.logistics.model.vo.OrderVO;
import com.dreams.logistics.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 支付相关接口
 */
@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {

    @Resource
    private OrderService orderService;


    /**
     * 分页获取订单列表
     *
     * @param orderSearchRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<OrderVO>> listOrderByPage(@RequestBody OrderSearchRequest orderSearchRequest,
                                                     HttpServletRequest request) {

        Page<OrderVO> orderPage = orderService.page(orderSearchRequest);
        return ResultUtils.success(orderPage);
    }


    /**
     * 创建订单
     *
     * @param orderAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
//    @PreAuthorize("hasAuthority('admin_order_add')")
    public BaseResponse<Boolean> addOrder(@RequestBody OrderAddRequest orderAddRequest, HttpServletRequest request) {
        if (orderAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Boolean b = orderService.saveOrderVO(orderAddRequest);
        return ResultUtils.success(b);
    }

    /**
     * 删除订单
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteOrder(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = orderService.removeOrder(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新订单
     *
     * @param orderUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateOrder(@RequestBody OrderUpdateRequest orderUpdateRequest,
                                            HttpServletRequest request) {
        if (orderUpdateRequest == null || orderUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        boolean result = orderService.updateOrder(orderUpdateRequest);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取订单
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<OrderVO> getOrderById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        OrderVO orderVO = orderService.getOrderVOById(id);

        ThrowUtils.throwIf(orderVO == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(orderVO);
    }


}
