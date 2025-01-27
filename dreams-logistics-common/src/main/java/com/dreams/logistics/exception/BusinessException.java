package com.dreams.logistics.exception;


import com.dreams.logistics.common.ErrorCode;
import com.dreams.logistics.enums.BaseEnum;
import com.dreams.logistics.enums.TradingEnum;

/**
 * 自定义异常类
 */
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(BaseEnum errorCode) {
        super(errorCode.getValue());
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    public BusinessException(String msg, Integer code) {
        super(msg);
        this.code = code;
    }

    public BusinessException(TradingEnum tradingEnum, Exception e) {
        super(tradingEnum.getValue(), e);
        this.code = tradingEnum.getCode();
    }

    public int getCode() {
        return code;
    }
}
