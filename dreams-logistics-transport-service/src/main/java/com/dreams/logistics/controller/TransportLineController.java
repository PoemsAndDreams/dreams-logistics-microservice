package com.dreams.logistics.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreams.logistics.common.BaseResponse;
import com.dreams.logistics.common.ResultUtils;
import com.dreams.logistics.entity.line.TransportLine;
import com.dreams.logistics.exception.BusinessException;
import com.dreams.logistics.model.dto.line.TransportLineDTO;
import com.dreams.logistics.model.dto.line.TransportLineNode;
import com.dreams.logistics.model.dto.line.TransportLineSearch;
import com.dreams.logistics.service.TransportLineService;
import com.dreams.logistics.utils.TransportLineUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 物流路线相关业务对外提供接口服务
 */
@RequestMapping("transports")
@RestController
public class TransportLineController {

    @Resource
    private TransportLineService transportLineService;


    /**
     *查询两个网点之间最短的路线，最大查询深度为：10
     */
    @GetMapping("{startId}/{endId}")
    public BaseResponse<TransportLineNode> queryShortestPath(@PathVariable("startId") Long startId,
                                                  @PathVariable("endId") Long endId) {
        return ResultUtils.success(this.transportLineService.queryShortestPath(startId, endId));
    }


    /**
     *查询两个网点之间成本最低的路线，最大查询深度为：10
     */
    @GetMapping("lowest/{startId}/{endId}")
    public BaseResponse<TransportLineNode> findLowestPath( @PathVariable("startId") Long startId,
                                               @PathVariable("endId") Long endId) {
        return ResultUtils.success(this.transportLineService.findLowestPath(startId, endId));
    }


    /**
     *新增路线，干线：起点终点无顺序，支线：起点必须是二级转运中心，接驳路线：起点必须是网点
     * ● 干线
     *   ○ 一级转运中心到一级转运中心
     * ● 支线
     *   ○ 一级转运中心与二级转运中心之间线路
     * ● 接驳路线
     *   ○ 二级转运中心到网点
     */
    @PostMapping
    public BaseResponse<Boolean> createLine(@RequestBody TransportLineDTO transportLineDTO) {
        TransportLine transportLine = TransportLineUtils.toEntity(transportLineDTO);
        Boolean result = this.transportLineService.createLine(transportLine);
        if (!result) {
            throw new BusinessException("当前线路未注支持!", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return ResultUtils.success(result);
    }

    /**
     *更新路线，可更新字段：cost、name、distance、time、extra，更新数据时id不能为空
     */
    @PutMapping
    public BaseResponse<Boolean> updateLine(@RequestBody TransportLineDTO transportLineDTO) {
        TransportLine transportLine = TransportLineUtils.toEntity(transportLineDTO);
        Boolean result = this.transportLineService.updateLine(transportLine);
        if (!result) {
            throw new BusinessException("更新路线失败！", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return ResultUtils.success(result);
    }


    /**
     *删除路线，单向删除
     */
    @DeleteMapping("{id}")
    public BaseResponse<Boolean> deleteLine(@PathVariable("id") Long id) {
        Boolean result = this.transportLineService.deleteLine(id);
        if (!result) {
            throw new BusinessException("更新路线失败！", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return ResultUtils.success(result);
    }


    /**
     *根据id查询路线
     */
    @GetMapping("{id}")
    public BaseResponse<TransportLineDTO> queryById(@PathVariable("id") Long id) {
        TransportLine transportLine = this.transportLineService.queryById(id);
        return ResultUtils.success(TransportLineUtils.toDTO(transportLine));
    }


    /**
     *根据ids批量查询路线
     */
    @GetMapping("list")
    public BaseResponse<List<TransportLineDTO>> queryByIds(@Size(min = 1, message = "至少要传入1个id") @RequestParam("ids") Long[] ids) {
        List<TransportLine> list = this.transportLineService.queryByIds(ids);
        return ResultUtils.success(TransportLineUtils.toDTOList(list));
    }

    /**
     *分页查询路线，如果有条件就进行筛选查询
     */
    @PostMapping("page")
    public BaseResponse<Page<TransportLine>> queryPageList(@RequestBody TransportLineSearch transportLineSearch) {

        return ResultUtils.success(this.transportLineService.queryPageList(transportLineSearch));
    }

}
