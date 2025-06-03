package com.greendam.cloudphotoalbum.service;

import com.greendam.cloudphotoalbum.common.BaseResponse;
import com.greendam.cloudphotoalbum.model.dto.UserRegisterDTO;
import com.greendam.cloudphotoalbum.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author ForeverGreenDam
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-06-03 16:59:18
*/
public interface UserService extends IService<User> {
    /**
     * 用户注册
     * @param userRegisterDTO 用户注册数据传输对象
     * @return 返回用户ID
     */
    Long register(UserRegisterDTO userRegisterDTO);
}
