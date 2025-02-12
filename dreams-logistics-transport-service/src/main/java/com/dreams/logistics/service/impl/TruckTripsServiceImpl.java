package com.dreams.logistics.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreams.logistics.common.ErrorCode;
import com.dreams.logistics.constant.CommonConstant;
import com.dreams.logistics.constant.TruckConstant;
import com.dreams.logistics.entity.line.TransportLine;
import com.dreams.logistics.exception.BusinessException;
import com.dreams.logistics.model.dto.truckPlan.OrganIdsDto;
import com.dreams.logistics.model.dto.truckTrips.TruckTripsAddRequest;
import com.dreams.logistics.model.dto.truckTrips.TruckTripsQueryRequest;
import com.dreams.logistics.model.dto.truckTrips.TruckTripsUpdateRequest;
import com.dreams.logistics.model.entity.TruckTrips;
import com.dreams.logistics.model.vo.TruckTripsVO;
import com.dreams.logistics.service.TransportLineService;
import com.dreams.logistics.service.TruckTripsService;
import com.dreams.logistics.mapper.TruckTripsMapper;
import com.dreams.logistics.service.TruckTripsTruckDriverService;
import com.dreams.logistics.utils.SqlUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author xiayutian
* @description 针对表【truck_trips(车次信息表)】的数据库操作Service实现
* @createDate 2025-02-05 15:19:37
*/
@Service
public class TruckTripsServiceImpl extends ServiceImpl<TruckTripsMapper, TruckTrips>
    implements TruckTripsService{


    @Resource
    TransportLineService transportLineService;

    @Resource
    private TruckTripsTruckDriverService truckTripsTruckDriverService;

    /**
     * 获取车次列表
     *
     * @param transportLineId 线路id
     * @param ids             车次id列表
     * @return 车次列表
     */
    @Override
    public List<TruckTrips> findAll(Long transportLineId, List<Long> ids) {
        LambdaQueryWrapper<TruckTrips> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (ObjectUtil.isNotEmpty(transportLineId)) {
            lambdaQueryWrapper.eq(TruckTrips::getTransportLineId, transportLineId);
        }
        if (CollUtil.isNotEmpty(ids)) {
            lambdaQueryWrapper.in(TruckTrips::getId, ids);
        }
        lambdaQueryWrapper.orderByDesc(TruckTrips::getCreated);
        lambdaQueryWrapper.eq(TruckTrips::getStatus, TruckConstant.DATA_DEFAULT_STATUS);
        return super.list(lambdaQueryWrapper);
    }

    /**
     * 删除车次
     *
     * @param id 车次ID
     */
    @Override
    public void disable(Long id) {
        // 检查车次绑定关系
        Boolean remove = truckTripsTruckDriverService.canRemove(id, null, null);
        if (!remove) {
            throw new BusinessException("该车次下存在绑定车辆，请先解除绑定后删除");
        }

        TruckTrips truckTrips = new TruckTrips();
        truckTrips.setId(id);
        truckTrips.setStatus(TruckConstant.DATA_DISABLE_STATUS);
        baseMapper.updateById(truckTrips);
    }

    /**
     * 根据线路ID查询机构
     * @param values 线路ID
     * @return 机构信息
     */
    @Override
    public Map<Long, OrganIdsDto> getOrganIdsByTripsLineId(HashSet<Long> values) {
        HashMap<Long, OrganIdsDto> hashMap = new HashMap<>();
        List<TransportLine> listR = transportLineService.queryByIds(values.toArray(new Long[0]));

        listR.forEach(v -> {
            OrganIdsDto organIdsDto = new OrganIdsDto();
            organIdsDto.setStartOrganId(v.getStartOrganId());
            organIdsDto.setEndOrganId(v.getEndOrganId());
            hashMap.put(v.getId(), organIdsDto);
        });

        return hashMap;
    }
    @Override
    public boolean saveTruckTrips(TruckTripsAddRequest truckTripsAddRequest) {
        TruckTrips truckTrips = new TruckTrips();
        BeanUtils.copyProperties(truckTripsAddRequest, truckTrips);
        return this.save(truckTrips);

    }

    @Override
    public boolean updateTruckTrips(TruckTripsUpdateRequest truckTripsUpdateRequest) {
        TruckTrips truckTrips = new TruckTrips();
        truckTrips.setId(truckTripsUpdateRequest.getId());
        truckTrips.setName(truckTripsUpdateRequest.getName());
        truckTrips.setPeriod(truckTripsUpdateRequest.getPeriod());
        truckTrips.setEstimatedTime(truckTripsUpdateRequest.getEstimatedTime());
        truckTrips.setDepartureTime(truckTripsUpdateRequest.getDepartureTime());
        return this.updateById(truckTrips);
    }

    @Override
    public TruckTripsVO getTruckTrips(long id) {
        // 获取 TruckTrips 实体
        TruckTrips truckTrips = this.getById(id);

        // 将转换后的数据封装到 TruckTripsVO 对象中
        TruckTripsVO truckTripsVO = new TruckTripsVO();
        BeanUtils.copyProperties(truckTrips, truckTripsVO);
        return truckTripsVO;
    }

    @Override
    public Wrapper<TruckTrips> getQueryWrapper(TruckTripsQueryRequest truckTripsQueryRequest) {
        if (truckTripsQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        Long transportLineId = truckTripsQueryRequest.getTransportLineId();
        String name = truckTripsQueryRequest.getName();
        Integer period = truckTripsQueryRequest.getPeriod();
        String departureTime = truckTripsQueryRequest.getDepartureTime();
        BigDecimal estimatedTime = truckTripsQueryRequest.getEstimatedTime();
        Integer status = truckTripsQueryRequest.getStatus();

        String sortField = truckTripsQueryRequest.getSortField();
        String sortOrder = truckTripsQueryRequest.getSortOrder();


        QueryWrapper<TruckTrips> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(transportLineId != null, "transport_line_id", transportLineId);
        queryWrapper.eq(period != null, "period", period);
        queryWrapper.eq(status != null, "status", status);
        queryWrapper.eq(StringUtils.isNotEmpty(name), "name", name);
        queryWrapper.eq(StringUtils.isNotEmpty(departureTime), "departure_time", departureTime);
        queryWrapper.eq(StringUtils.isNotEmpty(name), "estimated_time", estimatedTime);

        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;

    }

    @Override
    public List<TruckTripsVO> getTruckTripsVO(List<TruckTrips> records) {

        List<TruckTripsVO> list = records.stream().map(truckTrips -> {
            return getTruckTripsVO(truckTrips);
        }).collect(Collectors.toList());

        return list;
    }

    @Override
    public TruckTripsVO getTruckTripsVO(TruckTrips truckTrips) {

        // 将转换后的数据封装到 TruckTripsVO 对象中
        TruckTripsVO truckTripsVO = new TruckTripsVO();
        truckTripsVO.setId(truckTrips.getId());
        truckTripsVO.setTransportLineId(truckTrips.getTransportLineId());
        truckTripsVO.setName(truckTrips.getName());
        truckTripsVO.setPeriod(truckTrips.getPeriod());
        truckTripsVO.setDepartureTime(truckTrips.getDepartureTime());
        truckTripsVO.setEstimatedTime(truckTrips.getEstimatedTime());
        truckTripsVO.setStatus(truckTrips.getStatus());

        List<String> plateList = truckTripsTruckDriverService.findTruckDriverLicensePlateList(String.valueOf(truckTrips.getId()));
        truckTripsVO.setLicensePlateList(plateList);

        return truckTripsVO;
    }
}




