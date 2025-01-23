package com.dreams.logistics.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreams.logistics.common.BaseResponse;
import com.dreams.logistics.common.DeleteRequest;
import com.dreams.logistics.common.ErrorCode;
import com.dreams.logistics.common.ResultUtils;
import com.dreams.logistics.exception.BusinessException;
import com.dreams.logistics.exception.ThrowUtils;
import com.dreams.logistics.model.dto.area.AreaAddRequest;
import com.dreams.logistics.model.dto.area.AreaQueryRequest;
import com.dreams.logistics.model.dto.area.AreaUpdateRequest;
import com.dreams.logistics.model.entity.Area;
import com.dreams.logistics.model.entity.Area;
import com.dreams.logistics.model.vo.AreaVO;
import com.dreams.logistics.service.AreaService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 区域接口
 */
@RestController
@RequestMapping("/area")
@Slf4j
public class AreaController {

    @Resource
    private AreaService areaService;
    /**
     * 创建区域
     *
     * @param areaAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
//    @PreAuthorize("hasAuthority('admin_area_add')")
    public BaseResponse<String> addArea(@RequestBody AreaAddRequest areaAddRequest, HttpServletRequest request) {
        if (areaAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Area area = new Area();
        BeanUtils.copyProperties(areaAddRequest, area);
        areaService.save(area);
        return ResultUtils.success(area.getId());
    }

    /**
     * 删除区域
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteArea(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = areaService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新区域
     *
     * @param areaUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateArea(@RequestBody AreaUpdateRequest areaUpdateRequest,
            HttpServletRequest request) {
        if (areaUpdateRequest == null || areaUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Area area = new Area();
        BeanUtils.copyProperties(areaUpdateRequest, area);

        boolean result = areaService.updateById(area);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取区域（仅管理员）
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Area> getAreaById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Area area = areaService.getById(id);
        ThrowUtils.throwIf(area == null, ErrorCode.NOT_FOUND_ERROR);

        return ResultUtils.success(area);
    }


    /**
     * 分页获取区域列表
     *
     * @param areaQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<Area>> listAreaByPage(@RequestBody AreaQueryRequest areaQueryRequest,
                                                     HttpServletRequest request) {
        long current = areaQueryRequest.getCurrent();
        long size = areaQueryRequest.getPageSize();
        Page<Area> areaPage = areaService.page(new Page<>(current, size),
                areaService.getQueryWrapper(areaQueryRequest));
        return ResultUtils.success(areaPage);
    }


    // todo 数据量巨大
//    /**
//     * 查出所有分类以及子分类,以树形结构组装起来
//     */
//    @GetMapping("/list/tree")
//    public BaseResponse<List<Area>> listAreaList(){
//        List<Area> entities = areaService.listWithTree();
//        return ResultUtils.success(entities);
//    }


    @GetMapping("/children")
    public BaseResponse<List<AreaVO>> findAreaChildren(@NotNull(message = "父id不能为空!") @RequestParam("parentId") Long parentId) {
        return ResultUtils.success(areaService.findChildren(parentId));
    }

}
