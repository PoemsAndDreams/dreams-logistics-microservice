package com.dreams.logistics.model.dto.user;

import com.dreams.logistics.model.entity.DcUser;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * @description 用户扩展信息
 */
@Data
public class DcUserExt extends DcUser {
    //用户权限
    List<String> permissions = new ArrayList<>();
}
