package com.dreams.logistics.model.dto.msg;

import cn.hutool.json.JSONUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 基础消息类
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BaseMsg {

    /**
     * 扩展信息
     */
    private String info;

    /**
     * 创建时间
     */
    private Long created;

    public String toJson() {
        return JSONUtil.toJsonStr(this);
    }

}
