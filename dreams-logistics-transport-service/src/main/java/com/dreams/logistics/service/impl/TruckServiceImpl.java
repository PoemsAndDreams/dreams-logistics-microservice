package com.dreams.logistics.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreams.logistics.common.ErrorCode;
import com.dreams.logistics.constant.CommonConstant;
import com.dreams.logistics.enums.StatusEnum;
import com.dreams.logistics.enums.TruckRunStatusEnum;
import com.dreams.logistics.exception.BusinessException;
import com.dreams.logistics.model.dto.truck.TruckQueryRequest;
import com.dreams.logistics.model.dto.truckPlan.TruckDto;
import com.dreams.logistics.model.entity.Truck;
import com.dreams.logistics.model.entity.Organization;
import com.dreams.logistics.model.vo.TruckVO;
import com.dreams.logistics.service.TruckService;
import com.dreams.logistics.mapper.TruckMapper;
import com.dreams.logistics.service.UserFeignClient;
import com.dreams.logistics.utils.SqlUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
* @author xiayutian
* @description 针对表【truck(车辆信息表)】的数据库操作Service实现
* @createDate 2025-02-04 08:47:38
*/
@Service
public class TruckServiceImpl extends ServiceImpl<TruckMapper, Truck>
    implements TruckService{

    @Resource
    private UserFeignClient userFeignClient;

    @Override
    public Wrapper<Truck> getQueryWrapper(TruckQueryRequest truckQueryRequest) {

        if (truckQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        Long truckTypeId = truckQueryRequest.getTruckTypeId();
        String brand = truckQueryRequest.getBrand();
        String licensePlate = truckQueryRequest.getLicensePlate();
        BigDecimal allowableLoad = truckQueryRequest.getAllowableLoad();
        BigDecimal allowableVolume = truckQueryRequest.getAllowableVolume();
        Long currentOrganId = truckQueryRequest.getCurrentOrganId();
        Integer runStatus = truckQueryRequest.getRunStatus();
        Integer status = truckQueryRequest.getStatus();
        Integer workStatus = truckQueryRequest.getWorkStatus();
        Double loadingRatio = truckQueryRequest.getLoadingRatio();

        String sortField = truckQueryRequest.getSortField();
        String sortOrder = truckQueryRequest.getSortOrder();

        QueryWrapper<Truck> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq( truckTypeId != null, "truck_type_id",  truckTypeId);
        queryWrapper.eq( currentOrganId != null, "current_organ_id",  currentOrganId);
        queryWrapper.eq( runStatus != null, "run_status",  runStatus);
        queryWrapper.eq( status != null, "status",  status);
        queryWrapper.eq( workStatus != null, "work_status",  workStatus);
        queryWrapper.eq( loadingRatio != null, "loading_ratio",  loadingRatio);
        queryWrapper.eq( allowableLoad != null, "allowable_load",  allowableLoad);
        queryWrapper.eq( allowableVolume != null, "allowable_volume",  allowableVolume);
        queryWrapper.eq(StringUtils.isNotBlank(brand), "brand",brand);
        queryWrapper.eq(StringUtils.isNotBlank(licensePlate), "license_plate", licensePlate);

        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public TruckVO getTruckVO(Truck truck) {
        if (truck == null) {
            return null;
        }
        TruckVO truckVO = new TruckVO();
        BeanUtils.copyProperties(truck, truckVO);
        // 补充字段
        Organization organization =  userFeignClient.getOrganizationById(truck.getCurrentOrganId().toString());
        if (!Objects.isNull(organization)){
            truckVO.setCurrentOrganName(organization.getName());
        }
        return truckVO;
    }

    @Override
    public List<TruckVO> getTruckVO(List<Truck> truckList) {

        if (CollUtil.isEmpty(truckList)) {
            return new ArrayList<>();
        }
        return truckList.stream().map(truck -> {
            TruckVO truckVO = new TruckVO();
            BeanUtils.copyProperties(truck, truckVO);
            if (!Objects.isNull(truck.getCurrentOrganId())){
                Organization organization =  userFeignClient.getOrganizationById(truck.getCurrentOrganId().toString());
                if (!Objects.isNull(organization)){
                    truckVO.setCurrentOrganName(organization.getName());
                }
            }
            // 补充字段
            return truckVO;
        }).collect(Collectors.toList());

    }

    @Override
    public Boolean updateCurrentOrganId(Long truckId, Long currentOrganId, StatusEnum statusEnum) {
        UpdateWrapper<Truck> wrapper = new UpdateWrapper<>();

        wrapper.lambda()
                // 更新位置
                .set(Truck::getCurrentOrganId, currentOrganId)
                // 更新车辆状态
                .set(Truck::getRunStatus, TruckRunStatusEnum.ARRIVED.getCode())
                // 禁用状态
                .set(Truck::getStatus, statusEnum.getCode())
                .eq(Truck::getId, truckId);
        return super.update(wrapper);

    }

    @Override
    public Truck getTruck(String licensePlate) {
        LambdaQueryWrapper<Truck> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Truck::getLicensePlate,licensePlate);
        return this.getOne(wrapper);
    }

    @Override
    public TruckDto fineById(Long truckId) {
        Truck truck = this.getById(truckId);
        if (ObjectUtil.isEmpty(truck)) {
            return null;
        }
        return BeanUtil.toBean(truck, TruckDto.class);
    }

    @Override
    public void updateRunStatus(Long id, TruckRunStatusEnum status) {
        Truck truck= new Truck();
        truck.setId(id);
        truck.setRunStatus(status.getCode());
        super.updateById(truck);
    }
}




