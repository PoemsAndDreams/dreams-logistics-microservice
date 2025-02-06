package com.dreams.logistics.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreams.logistics.common.ErrorCode;
import com.dreams.logistics.entity.line.DispatchConfiguration;
import com.dreams.logistics.enums.DispatchMethodEnum;
import com.dreams.logistics.exception.BusinessException;

import com.dreams.logistics.entity.line.TransportLine;
import com.dreams.logistics.entity.node.AgencyEntity;
import com.dreams.logistics.entity.node.BaseEntity;
import com.dreams.logistics.entity.node.OLTEntity;
import com.dreams.logistics.entity.node.TLTEntity;

import com.dreams.logistics.enums.ExceptionEnum;
import com.dreams.logistics.enums.TransportLineEnum;
import com.dreams.logistics.model.dto.line.Organ;
import com.dreams.logistics.model.dto.line.TransportLineNode;
import com.dreams.logistics.model.dto.line.TransportLineSearch;
import com.dreams.logistics.repository.TransportLineRepository;
import com.dreams.logistics.service.CostConfigurationService;

import com.dreams.logistics.service.DispatchConfigurationService;
import com.dreams.logistics.service.OrganService;
import com.dreams.logistics.service.TransportLineService;
import com.dreams.logistics.utils.BaiduMap;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * 路线相关业务
 */
@Service
public class TransportLineServiceImpl implements TransportLineService {

    @Resource
    private TransportLineRepository transportLineRepository;

    @Resource
    private OrganService organService;

    @Resource
    private BaiduMap baiduMap;

    @Resource
    private CostConfigurationService costConfigurationService;


    @Resource
    private DispatchConfigurationService dispatchConfigurationService;

    // 新增路线业务规则：干线：起点终点无顺序，支线：起点必须是二级转运中心，接驳路线：起点必须是网点
    @Override
    public Boolean createLine(TransportLine transportLine) {
        TransportLineEnum transportLineEnum = TransportLineEnum.codeOf(transportLine.getType());
        if (null == transportLineEnum) {
            throw new BusinessException(ExceptionEnum.TRANSPORT_LINE_TYPE_ERROR);
        }

        if (ObjectUtil.equal(transportLine.getStartOrganId(), transportLine.getEndOrganId())) {
            //起点终点不能相同
            throw new BusinessException(ExceptionEnum.TRANSPORT_LINE_ORGAN_CANNOT_SAME);
        }

        BaseEntity firstNode;
        BaseEntity secondNode;
        switch (transportLineEnum) {
            case TRUNK_LINE: {
                // 干线
                firstNode = OLTEntity.builder().bid(transportLine.getStartOrganId()).build();
                secondNode = OLTEntity.builder().bid(transportLine.getEndOrganId()).build();
                break;
            }
            case BRANCH_LINE: {
                // 支线，起点必须是 二级转运中心
                firstNode = TLTEntity.builder().bid(transportLine.getStartOrganId()).build();
                secondNode = OLTEntity.builder().bid(transportLine.getEndOrganId()).build();
                break;
            }
            case CONNECT_LINE: {
                // 接驳路线，起点必须是 网点
                firstNode = AgencyEntity.builder().bid(transportLine.getStartOrganId()).build();
                secondNode = TLTEntity.builder().bid(transportLine.getEndOrganId()).build();
                break;
            }
            default: {
                throw new BusinessException(ExceptionEnum.TRANSPORT_LINE_TYPE_ERROR);
            }
        }

        if (ObjectUtil.hasEmpty(firstNode, secondNode)) {
            throw new BusinessException(ExceptionEnum.START_END_ORGAN_NOT_FOUND);
        }

        //判断路线是否已经存在
        Long count = this.transportLineRepository.queryCount(firstNode, secondNode);
        if (count > 0) {
            throw new BusinessException(ExceptionEnum.TRANSPORT_LINE_ALREADY_EXISTS);
        }

        transportLine.setId(null);
        transportLine.setCreated(System.currentTimeMillis());
        transportLine.setUpdated(transportLine.getCreated());
        //补充信息
        this.infoFromMap(firstNode, secondNode, transportLine);

        count = this.transportLineRepository.create(firstNode, secondNode, transportLine);
        return count > 0;
    }

