//package com.dreams.logistics.config;
//
//
//import com.dreams.logistics.common.BaseResponse;
//import com.dreams.logistics.common.ErrorCode;
//import com.dreams.logistics.common.ResultUtils;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//
///**
// * 全局异常处理器
// */
//@RestControllerAdvice
//@Slf4j
//public class UserGlobalExceptionHandler {
//
//    @ExceptionHandler(AccessDeniedException.class)
//    public BaseResponse<?> accessDeniedExceptionHandler(AccessDeniedException e) {
//        log.error("AccessDeniedException", e);
//        return ResultUtils.error(ErrorCode.NO_AUTH_ERROR, "警告，无权限！");
//    }
//}
