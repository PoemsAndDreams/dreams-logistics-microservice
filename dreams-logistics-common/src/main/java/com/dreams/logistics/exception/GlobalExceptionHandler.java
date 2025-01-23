package com.dreams.logistics.exception;


import com.dreams.logistics.common.BaseResponse;
import com.dreams.logistics.common.ErrorCode;
import com.dreams.logistics.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


    @ExceptionHandler(AuthenticationException.class)
    public BaseResponse<?> authenticationExceptionHandler(AuthenticationException e) {
        log.error("AuthenticationException", e);
        return ResultUtils.error(ErrorCode.NO_AUTH_ERROR, "未登录，请登录！");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public BaseResponse<?> accessDeniedExceptionHandler(AccessDeniedException e) {
        log.error("AccessDeniedException", e);
        return ResultUtils.error(ErrorCode.NO_AUTH_ERROR, "警告，无权限！");
    }

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }
}
