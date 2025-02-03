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

    SIGNATURE_VERIFICATION_FAILED(50009,"验签失败"),
    SERVICE_FAILURE(60000, "处理业务失败"),
    CHECK_CODE_ERROR(60002, "验证码错误"),
    SUBMENU_NOT_NULL_ERROR(60003, "子菜单不为null"),
    NOT_ECONOMIC_ZONE_REPEAT(60004, "非经济区的模板重复，只能有一个模板"),
    CARRIAGE_REPEAT(60005, "模板重复，只能有一个模板"),
    ECONOMIC_ZONE_CITY_REPEAT(60006, "经济区互寄关联城市重复"),
    NOT_FOUND(60007, "寄/收地址所属区域暂无计价规则，无法下单"),
    METHOD_CALL_ERROR(60008, "方法调用错误，经济区互寄不通过该方法查询模板"),

    ORGANIZATION_INFORMATION_FIRST(60009,"请先完善机构信息"),

    JSON_SERIALIZE_ERROR(60010,"序列化json出错！");

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
