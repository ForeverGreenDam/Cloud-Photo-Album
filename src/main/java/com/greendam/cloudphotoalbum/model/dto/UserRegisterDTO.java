package com.greendam.cloudphotoalbum.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册数据传输对象
 * @author ForeverGreenDam
 */
@Data
public class UserRegisterDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 用户账号
     */
    private String userAccount;
    /**
     * 用户密码
     */
    private String userPassword;
    /**
     * 确认密码
     */
    private String checkPassword;
}
