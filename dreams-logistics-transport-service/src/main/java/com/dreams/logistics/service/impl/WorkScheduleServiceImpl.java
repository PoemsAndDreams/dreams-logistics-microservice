package com.dreams.logistics.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreams.logistics.common.ErrorCode;
import com.dreams.logistics.constant.CommonConstant;
import com.dreams.logistics.exception.BusinessException;
import com.dreams.logistics.model.dto.workSchedule.WorkScheduleAddRequest;
import com.dreams.logistics.model.dto.workSchedule.WorkScheduleQueryRequest;
import com.dreams.logistics.model.dto.workSchedule.WorkScheduleUpdateRequest;
import com.dreams.logistics.model.entity.DcUser;
import com.dreams.logistics.model.entity.WorkSchedule;
import com.dreams.logistics.model.vo.WorkScheduleVO;
import com.dreams.logistics.service.WorkScheduleService;
import com.dreams.logistics.mapper.WorkScheduleMapper;
import com.dreams.logistics.utils.DateUtils;
import com.dreams.logistics.utils.SqlUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author xiayutian
 * @description 针对表【work_schedule】的数据库操作Service实现
 * @createDate 2025-02-07 08:07:45
 */
@Service
public class WorkScheduleServiceImpl extends ServiceImpl<WorkScheduleMapper, WorkSchedule>
        implements WorkScheduleService {

    @Override
    public boolean saveWorkSchedule(WorkScheduleAddRequest workScheduleAddRequest) {
        WorkSchedule workSchedule = new WorkSchedule();
        BeanUtils.copyProperties(workScheduleAddRequest, workSchedule);

        List<Integer> weekScheduleList = workScheduleAddRequest.getWeekSchedule();

        // 将 weekScheduleList 转换为一个 int 类型的二进制数
        int weekScheduleInt = 0;
        for (int i = 0; i < weekScheduleList.size(); i++) {
            if (weekScheduleList.get(i) == 1) {
                weekScheduleInt |= (1 << i);  // 将对应位设置为 1
            }
        }

        // 设置转换后的 weekSchedule 到 WorkSchedule 实体中
        workSchedule.setWeekSchedule(weekScheduleInt);

        return this.save(workSchedule);

    }

    @Override
    public boolean updateWorkSchedule(WorkScheduleUpdateRequest workScheduleUpdateRequest) {
        LambdaQueryWrapper<WorkSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkSchedule::getUserId,workScheduleUpdateRequest.getUserId());
        // 获取 WorkSchedule 实体
        WorkSchedule workSchedule = this.getOne(wrapper);

        List<Integer> weekScheduleList = workScheduleUpdateRequest.getWeekSchedule();

        // 将 weekScheduleList 转换为一个 int 类型的二进制数
        int weekScheduleInt = 0;
        for (int i = 0; i < weekScheduleList.size(); i++) {
            if (weekScheduleList.get(i) == 1) {
                weekScheduleInt |= (1 << i);  // 将对应位设置为 1
            }
        }
        // 设置转换后的 weekSchedule 到 WorkSchedule 实体中
        workSchedule.setWeekSchedule(weekScheduleInt);

        return this.updateById(workSchedule);
    }

    @Override
    public WorkScheduleVO getWorkSchedule(long id) {
        LambdaQueryWrapper<WorkSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkSchedule::getUserId,id);
        // 获取 WorkSchedule 实体
        WorkSchedule workSchedule = this.getOne(wrapper);
        WorkScheduleVO workScheduleVO = new WorkScheduleVO();
        BeanUtils.copyProperties(workSchedule, workScheduleVO);

        // 获取存储的 weekSchedule (二进制表示的整数)
        int weekScheduleInt = workSchedule.getWeekSchedule();

        // 将 weekScheduleInt 转换为 List<Integer>（一周7天排班）
        List<Integer> weekScheduleList = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            // 使用位运算获取对应天的排班状态
            int dayStatus = (weekScheduleInt >> i) & 1;  // 获取第 i 天的状态，0 或 1
            weekScheduleList.add(dayStatus);
        }

        // 将转换后的数据封装到 WorkScheduleVO 对象中

        workScheduleVO.setWeekSchedule(weekScheduleList);

        return workScheduleVO;
    }

    @Override
    public Wrapper<WorkSchedule> getQueryWrapper(WorkScheduleQueryRequest workScheduleQueryRequest) {
        if (workScheduleQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        Long userId = workScheduleQueryRequest.getUserId();
        String sortField = workScheduleQueryRequest.getSortField();
        String sortOrder = workScheduleQueryRequest.getSortOrder();


        QueryWrapper<WorkSchedule> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(userId != null, "user_id", userId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;

    }

    @Override
    public List<WorkScheduleVO> getWorkScheduleVO(List<WorkSchedule> records) {

        List<WorkScheduleVO> list = records.stream().map(workSchedule -> {
            return getWorkScheduleVO(workSchedule);
        }).collect(Collectors.toList());

        return list;
    }

    @Override
    public WorkScheduleVO getWorkScheduleVO(WorkSchedule workSchedule) {

        // 获取存储的 weekSchedule (二进制表示的整数)
        int weekScheduleInt = workSchedule.getWeekSchedule();

        // 将 weekScheduleInt 转换为 List<Integer>（一周7天排班）
        List<Integer> weekScheduleList = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            // 使用位运算获取对应天的排班状态
            int dayStatus = (weekScheduleInt >> i) & 1;  // 获取第 i 天的状态，0 或 1
            weekScheduleList.add(dayStatus);
        }

        // 将转换后的数据封装到 WorkScheduleVO 对象中
        WorkScheduleVO workScheduleVO = new WorkScheduleVO();
        BeanUtils.copyProperties(workSchedule,workScheduleVO);
        workScheduleVO.setWeekSchedule(weekScheduleList);
        return workScheduleVO;

    }

    /**
     * 获取整个计划（运输任务）期间每一天都上班的司机
     *
     * @param driverIds         司机ID列表
     * @param planDepartureTime 计划发车时间
     * @param planArrivalTime   计划到达时间
     * @return 正常上班的司机ID列表
     */
    @Override
    public List<Long> getWorkingDrivers(List<Long> driverIds, LocalDateTime planDepartureTime, LocalDateTime planArrivalTime) {
        // 查询排班
        LambdaQueryWrapper<WorkSchedule> queryWrapper = Wrappers.<WorkSchedule>lambdaQuery()
                // 司机ID
                .in(WorkSchedule::getUserId, driverIds);
        List<WorkSchedule> list = list(queryWrapper);

        // 过滤整个计划（运输任务）期间每一天都上班的司机
        List<Long> workUserIds = list.stream()
                .filter(workScheduleEntity -> {
                    // 按照一年中365天 天数从小到达遍历 比如 从2020年1月1日 到2020年1月5日
                    for (LocalDateTime count = planDepartureTime; count.isBefore(planArrivalTime); count = count.plusDays(1)) {
                        // 转换为该日期是星期几，通常可以用 count.getDayOfWeek()
                        int dayOfWeek = count.getDayOfWeek().getValue(); // 1 = Monday, 7 = Sunday

                        // 获取司机的每周排班情况
                        Integer weekSchedule = workScheduleEntity.getWeekSchedule();

                        // 检查司机是否在该日期上班（假设 weekSchedule 是一个 7 位二进制数，每一位代表一天，1 表示上班，0 表示不上班）
                        // `dayOfWeek - 1` 是因为 Java 中星期一是 1，星期天是 7，而位运算从 0 开始
                        boolean worked = (weekSchedule & (1 << (dayOfWeek - 1))) != 0;
                        // 如果某一天不工作，则过滤掉该司机
                        if (!worked) {
                            return false;
                        }

                    }
                    // 整个计划（运输任务）期间每一天都上班的司机
                    return true;
                }).map(WorkSchedule::getUserId).collect(Collectors.toList());

        if (workUserIds.size() <= 2) {
            return workUserIds;
        }
        return workUserIds.subList(0, 2);
    }

    @Override
    public List<WorkScheduleVO> employeeSchedule(List<Long> userIds,LocalDateTime estimatedEndTime) {

        LambdaQueryWrapper<WorkSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(CollUtil.isNotEmpty(userIds), WorkSchedule::getUserId, userIds);
        List<WorkSchedule> list = this.list(wrapper);

        List<WorkScheduleVO> workScheduleVO = getWorkScheduleVO(list);

        // 获取估算结束时间的周几
        int dayOfWeek = estimatedEndTime.getDayOfWeek().getValue();  // 1表示星期一，7表示星期日

        // 遍历每个排班，筛选出在该时间段上班的记录
        return workScheduleVO.stream()
                .map(schedules -> {
                    // 获取该排班的每周工作安排
                    List<Integer> weekSchedule = schedules.getWeekSchedule();

                    // 判断该时间段是否上班
                    if (weekSchedule != null && weekSchedule.size() >= dayOfWeek && weekSchedule.get(dayOfWeek - 1) == 1) {
                        // 说明该日期需要工作，返回WorkSchedule
                        return schedules;
                    }
                    return null; // 没有工作安排的，返回null
                })
                .filter(Objects::nonNull) // 过滤掉null值
                .collect(Collectors.toList());

    }
}




