package com.greendam.cloudphotoalbum.common.utils;

import com.greendam.cloudphotoalbum.exception.BusinessException;
import com.greendam.cloudphotoalbum.exception.ErrorCode;

/**
 * 异常处理工具类
 * @author ForeverGreenDam
 */
public class ThrowUtils {
    public static void throwIf(boolean condition, RuntimeException runtimeException) {
        if (condition) {
            throw runtimeException;
        }
    }
    public static void throwIf(boolean condition, ErrorCode errorCode) {
        throwIf(condition,new BusinessException(errorCode));
    }
    public static void throwIf(boolean condition,ErrorCode errorCode,String message) {
        throwIf(condition,new BusinessException(errorCode,message));
    }
}
