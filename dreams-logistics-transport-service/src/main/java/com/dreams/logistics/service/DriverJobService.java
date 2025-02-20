package com.dreams.logistics.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.dreams.logistics.enums.DriverJobStatus;
import com.dreams.logistics.model.dto.driverJob.DriverDeliverDTO;
import com.dreams.logistics.model.dto.driverJob.DriverJobQueryRequest;
import com.dreams.logistics.model.dto.driverJob.DriverPickUpDTO;
import com.dreams.logistics.model.dto.driverJob.DriverReturnRegisterDTO;
import com.dreams.logistics.model.entity.DriverJob;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dreams.logistics.model.vo.DriverJobVO;

import java.util.List;

/**
* @author xiayutian
* @description 针对表【driver_job(司机作业单)】的数据库操作Service
* @createDate 2025-02-12 22:22:02
*/
public interface DriverJobService extends IService<DriverJob> {

    Long createDriverJob(Long transportTaskId, Long aLong);

    void intoStorage(DriverDeliverDTO driverDeliverDTO);

    void outStorage(DriverPickUpDTO driverPickUpDTO);

    boolean removeByTransportTaskId(Long transportTaskId);

    Wrapper<DriverJob> getQueryWrapper(DriverJobQueryRequest driverJobQueryRequest);

    DriverJobVO getDriverJobVO(DriverJob driverJob);

    List<DriverJobVO> getDriverJobVO(List<DriverJob> records);

    boolean updateStatus(Long id, DriverJobStatus status);

    void returnRegister(DriverReturnRegisterDTO driverReturnRegisterDTO);
}
