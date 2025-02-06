package com.dreams.logistics.service;

import com.dreams.logistics.entity.line.DispatchConfiguration;

/**
 * 调度配置相关业务
 */
public interface DispatchConfigurationService {
    /**
     * 查询调度配置
     *
     * @return 调度配置
     */
    DispatchConfiguration findConfiguration();

    /**
     * 保存调度配置
     * @param dto 调度配置
     */
    void saveConfiguration(DispatchConfiguration dto);
}
