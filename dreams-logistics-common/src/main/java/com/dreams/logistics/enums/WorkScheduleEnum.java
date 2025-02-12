package com.dreams.logistics.enums;

import cn.hutool.core.util.EnumUtil;

import java.util.Arrays;
import java.util.List;

/**
 * 异常枚举
 */
public enum WorkScheduleEnum  {

    YES_STATUS(Arrays.asList(1,1,1,1,1,1,1)),
    NO_STATUS(Arrays.asList(0,0,0,0,0,0,0));

    private List<Integer> status;

    WorkScheduleEnum(List<Integer> status) {
        this.status = status;
    }

    public List<Integer> getStatus() {
        return status;
    }

    public void setStatus(List<Integer> status) {
        this.status = status;
    }
}