    /**
     * 通过地图查询距离、时间，计算成本
     *
     * @param firstNode     开始节点
     * @param secondNode    结束节点
     * @param transportLine 路线对象
     */
    private void infoFromMap(BaseEntity firstNode, BaseEntity secondNode, TransportLine transportLine) {
        //查询节点数据
        Organ startOrgan = this.organService.findByBid(firstNode.getBid());
        if (ObjectUtil.hasEmpty(startOrgan, startOrgan.getLongitude(), startOrgan.getLatitude())) {
            throw new BusinessException(ErrorCode.ORGANIZATION_INFORMATION_FIRST);
        }
        Organ endOrgan = this.organService.findByBid(secondNode.getBid());
        if (ObjectUtil.hasEmpty(endOrgan, endOrgan.getLongitude(), endOrgan.getLatitude())) {
            throw new BusinessException(ErrorCode.ORGANIZATION_INFORMATION_FIRST);
        }

        //查询地图服务商
//        Coordinate origin = new Coordinate(startOrgan.getLongitude(), startOrgan.getLatitude());
//        Coordinate destination = new Coordinate(endOrgan.getLongitude(), endOrgan.getLatitude());

        String driving = "";
        //设置地图参数
        try {
            driving = baiduMap.directionLiteByDriving(startOrgan.getLatitude() + "," + startOrgan.getLongitude(), endOrgan.getLatitude() + "," + endOrgan.getLongitude());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (StrUtil.isEmpty(driving)) {
            return;
        }
        JSONObject jsonObject = JSONUtil.parseObj(driving);
        //时间，单位：秒
        //方法一
//        long duration = jsonObject.getJSONObject("result")
//                .getJSONArray("routes")
//                .getJSONObject(0)
//                .getLong("duration");

        Long duration = Convert.toLong(jsonObject.getByPath("result.routes[0].duration"), -1L);


        transportLine.setTime(duration);
        //距离，单位：米
        Double distance = Convert.toDouble(jsonObject.getByPath("result.routes[0].distance"), -1d);

        transportLine.setDistance(NumberUtil.round(distance, 0).doubleValue());

        // 总成本 = 每公里平均成本 * 距离（单位：米） / 1000
        Double cost = costConfigurationService.findCostByType(transportLine.getType());
        transportLine.setCost(NumberUtil.round(cost * distance / 1000, 2).doubleValue());
    }

    @Override
    public Boolean updateLine(TransportLine transportLine) {
        // 先查后改
        TransportLine transportLineData = this.queryById(transportLine.getId());
        if (null == transportLineData) {
            throw new BusinessException(ExceptionEnum.TRANSPORT_LINE_NOT_FOUND);
        }

        // 比较不可修改的字段
        if (hasFieldsChanged(transportLine, transportLineData)) {

            throw new BusinessException(ExceptionEnum.FIELD_CANNOT_BE_CHANGED);
        }

        //拷贝数据，忽略null值以及不能修改的字段
        BeanUtil.copyProperties(transportLine, transportLineData, CopyOptions.create().setIgnoreNullValue(true)
                .setIgnoreProperties("type", "startOrganId", "startOrganName", "endOrganId", "endOrganName"));

        transportLineData.setUpdated(System.currentTimeMillis());
        Long count = this.transportLineRepository.update(transportLineData);
        return count > 0;
    }

    private boolean hasFieldsChanged(TransportLine transportLine, TransportLine transportLineData) {
        // 比较不可修改字段是否发生变化
        return !Objects.equals(transportLine.getType(), transportLineData.getType()) ||
                !Objects.equals(transportLine.getStartOrganId(), transportLineData.getStartOrganId()) ||
                !Objects.equals(transportLine.getStartOrganName(), transportLineData.getStartOrganName()) ||
                !Objects.equals(transportLine.getEndOrganId(), transportLineData.getEndOrganId()) ||
                !Objects.equals(transportLine.getEndOrganName(), transportLineData.getEndOrganName());
    }

    @Override
    public Boolean deleteLine(Long id) {
        Long count = this.transportLineRepository.remove(id);
        return count > 0;
    }

    @Override
    public Page<TransportLine> queryPageList(TransportLineSearch transportLineSearch) {
        return this.transportLineRepository.queryPageList(transportLineSearch);
    }

    @Override
    public TransportLineNode queryShortestPath(Long startId, Long endId) {
        AgencyEntity start = AgencyEntity.builder().bid(startId).build();
        AgencyEntity end = AgencyEntity.builder().bid(endId).build();
        if (ObjectUtil.hasEmpty(start, end)) {
            throw new BusinessException(ExceptionEnum.START_END_ORGAN_NOT_FOUND);
        }
        return this.transportLineRepository.findShortestPath(start, end);
    }

    @Override
    public TransportLineNode findLowestPath(Long startId, Long endId) {
        AgencyEntity start = AgencyEntity.builder().bid(startId).build();
        AgencyEntity end = AgencyEntity.builder().bid(endId).build();

        if (ObjectUtil.hasEmpty(start, end)) {
            throw new BusinessException(ExceptionEnum.START_END_ORGAN_NOT_FOUND);
        }

        List<TransportLineNode> pathList = this.transportLineRepository.findPathList(start, end, 10, 1);
        if (CollUtil.isNotEmpty(pathList)) {
            return pathList.get(0);
        }
        return null;
    }

    @Override
    public List<TransportLine> queryByIds(Long... ids) {
        return this.transportLineRepository.queryByIds(ids);
    }

    @Override
    public TransportLine queryById(Long id) {
        return this.transportLineRepository.queryById(id);
    }

    /**
     * 根据调度策略查询路线
     *
     * @param startId 开始网点id
     * @param endId   结束网点id
     * @return 路线
     */
    @Override
    public TransportLineNode queryPathByDispatchMethod(Long startId, Long endId) {
        //调度方式配置
        DispatchConfiguration configuration = this.dispatchConfigurationService.findConfiguration();
        int method = configuration.getDispatchMethod();

        //调度方式，1转运次数最少，2成本最低
        if (ObjectUtil.equal(DispatchMethodEnum.SHORTEST_PATH.getCode(), method)) {
            return this.queryShortestPath(startId, endId);
        } else {
            return this.findLowestPath(startId, endId);
        }
    }

}
