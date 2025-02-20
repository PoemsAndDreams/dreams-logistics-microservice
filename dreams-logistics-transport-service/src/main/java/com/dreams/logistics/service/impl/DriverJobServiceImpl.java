package com.dreams.logistics.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreams.logistics.common.Constants;
import com.dreams.logistics.common.ErrorCode;
import com.dreams.logistics.constant.CommonConstant;
import com.dreams.logistics.enums.*;
import com.dreams.logistics.exception.BusinessException;
import com.dreams.logistics.model.dto.driverJob.DriverDeliverDTO;
import com.dreams.logistics.model.dto.driverJob.DriverJobQueryRequest;
import com.dreams.logistics.model.dto.driverJob.DriverPickUpDTO;
import com.dreams.logistics.model.dto.driverJob.DriverReturnRegisterDTO;
import com.dreams.logistics.model.dto.transport.TransportTaskDTO;
import com.dreams.logistics.model.dto.transport.request.TransportTaskCompleteDTO;
import com.dreams.logistics.model.dto.transport.request.TransportTaskStartDTO;
import com.dreams.logistics.model.dto.truckPlan.TruckDto;
import com.dreams.logistics.model.dto.truckPlan.TruckPlanDto;
import com.dreams.logistics.model.entity.DriverJob;
import com.dreams.logistics.model.entity.Organization;
import com.dreams.logistics.model.vo.DriverJobVO;
import com.dreams.logistics.service.*;
import com.dreams.logistics.mapper.DriverJobMapper;
import com.dreams.logistics.utils.ObjectUtil;
import com.dreams.logistics.utils.SqlUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author xiayutian
* @description 针对表【driver_job(司机作业单)】的数据库操作Service实现
* @createDate 2025-02-12 22:22:02
*/
@Service
public class DriverJobServiceImpl extends ServiceImpl<DriverJobMapper, DriverJob>
    implements DriverJobService{

    @Resource
    private TransportOrderService transportOrderService;
    @Resource
    private TransportTaskService transportTaskService;
    @Resource
    private TruckPlanService truckPlanService;
    @Resource
    private TruckService truckService;
    @Resource
    private RedissonClient redissonClient;

    @Resource
    private UserFeignClient organFeign;


    @Override
    @Transactional
    public Long createDriverJob(Long transportTaskId, Long driverId) {
        //查询运输任务
        TransportTaskDTO transportTaskDTO = this.transportTaskService.findById(transportTaskId);
        if (ObjectUtil.isEmpty(transportTaskDTO)) {
            throw new BusinessException(DriverExceptionEnum.TRANSPORT_TASK_NOT_FOUND);
        }

        DriverJob driverJobEntity = new DriverJob();
        driverJobEntity.setDriverId(driverId);
        driverJobEntity.setStartAgencyId(transportTaskDTO.getStartAgencyId());
        driverJobEntity.setEndAgencyId(transportTaskDTO.getEndAgencyId());
        driverJobEntity.setStatus(DriverJobStatus.PENDING);
        driverJobEntity.setTransportTaskId(transportTaskId);

        //根据车辆计划id查询预计发车时间和预计到达时间
        TruckPlanDto truckPlanDto = this.truckPlanService.findById(transportTaskDTO.getTruckPlanId());
        driverJobEntity.setPlanDepartureTime(truckPlanDto.getPlanDepartureTime()); //计划发车时间
        driverJobEntity.setPlanArrivalTime(truckPlanDto.getPlanArrivalTime()); //实际到达时间

        boolean result = super.save(driverJobEntity);

        if (result) {
            //构建消息内容
            TruckDto truckDto = truckService.fineById(transportTaskDTO.getTruckId());

            Organization startOrgan = organFeign.getOrganizationById(String.valueOf(transportTaskDTO.getStartAgencyId()));
            Organization endOrgan = organFeign.getOrganizationById(String.valueOf(transportTaskDTO.getEndAgencyId()));

            String content = CharSequenceUtil.format("运输车辆：{}\n运输路线：{}——{}", truckDto.getLicensePlate(), startOrgan.getName(), endOrgan.getName());

            //构建消息对象
            //todo 您有新的运输任务！

            return driverJobEntity.getId();
        }
        throw new BusinessException(DriverExceptionEnum.DRIVER_JOB_SAVE_ERROR);
    }

    @Override
    public boolean updateStatus(Long id, DriverJobStatus status) {
        DriverJob driverJobEntity = new DriverJob();
        driverJobEntity.setId(id);
        switch (status) {
            case PENDING: {
                throw new BusinessException(DriverExceptionEnum.DRIVER_JOB_STATUS_NOT_PENDING);
            }
            case PROCESSING: {
                //司机出库
                DriverPickUpDTO driverPickUpDTO = new DriverPickUpDTO();
                driverPickUpDTO.setId(String.valueOf(id));
                this.outStorage(driverPickUpDTO);
                break;
            }
            case CONFIRM: {
                //改派，暂时只做状态修改处理
                driverJobEntity.setStatus(DriverJobStatus.CONFIRM);
                break;
            }
            case DELIVERED: {
                //司机入库
                DriverDeliverDTO driverDeliverDTO = new DriverDeliverDTO();
                driverDeliverDTO.setId(String.valueOf(id));
                this.intoStorage(driverDeliverDTO);
                break;
            }
            case CANCELLED: {
                //已作废，暂时只做状态修改处理
                driverJobEntity.setStatus(DriverJobStatus.CANCELLED);
                break;
            }
            default: {
                break;
            }
        }

        if (ObjectUtil.isNotEmpty(driverJobEntity.getStatus())) {
            //更新状态
            return super.updateById(driverJobEntity);
        }

        return true;
    }

    @Override
    public void returnRegister(DriverReturnRegisterDTO driverReturnRegisterDTO) {
        //更新关联运输任务id的司机作业单状态为已完成
        LambdaUpdateWrapper<DriverJob> updateWrapper = Wrappers.<DriverJob>lambdaUpdate()
                .eq(DriverJob::getTransportTaskId, driverReturnRegisterDTO.getId())
                .set(DriverJob::getStatus, DriverJobStatus.COMPLETED);
        this.update(updateWrapper);

        //根据id查询运输任务
        TransportTaskDTO transportTaskDTO = transportTaskService.findById(Long.valueOf(driverReturnRegisterDTO.getId()));


        //修改车辆状态
        truckPlanService.finishedPlan(transportTaskDTO.getStartAgencyId(), transportTaskDTO.getTruckPlanId(), transportTaskDTO.getTruckId(), StatusEnum.NORMAL);

    }

    /**
     * 司机入库，修改运单的当前节点和下个节点 以及 修改运单为待调度状态，结束运输任务
     *
     * @param driverDeliverDTO 司机作业单id
     */
    @Override
    public void intoStorage(DriverDeliverDTO driverDeliverDTO) {
        //1.司机作业单，获取运输任务id
        DriverJob driverJob = super.getById(driverDeliverDTO.getId());
        if (ObjectUtil.isEmpty(driverJob)) {
            throw new BusinessException(DriverExceptionEnum.DRIVER_JOB_NOT_FOUND);
        }
        if (ObjectUtil.notEqual(driverJob.getStatus(), DriverJobStatus.PROCESSING)) {
            throw new BusinessException(DriverExceptionEnum.DRIVER_JOB_STATUS_UNKNOWN);
        }

        //运输任务id
        Long transportTaskId = driverJob.getTransportTaskId();

        //2.更新运输任务状态为完成
        //加锁，只能有一个司机操作，任务已经完成的话，就不需要进行流程流转，只要完成司机自己的作业单即可
        String lockRedisKey = Constants.LOCKS.DRIVER_JOB_LOCK_PREFIX + transportTaskId;
        //2.1获取锁
        RLock lock = this.redissonClient.getFairLock(lockRedisKey);
        if (lock.tryLock()) {
            //2.2获取到锁
            try {
                //2.3查询运输任务
                TransportTaskDTO transportTask = this.transportTaskService.findById(transportTaskId);
                //2.4判断任务是否已结束，不能再修改流转
                if (ObjectUtil.equalsAny(transportTask.getStatus(), TransportTaskStatus.CANCELLED, TransportTaskStatus.COMPLETED)) {
                    return;
                }

                //2.5修改运单流转节点，修改当前节点和下一个节点
                this.transportOrderService.updateByTaskId(transportTaskId);

                //2.6结束运输任务
                TransportTaskCompleteDTO transportTaskCompleteDTO = BeanUtil.toBean(driverDeliverDTO, TransportTaskCompleteDTO.class);
                transportTaskCompleteDTO.setTransportTaskId(String.valueOf(transportTaskId));
                this.transportTaskService.completeTransportTask(transportTaskCompleteDTO);
            } finally {
                lock.unlock();
            }
        } else {
            throw new BusinessException(DriverExceptionEnum.DRIVER_JOB_INTO_STORAGE_ERROR);
        }

        //3.修改所有与运输任务id相关联的司机作业单状态和实际到达时间
        LambdaUpdateWrapper<DriverJob> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ObjectUtil.isNotEmpty(transportTaskId), DriverJob::getTransportTaskId, transportTaskId)
                .set(DriverJob::getStatus, DriverJobStatus.DELIVERED)
                .set(DriverJob::getActualArrivalTime, LocalDateTime.now());
        this.update(updateWrapper);
    }

    /**
     * 司机出库，修改运单为运输中状态，开始运输任务
     *
     * @param driverPickUpDTO 司机作业单id
     */
    @Override
    public void outStorage(DriverPickUpDTO driverPickUpDTO) {
        //1.司机作业单，获取运输任务id
        DriverJob driverJob = super.getById(driverPickUpDTO.getId());
        if (ObjectUtil.isEmpty(driverJob)) {
            throw new BusinessException(DriverExceptionEnum.DRIVER_JOB_NOT_FOUND);
        }
        if (ObjectUtil.notEqual(driverJob.getStatus(), DriverJobStatus.PENDING)) {
            throw new BusinessException(DriverExceptionEnum.DRIVER_JOB_STATUS_UNKNOWN);
        }

        //查询当前司机是否有在途或交付任务，如有则不可提货
        long count = this.getDriverJobCountProcessing(driverJob.getDriverId());
        if (count > 0) {
            throw new BusinessException(DriverExceptionEnum.PROCESSING_DRIVER_JOB_NOT_EMPTY);
        }

        //运输任务id
        Long transportTaskId = driverJob.getTransportTaskId();

        //2.更新运输任务和运输运单
        //加锁，只能有一个司机操作，任务已经为在途的话，就不需要进行流程流转，只要完成司机自己的作业单即可
        String lockRedisKey = Constants.LOCKS.DRIVER_JOB_LOCK_PREFIX + transportTaskId;
        //2.1获取锁
        RLock lock = this.redissonClient.getFairLock(lockRedisKey);
        if (lock.tryLock()) {
            //2.2锁定
            try {
                //2.3查询运输任务
                TransportTaskDTO transportTask = this.transportTaskService.findById(transportTaskId);
                //2.4判断任务是否正在进行，不能再修改流转
                if (ObjectUtil.equalsAny(transportTask.getStatus(), TransportTaskStatus.PROCESSING, TransportTaskStatus.CONFIRM)) {
                    return;
                }

                //2.5修改运单状态为 运输中
                List<String> transportOrderIdList = this.transportTaskService.queryTransportOrderIdListById(transportTaskId);
                this.transportOrderService.updateStatus(transportOrderIdList, TransportOrderStatus.PROCESSING);

                //2.6开始运输任务
                TransportTaskStartDTO transportTaskStartDTO = BeanUtil.toBean(driverPickUpDTO, TransportTaskStartDTO.class);
                transportTaskStartDTO.setTransportTaskId(String.valueOf(transportTaskId));
                this.transportTaskService.startTransportTask(transportTaskStartDTO);
            } finally {
                lock.unlock();
            }
        } else {
            throw new BusinessException(DriverExceptionEnum.DRIVER_JOB_OUT_STORAGE_ERROR);
        }

        //3.修改所有与运输任务id相关联的司机作业单状态和实际出发时间
        LambdaUpdateWrapper<DriverJob> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ObjectUtil.isNotEmpty(transportTaskId), DriverJob::getTransportTaskId, transportTaskId)
                .set(DriverJob::getStatus, DriverJobStatus.PROCESSING)
                .set(DriverJob::getActualDepartureTime, LocalDateTime.now());
        this.update(updateWrapper);

        //4.修改车辆状态为运输中,调用base服务，需要注意事务问题
        TransportTaskDTO transportTask = this.transportTaskService.findById(transportTaskId);
        this.truckService.updateRunStatus(transportTask.getTruckId(), TruckRunStatusEnum.RUNNING);
    }

    /**
     * 查询当前司机的在途和交付任务数量
     *
     * @param driverId 司机id
     * @return 数量
     */
    private long getDriverJobCountProcessing(Long driverId) {
        LambdaQueryWrapper<DriverJob> countQueryWrapper = Wrappers.<DriverJob>lambdaQuery()
                .eq(ObjectUtil.isNotEmpty(driverId), DriverJob::getDriverId, driverId)
                .in(DriverJob::getStatus, ListUtil.of(DriverJobStatus.PROCESSING, DriverJobStatus.DELIVERED));
        return this.count(countQueryWrapper);
    }



    @Override
    public boolean removeByTransportTaskId(Long transportTaskId) {
        LambdaQueryWrapper<DriverJob> queryWrapper = Wrappers.<DriverJob>lambdaQuery()
                .eq(DriverJob::getTransportTaskId, transportTaskId);
        return super.remove(queryWrapper);
    }

    @Override
    public Wrapper<DriverJob> getQueryWrapper(DriverJobQueryRequest driverJobQueryRequest) {

        if (driverJobQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = driverJobQueryRequest.getId();
        Long startAgencyId = driverJobQueryRequest.getStartAgencyId();
        Long endAgencyId = driverJobQueryRequest.getEndAgencyId();
        Integer status = driverJobQueryRequest.getStatus();
        Long driverId = driverJobQueryRequest.getDriverId();
        Long transportTaskId = driverJobQueryRequest.getTransportTaskId();
        String startHandover = driverJobQueryRequest.getStartHandover();
        String finishHandover = driverJobQueryRequest.getFinishHandover();
        Date planDepartureTime = driverJobQueryRequest.getPlanDepartureTime();
        Date actualDepartureTime = driverJobQueryRequest.getActualDepartureTime();
        Date planArrivalTime = driverJobQueryRequest.getPlanArrivalTime();
        Date actualArrivalTime = driverJobQueryRequest.getActualArrivalTime();

        String sortField = driverJobQueryRequest.getSortField();
        String sortOrder = driverJobQueryRequest.getSortOrder();
        
        
        QueryWrapper<DriverJob> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(startAgencyId != null, "start_agency_id", startAgencyId);
        queryWrapper.eq(endAgencyId != null, "end_agency_id", endAgencyId);
        queryWrapper.eq(status != null, "status", status);
        queryWrapper.eq(driverId != null, "driver_id", driverId);
        queryWrapper.eq(transportTaskId != null, "transport_task_id", transportTaskId);
        queryWrapper.eq(planDepartureTime != null, "plan_departure_time", planDepartureTime);
        queryWrapper.eq(actualDepartureTime != null, "actual_departure_time", actualDepartureTime);
        queryWrapper.eq(planArrivalTime != null, "plan_arrival_time", planArrivalTime);
        queryWrapper.eq(actualArrivalTime != null, "actual_arrival_time", actualArrivalTime);
        
        
        queryWrapper.eq(StringUtils.isNotBlank(startHandover), "start_handover", startHandover);
        queryWrapper.eq(StringUtils.isNotBlank(finishHandover), "finish_handover", finishHandover);

        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
        
    }

    
    @Override
    public DriverJobVO getDriverJobVO(DriverJob driverJob) {
        if (driverJob == null) {
            return null;
        }
        DriverJobVO driverJobVO = new DriverJobVO();
        BeanUtils.copyProperties(driverJob, driverJobVO);
        return driverJobVO;
    }

    @Override
    public List<DriverJobVO> getDriverJobVO(List<DriverJob> driverJobList) {
        if (CollUtil.isEmpty(driverJobList)) {
            return new ArrayList<>();
        }
        return driverJobList.stream().map(driverJob -> {
            DriverJobVO bean = BeanUtil.toBean(driverJob, DriverJobVO.class);
            bean.setStatus(driverJob.getStatus().getCode());
            return bean;
        }).collect(Collectors.toList());
    }


}




