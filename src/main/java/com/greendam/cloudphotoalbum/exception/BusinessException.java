package com.greendam.cloudphotoalbum.exception;

import lombok.Getter;

/**
 * 自定义业务异常类
 * @author ForeverGreenDam
 */
@Getter
public class BusinessException extends RuntimeException {
    /**
     * 错误码
     */
    private final int code;
    public BusinessException(int code,String message) {
        super(message);
        this.code = code;
    }
    public BusinessException(ErrorCode errorCode) {
        this(errorCode.getCode(),errorCode.getMessage());
    }
    public BusinessException(ErrorCode errorCode,String message) {
        this(errorCode.getCode(),message);
    }
}
