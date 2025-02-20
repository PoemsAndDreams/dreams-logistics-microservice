package com.dreams.logistics.mapper;

import com.dreams.logistics.model.dto.transport.CourierTaskCountDTO;
import com.dreams.logistics.model.entity.PickupDispatchTask;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
* @author xiayutian
* @description 针对表【pickup_dispatch_task(取件、派件任务信息表)】的数据库操作Mapper
* @createDate 2025-02-16 20:10:20
* @Entity com.dreams.logistics.model.entity.PickupDispatchTask
*/
public interface PickupDispatchTaskMapper extends BaseMapper<PickupDispatchTask> {
    List<CourierTaskCountDTO> findCountByCourierIds(@Param("courierIds") List<Long> courierIds,
                                                    @Param("type") Integer type,
                                                    @Param("startDateTime") LocalDateTime startDateTime,
                                                    @Param("endDateTime") LocalDateTime endDateTime);


}




