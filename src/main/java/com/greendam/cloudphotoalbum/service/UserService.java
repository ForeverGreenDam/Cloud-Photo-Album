package com.greendam.cloudphotoalbum.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.greendam.cloudphotoalbum.model.dto.UserLoginDTO;
import com.greendam.cloudphotoalbum.model.dto.UserQueryDTO;
import com.greendam.cloudphotoalbum.model.dto.UserRegisterDTO;
import com.greendam.cloudphotoalbum.model.entity.User;
import com.greendam.cloudphotoalbum.model.vo.UserLoginVO;

import javax.servlet.http.HttpServletRequest;

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

    /**
     * 用户登录
     *
     * @param userLoginDTO 用户登录数据传输对象
     * @param request
     * @return 返回用户登录信息
     */
    UserLoginVO login(UserLoginDTO userLoginDTO, HttpServletRequest request);

    /**
     * 获取当前登录用户信息
     *
     * @param request HttpServletRequest对象
     * @return 返回用户登录信息
     */
    UserLoginVO getUser(HttpServletRequest request);

    /**
     * 用户登出
     *
     * @param request HttpServletRequest对象
     */
    void logout(HttpServletRequest request);

    /**
     * 设置分页查询用户条件
     *
     * @param userQueryRequest 用户查询数据传输对象
     * @return 返回分页查询结果
     */
    QueryWrapper<User> getQueryWrapper(UserQueryDTO userQueryRequest);

    /**
     * 获取加密后的密码
     *
     * @param defaultPassword 默认密码
     * @return 返回加密后的密码
     */
    String getEncryptPassword(String defaultPassword);
}
