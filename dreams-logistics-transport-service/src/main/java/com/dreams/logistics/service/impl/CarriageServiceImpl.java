package com.dreams.logistics.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreams.logistics.common.ErrorCode;
import com.dreams.logistics.constant.CarriageConstant;
import com.dreams.logistics.constant.CommonConstant;
import com.dreams.logistics.exception.BusinessException;
import com.dreams.logistics.handler.CarriageChainHandler;
import com.dreams.logistics.model.dto.carriage.CarriageQueryRequest;
import com.dreams.logistics.model.dto.carriage.WaybillDTO;
import com.dreams.logistics.model.entity.Carriage;
import com.dreams.logistics.model.vo.CarriageVO;
import com.dreams.logistics.service.CarriageService;
import com.dreams.logistics.mapper.CarriageMapper;
import com.dreams.logistics.utils.SqlUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/**
* @author xiayutian
* @description 针对表【carriage(运费模板表)】的数据库操作Service实现
* @createDate 2025-01-23 14:42:15
*/
@Service
public class CarriageServiceImpl extends ServiceImpl<CarriageMapper, Carriage>
    implements CarriageService{
    @Resource
    private CarriageChainHandler carriageChainHandler;
    @Override
    public Wrapper<Carriage> getQueryWrapper(CarriageQueryRequest carriageQueryRequest) {

        if (carriageQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        Long id = carriageQueryRequest.getId();
        String templateType = carriageQueryRequest.getTemplateType();
        String transportType = carriageQueryRequest.getTransportType();
        String associatedCity = carriageQueryRequest.getAssociatedCity();
        Double firstWeight = carriageQueryRequest.getFirstWeight();
        Double continuousWeight = carriageQueryRequest.getContinuousWeight();
        Integer lightThrowingCoefficient = carriageQueryRequest.getLightThrowingCoefficient();

        String sortField = carriageQueryRequest.getSortField();
        String sortOrder = carriageQueryRequest.getSortOrder();

        QueryWrapper<Carriage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(StringUtils.isNotEmpty(templateType),"template_type", templateType);
        queryWrapper.like(StringUtils.isNotEmpty(transportType),"transport_type", transportType);
        queryWrapper.like(StringUtils.isNotEmpty(associatedCity),"associated_city", associatedCity);
        queryWrapper.eq(firstWeight != null,"first_weight", firstWeight);
        queryWrapper.eq(continuousWeight != null,"continuous_weight", continuousWeight);
        queryWrapper.eq(lightThrowingCoefficient != null,"light_throwing_coefficient", lightThrowingCoefficient);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
        
    }

    @Override
    public boolean saveOrUpdateCarriage(Carriage carriage) {
        // 判断是否有重复的模板
        LambdaQueryWrapper<Carriage> queryWrapper = Wrappers.<Carriage>lambdaQuery()
                .eq(Carriage::getTemplateType, carriage.getTemplateType())
                .eq(Carriage::getTransportType, carriage.getTransportType())
                .eq(Carriage::getAssociatedCity, carriage.getAssociatedCity());

        // 查询数据库
        List<Carriage> carriageList = super.list(queryWrapper);

        // 如果有重复的模板，抛出异常
        if (!CollUtil.isEmpty(carriageList)) {
            throw new BusinessException(ErrorCode.CARRIAGE_REPEAT);
        }

        // 如果是更新操作，检查是否有变化
        if (carriage.getId() != null) {
            Carriage oldCarriage = this.getById(carriage.getId());
            // 比较 templateType, transportType, associatedCity 字段是否发生变化
            if (Objects.equals(oldCarriage.getTemplateType(), carriage.getTemplateType()) &&
                    Objects.equals(oldCarriage.getTransportType(), carriage.getTransportType()) &&
                    Objects.equals(oldCarriage.getAssociatedCity(), carriage.getAssociatedCity())) {
                // 没有变化，直接更新
                return this.updateById(carriage);
            }
        }

        // 如果没有重复模板且需要插入或更新
        return this.saveOrUpdate(carriage);
    }

    @Override
    public CarriageVO compute(WaybillDTO waybillDTO) {
        //根据参数查找运费模板
        Carriage carriage = this.carriageChainHandler.findCarriage(waybillDTO);

        //计算重量，确保最小重量为1kg
        double computeWeight = this.getComputeWeight(waybillDTO, carriage);

        //计算运费，首重 + 续重
        double expense = carriage.getFirstWeight() + ((computeWeight - 1) * carriage.getContinuousWeight());

        //保留一位小数
        expense = NumberUtil.round(expense, 1).doubleValue();

        //封装运费和计算重量到DTO，并返回
        CarriageVO carriageVO = new CarriageVO();
        carriageVO.setExpense(expense);
        carriageVO.setComputeWeight(computeWeight);
        return carriageVO;
    }

    @Override
    public Carriage findByTemplateType(Integer templateType) {
        if (ObjectUtil.equals(templateType, CarriageConstant.ECONOMIC_ZONE)) {
            throw new BusinessException(ErrorCode.METHOD_CALL_ERROR);
        }
        LambdaQueryWrapper<Carriage> queryWrapper = Wrappers.lambdaQuery(Carriage.class).eq(Carriage::getTemplateType, templateType).eq(Carriage::getTransportType, CarriageConstant.REGULAR_FAST);
        return super.getOne(queryWrapper);
    }

    /**
     * 根据体积参数与实际重量计算计费重量
     *
     * @param waybillDTO 运费计算对象
     * @param carriage   运费模板
     * @return 计费重量
     */
    private double getComputeWeight(WaybillDTO waybillDTO, Carriage carriage) {
        //计算体积，如果传入体积不需要计算
        Integer volume = waybillDTO.getVolume();
        if (ObjectUtil.isEmpty(volume)) {
            try {
                //长*宽*高计算体积
                volume = waybillDTO.getMeasureLong() * waybillDTO.getMeasureWidth() * waybillDTO.getMeasureHigh();
            } catch (Exception e) {
                //计算出错设置体积为0
                volume = 0;
            }
        }

        // 计算体积重量，体积 / 轻抛系数
        BigDecimal volumeWeight = NumberUtil.div(volume, carriage.getLightThrowingCoefficient(), 1);

        //取大值
        double computeWeight = NumberUtil.max(volumeWeight.doubleValue(), NumberUtil.round(waybillDTO.getWeight(), 1).doubleValue());

        //计算续重，规则：不满1kg，按1kg计费；
        // 10kg以下续重以0.1kg计量保留1位小数；
        // 10-100kg续重以0.5kg计量保留1位小数；
        // 100kg以上四舍五入取整
        if (computeWeight <= 1) {
            return 1;
        }

        if (computeWeight <= 10) {
            return computeWeight;
        }

        if (computeWeight >= 100) {
            return NumberUtil.round(computeWeight, 0).doubleValue();
        }

        //0.5为一个计算单位
        int integer = NumberUtil.round(computeWeight, 0, RoundingMode.DOWN).intValue();
        if (NumberUtil.sub(computeWeight, integer) == 0) {
            return integer;
        }

        if (NumberUtil.sub(computeWeight, integer) <= 0.5) {
            return NumberUtil.add(integer, 0.5);
        }
        return NumberUtil.add(integer, 1);
    }


}




