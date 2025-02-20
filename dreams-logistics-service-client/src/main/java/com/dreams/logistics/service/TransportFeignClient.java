package com.dreams.logistics.service;

import com.dreams.logistics.enums.PickupDispatchTaskType;
import com.dreams.logistics.enums.StatusEnum;
import com.dreams.logistics.enums.UserRoleEnum;
import com.dreams.logistics.model.dto.transport.CourierTaskCountDTO;
import com.dreams.logistics.model.dto.truckPlan.TruckPlanDto;
import com.dreams.logistics.model.dto.workSchedule.WorkScheduleAddRequest;
import com.dreams.logistics.model.entity.Area;
import com.dreams.logistics.model.entity.DcUser;
import com.dreams.logistics.model.entity.Menu;
import com.dreams.logistics.model.entity.Organization;
import com.dreams.logistics.model.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;

import static com.dreams.logistics.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务
 *
 */
@FeignClient(name = "dreams-logistics-transport-service",path = "/api/transport/inner")
public interface TransportFeignClient {

    @PostMapping("/add")
    Boolean add(@RequestBody WorkScheduleAddRequest workScheduleAddRequest);

    /**
     * 获取未分配运输任务的车次计划列表
     * @return 未分配运输任务的车次计划列表
     */
    @PostMapping("/unassignedPlan")
    List<TruckPlanDto> pullUnassignedPlan(@RequestParam(name = "shardTotal") Integer shardTotal, @RequestParam(name = "shardIndex") Integer shardIndex);


    /**
     * 根据ID获取
     * @param id 数据库ID
     * @return 返回信息
     */
    @PostMapping("/{id}")
    TruckPlanDto findById(@PathVariable("id") Long id);

    /**
     * 计划完成
     * @param currentOrganId 结束机构id
     * @param planId          计划ID
     * @param truckId 车辆ID
     * @param statusEnum 车辆状态枚举
     * @return 车次与车辆和司机关联关系列表
     */
    @PostMapping("finished")
    void finished(@RequestParam("currentOrganId") Long currentOrganId, @RequestParam("planId") Long planId, @RequestParam("truckId") Long truckId, @RequestParam("statusEnum") StatusEnum statusEnum);



    @PostMapping("count")
    List<CourierTaskCountDTO> findCountByCourierIds(@RequestParam("courierIds") List<Long> courierIds,
                                                    @RequestParam("taskType") PickupDispatchTaskType taskType,
                                                    @RequestParam("date") String date);


    @PostMapping("/queryCourierIdListByCondition")
    List<Long> queryCourierIdListByCondition(@RequestParam("agencyId") Long agencyId,
                                             @RequestParam("longitude") Double longitude,
                                             @RequestParam("latitude") Double latitude,
                                             @RequestParam("estimatedEndTime") Long estimatedEndTime);

    @PostMapping("/area/id")
    Area findAreaById(@RequestParam("id") String id);
}


