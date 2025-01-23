package com.dreams.logistics.common;

/**
 * 自定义错误码
 */
public enum ErrorCode {

    SUCCESS(0, "ok"),
    PARAMS_ERROR(40000, "请求参数错误"),
    NOT_LOGIN_ERROR(40100, "未登录"),
    NO_AUTH_ERROR(40101, "无权限"),
    NOT_FOUND_ERROR(40400, "请求数据不存在"),
    FORBIDDEN_ERROR(40300, "禁止访问"),
    SYSTEM_ERROR(50000, "系统内部异常"),
    OPERATION_ERROR(50001, "操作失败"),
    CHECK_CODE_ERROR(50002, "验证码错误"),
    SUBMENU_NOT_NULL_ERROR(50003, "子菜单不为null"),
    NOT_ECONOMIC_ZONE_REPEAT(50004, "非经济区的模板重复，只能有一个模板"),
    CARRIAGE_REPEAT(50005, "模板重复，只能有一个模板"),
    ECONOMIC_ZONE_CITY_REPEAT(50006, "经济区互寄关联城市重复"),
    NOT_FOUND(50007, "寄/收地址所属区域暂无计价规则，无法下单"),
    METHOD_CALL_ERROR(50008, "方法调用错误，经济区互寄不通过该方法查询模板");
    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
