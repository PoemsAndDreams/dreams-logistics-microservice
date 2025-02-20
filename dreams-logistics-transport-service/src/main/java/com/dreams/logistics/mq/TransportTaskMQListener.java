package com.dreams.logistics.mq;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreams.logistics.common.Constants;
import com.dreams.logistics.entity.line.TransportLine;
import com.dreams.logistics.enums.TransportOrderSchedulingStatus;
import com.dreams.logistics.enums.TransportTaskAssignedStatus;
import com.dreams.logistics.enums.TransportTaskLoadingStatus;
import com.dreams.logistics.enums.TransportTaskStatus;
import com.dreams.logistics.model.dto.line.TransportLineDTO;
import com.dreams.logistics.model.dto.line.TransportLineSearch;
import com.dreams.logistics.model.dto.truckPlan.TruckPlanDto;
import com.dreams.logistics.model.entity.TransportOrder;
import com.dreams.logistics.model.entity.TransportOrderTask;
import com.dreams.logistics.model.entity.TransportTask;
import com.dreams.logistics.service.*;
import com.dreams.logistics.utils.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 接收运输任务消息，创建运输任务到数据库
 */
@Slf4j
@Component
public class TransportTaskMQListener {

    @Resource
    private TransportTaskService transportTaskService;
    @Resource
    private TransportOrderTaskService transportOrderTaskService;
    @Resource
    private TransportOrderService transportOrderService;
    @Resource
    private DriverJobService driverJobService;
    @Resource
    private TransportLineService transportLineService;
    @Resource
    private TruckPlanService truckPlanService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = Constants.MQ.Queues.WORK_TRANSPORT_TASK_CREATE),
            exchange = @Exchange(name = Constants.MQ.Exchanges.TRANSPORT_TASK, type = ExchangeTypes.TOPIC),
            key = Constants.MQ.RoutingKeys.TRANSPORT_TASK_CREATE
    ))
    public void listenTransportTaskMsg(String msg) {
        //解析消息 {"driverIds":[123,345], "truckPlanId":456, "truckId":1210114964812075008,"totalVolume":4.2,"endOrganId":90001,"totalWeight":7,"transportOrderIdList":[320733749248,420733749248],"startOrganId":100280}
        JSONObject jsonObject = JSONUtil.parseObj(msg);
        //获取到司机id列表
        JSONArray driverIds = jsonObject.getJSONArray("driverIds");
        // 分配状态
        TransportTaskAssignedStatus assignedStatus = CollUtil.isEmpty(driverIds) ? TransportTaskAssignedStatus.MANUAL_DISTRIBUTED : TransportTaskAssignedStatus.DISTRIBUTED;
        //创建运输任务
        Long transportTaskId = this.createTransportTask(jsonObject, assignedStatus);

        // 司机作业单
        if (CollUtil.isEmpty(driverIds)) {
            log.info("生成司机作业单，司机列表为空，需要手动设置司机作业单 -> msg = {}", msg);
            return;
        }
        for (Object driverId : driverIds) {
            //生成司机作业单
            this.driverJobService.createDriverJob(transportTaskId, Convert.toLong(driverId));
        }
    }

    @Transactional
    public Long createTransportTask(JSONObject jsonObject, TransportTaskAssignedStatus assignedStatus) {
        //创建运输任务
        TransportTask transportTaskEntity = new TransportTask();
        transportTaskEntity.setStatus(TransportTaskStatus.PENDING);
        transportTaskEntity.setAssignedStatus(assignedStatus);

        //根据车辆计划id查询预计发车时间和预计到达时间
        Long truckPlanId = jsonObject.getLong("truckPlanId");
        TruckPlanDto truckPlanDto = truckPlanService.findById(truckPlanId);
        transportTaskEntity.setTruckPlanId(jsonObject.getLong("truckPlanId"));
        transportTaskEntity.setTruckId(jsonObject.getStr("truckId"));
        transportTaskEntity.setStartAgencyId(jsonObject.getLong("startOrganId"));
        transportTaskEntity.setEndAgencyId(jsonObject.getLong("endOrganId"));
        transportTaskEntity.setTransportTripsId(jsonObject.getLong("transportTripsId"));

        transportTaskEntity.setPlanDepartureTime(truckPlanDto.getPlanDepartureTime()); //计划发车时间
        transportTaskEntity.setPlanArrivalTime(truckPlanDto.getPlanArrivalTime()); //计划到达时间

        if (CollUtil.isEmpty(jsonObject.getJSONArray("transportOrderIdList"))) {
            transportTaskEntity.setLoadingStatus(TransportTaskLoadingStatus.EMPTY);
        } else {
            transportTaskEntity.setLoadingStatus(TransportTaskLoadingStatus.FULL);
        }

        //查询路线距离
        TransportLineSearch transportLineSearch = new TransportLineSearch();
        transportLineSearch.setPage(1);
        transportLineSearch.setPageSize(1);
        transportLineSearch.setStartOrganId(transportTaskEntity.getStartAgencyId());
        transportLineSearch.setEndOrganId(transportTaskEntity.getEndAgencyId());
        Page<TransportLine> transportLineResponse = this.transportLineService.queryPageList(transportLineSearch);
        TransportLine transportLine = CollUtil.getFirst(transportLineResponse.getRecords());
        
        if (ObjectUtil.isNotEmpty(transportLine)) {
            //设置距离
            transportTaskEntity.setDistance(transportLine.getDistance());
        }

        //保存数据
        this.transportTaskService.save(transportTaskEntity);

        //创建运输任务与运单之间的关系
        this.createTransportOrderTask(transportTaskEntity.getId(), jsonObject);
        return transportTaskEntity.getId();
    }

    private void createTransportOrderTask(final Long transportTaskId, final JSONObject jsonObject) {
        //创建运输任务与运单之间的关系
        JSONArray transportOrderIdList = jsonObject.getJSONArray("transportOrderIdList");
        if (CollUtil.isEmpty(transportOrderIdList)) {
            return;
        }

        //将运单id列表转成运单实体列表
        List<TransportOrderTask> resultList = transportOrderIdList.stream()
                .map(o -> {
                    TransportOrderTask transportOrderTaskEntity = new TransportOrderTask();
                    transportOrderTaskEntity.setTransportTaskId(transportTaskId);
                    transportOrderTaskEntity.setTransportOrderId(Convert.toStr(o));
                    return transportOrderTaskEntity;
                }).collect(Collectors.toList());

        //批量保存运输任务与运单的关联表
        this.transportOrderTaskService.batchSaveTransportOrder(resultList);

        //批量标记运单为已调度状态
        List<TransportOrder> list = transportOrderIdList.stream()
                .map(o -> {
                    TransportOrder transportOrderEntity = new TransportOrder();
                    transportOrderEntity.setId(Convert.toStr(o));
                    //状态设置为已调度
                    transportOrderEntity.setSchedulingStatus(TransportOrderSchedulingStatus.SCHEDULED);
                    return transportOrderEntity;
                }).collect(Collectors.toList());
        this.transportOrderService.updateBatchById(list);
    }
}
