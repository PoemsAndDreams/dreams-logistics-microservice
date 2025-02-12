package com.dreams.logistics.controller.inner;

import com.dreams.logistics.model.dto.workSchedule.WorkScheduleAddRequest;
import com.dreams.logistics.model.entity.Organization;
import com.dreams.logistics.service.TransportFeignClient;
import com.dreams.logistics.service.WorkScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author PoemsAndDreams
 * @date 2025-02-10 20:50
 * @description //TODO
 */
@RestController
@RequestMapping("/inner")
@Slf4j
public class TransportInnerController implements TransportFeignClient {

    @Resource
    private WorkScheduleService workScheduleService;

    @Override
    @PostMapping("/add")
    public Boolean add(@RequestBody WorkScheduleAddRequest workScheduleAddRequest) {
        return workScheduleService.saveWorkSchedule(workScheduleAddRequest);
    }
}
