package com.dreams.logistics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dreams.logistics.entity.Msg;
import org.apache.ibatis.annotations.Mapper;

/**
 * 失败消息记录mapper
 */
@Mapper
public interface MsgMapper extends BaseMapper<Msg> {
}
