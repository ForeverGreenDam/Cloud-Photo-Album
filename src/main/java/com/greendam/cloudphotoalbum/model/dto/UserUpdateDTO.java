package com.greendam.cloudphotoalbum.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新数据传输对象
 * @author ForeverGreenDam
 */
@Data
public class UserUpdateDTO implements Serializable {

        /**
         * id
         */
        private Long id;

        /**
         * 用户昵称
         */
        private String userName;

        /**
         * 用户头像
         */
        private String userAvatar;

        /**
         * 简介
         */
        private String userProfile;

        /**
         * 用户角色：user/admin
         */
        private String userRole;

        private static final long serialVersionUID = 1L;

}
