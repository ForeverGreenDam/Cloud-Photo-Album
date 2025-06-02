package com.greendam.cloudphotoalbum.common;

import com.greendam.cloudphotoalbum.exception.ErrorCode;
import lombok.Getter;

import java.io.Serializable;

/**
 * 通用响应基类
 * @author ForeverGreenDam
 */
@Getter
public class BaseResponse<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 响应状态码
     */
    private int code;
    /**
     * 响应数据
     */
    private T data;
    /**
     * 提示信息
     */
    private String message;

    private BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public static<E> BaseResponse<E> success() {
        return new BaseResponse<E>(ErrorCode.SUCCESS.getCode(), null, ErrorCode.SUCCESS.getMessage());
    }
    public static<E> BaseResponse<E> success(E data) {
        return new BaseResponse<>(ErrorCode.SUCCESS.getCode(), data, ErrorCode.SUCCESS.getMessage());
    }
    public static <E>BaseResponse<E> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode.getCode(), null, errorCode.getMessage());
    }
    public static<E> BaseResponse<E> error(ErrorCode errorCode,String message) {
        return new BaseResponse<>(errorCode.getCode(), null, message);
    }
    public static<E> BaseResponse<E> error(int code,String message) {
        return new BaseResponse<>(code, null, message);
    }
}
