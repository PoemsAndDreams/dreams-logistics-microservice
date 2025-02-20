package com.dreams.logistics.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.dreams.logistics.common.ErrorCode;
import com.dreams.logistics.enums.ServiceTypeEnum;
import com.dreams.logistics.enums.WorkUserTypeEnum;
import com.dreams.logistics.exception.BusinessException;
import com.dreams.logistics.model.dto.serviceScope.ServiceScopeDTO;
import com.dreams.logistics.model.entity.ServiceScope;
import com.dreams.logistics.model.entity.WorkSchedule;
import com.dreams.logistics.model.vo.WorkScheduleVO;
import com.dreams.logistics.service.ScopeService;
import com.dreams.logistics.service.WorkScheduleService;
import com.dreams.logistics.utils.BaiduMap;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ScopeServiceImpl implements ScopeService {

    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private BaiduMap baiduMap;

    @Resource
    private WorkScheduleService workScheduleService;

    @Override
    public Boolean saveOrUpdate(Long bid, ServiceTypeEnum type, GeoJsonPolygon polygon) {
        Query query = Query.query(Criteria.where("bid").is(bid).and("type").is(type.getCode())); //构造查询条件
        ServiceScope serviceScopeEntity = this.mongoTemplate.findOne(query, ServiceScope.class);
        if (ObjectUtil.isEmpty(serviceScopeEntity)) {
            //新增
            serviceScopeEntity = new ServiceScope();
            serviceScopeEntity.setBid(bid);
            serviceScopeEntity.setType(type.getCode());
            serviceScopeEntity.setPolygon(polygon);
            serviceScopeEntity.setCreated(System.currentTimeMillis());
            serviceScopeEntity.setUpdated(serviceScopeEntity.getCreated());
        } else {
            //更新
            serviceScopeEntity.setPolygon(polygon);
            serviceScopeEntity.setUpdated(System.currentTimeMillis());
        }

        try {
            this.mongoTemplate.save(serviceScopeEntity);
            return true;
        } catch (Exception e) {
            log.error("新增/更新服务范围数据失败！ bid = {}, type = {}, points = {}", bid, type, polygon.getPoints(), e);
        }
        return false;
    }

    @Override
    public Boolean delete(String id) {
        Query query = Query.query(Criteria.where("id").is(new ObjectId(id))); //构造查询条件
        return this.mongoTemplate.remove(query, ServiceScope.class).getDeletedCount() > 0;
    }

    @Override
    public Boolean delete(Long bid, ServiceTypeEnum type) {
        Query query = Query.query(Criteria.where("bid").is(bid).and("type").is(type.getCode())); //构造查询条件
        return this.mongoTemplate.remove(query, ServiceScope.class).getDeletedCount() > 0;
    }

    @Override
    public ServiceScope queryById(String id) {
        return this.mongoTemplate.findById(new ObjectId(id), ServiceScope.class);
    }

    @Override
    public ServiceScope queryByBidAndType(Long bid, ServiceTypeEnum type) {
        Query query = Query.query(Criteria.where("bid").is(bid).and("type").is(type.getCode())); //构造查询条件
        return this.mongoTemplate.findOne(query, ServiceScope.class);
    }

    @Override
    public List<ServiceScope> queryListByPoint(ServiceTypeEnum type, GeoJsonPoint point) {
        Query query = Query.query(Criteria.where("polygon").intersects(point)
                .and("type").is(type.getCode()));
        return this.mongoTemplate.find(query, ServiceScope.class);
    }

    @Override
    public List<ServiceScope> queryListByPoint(ServiceTypeEnum type, String address) {
        //根据详细地址查询坐标
        List<Double> list = baiduMap.geocodingReturn(address);
        // 提取经度和纬度
        double lng = list.get(0);
        double lat =  list.get(1);

        return this.queryListByPoint(type, new GeoJsonPoint(lng, lat));
    }


    /**
     * 条件查询快递员列表（结束取件时间当天快递员有排班）
     * 如果服务范围内无快递员，或满足服务范围的快递员无排班，则返回该网点所有满足排班的快递员
     *
     * @param agencyId         网点id
     * @param longitude        用户地址的经度
     * @param latitude         用户地址的纬度
     * @param estimatedEndTime 结束取件时间
     * @return 快递员id列表
     */
    @Override
    public List<Long> queryCourierIdListByCondition(Long agencyId, Double longitude, Double latitude, Long estimatedEndTime) {
        log.info("当前机构id为：{}", agencyId);

        //1.根据经纬度查询服务范围内的快递员
        List<ServiceScope> serviceScopes = this.queryListByPoint(ServiceTypeEnum.codeOf(2), new GeoJsonPoint(longitude, latitude));

        List<WorkScheduleVO> workSchedules = new ArrayList<>();

        List<Long> courierIds = null;
        //2.如果服务范围内有快递员，则在其中筛选结束取件时间当天有排班的快递员
        if (CollUtil.isNotEmpty(serviceScopes)) {
            List<Long> bids = serviceScopes.stream().map(ServiceScope::getBid).collect(Collectors.toList());
            log.info("根据经纬度查询到的快递员id有：{}", bids);

            workSchedules = workScheduleService.employeeSchedule(bids, LocalDateTimeUtil.of(estimatedEndTime));

            log.info("满足服务范围、网点的快递员排班：{}", workSchedules);
        }

        //2.1对满足服务范围、网点的快递员筛选排班
        if (CollUtil.isNotEmpty(workSchedules)) {
            courierIds = workSchedules.stream()
                    // 结束取件时间当天是否有排班
                    .map(WorkScheduleVO::getUserId)
                    .collect(Collectors.toList());
            log.info("服务范围、网点、排班均满足的快递员id有：{}", courierIds);
        }

        //3.存在同时满足服务范围、网点、排班的快递员，直接返回
        if (CollUtil.isNotEmpty(courierIds)) {
            return courierIds;
        }

        //3.1 如果服务范围内没有快递员，或服务范围内的快递员没有排班，则查询该网点的任一有排班快递员
        workSchedules = workScheduleService.employeeSchedule(null, LocalDateTimeUtil.of(estimatedEndTime));
        log.info("查询该网点所有快递员排班：{}", workSchedules);
        if (CollUtil.isEmpty(workSchedules)) {
            return courierIds;
        }

        //3.2对满足网点的快递员筛选排班
        courierIds = workSchedules.stream()
                // 结束取件时间当天是否有排班
                .map(WorkScheduleVO::getUserId)
                .collect(Collectors.toList());
        log.info("服务范围、网点、排班均满足的快递员id有：{}", courierIds);


        return courierIds;
    }
}
