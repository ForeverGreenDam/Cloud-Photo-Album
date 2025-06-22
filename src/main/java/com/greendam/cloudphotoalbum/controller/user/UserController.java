package com.greendam.cloudphotoalbum.controller.user;

import cn.hutool.core.bean.BeanUtil;
import com.greendam.cloudphotoalbum.annotation.AuthCheck;
import com.greendam.cloudphotoalbum.common.BaseResponse;
import com.greendam.cloudphotoalbum.exception.ErrorCode;
import com.greendam.cloudphotoalbum.exception.ThrowUtils;
import com.greendam.cloudphotoalbum.model.dto.UserLoginDTO;
import com.greendam.cloudphotoalbum.model.dto.UserRegisterDTO;
import com.greendam.cloudphotoalbum.model.entity.User;
import com.greendam.cloudphotoalbum.model.vo.UserLoginVO;
import com.greendam.cloudphotoalbum.model.vo.UserVO;
import com.greendam.cloudphotoalbum.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 用户控制器
 * @author ForeverGreenDam
 */
@RestController("userUserController")
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;
    /**
     * 用户注册
     * @param userRegisterDTO 用户注册数据传输对象
     * @return 返回用户ID
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterDTO userRegisterDTO) {
        ThrowUtils.throwIf(userRegisterDTO==null, ErrorCode.PARAMS_ERROR);
        Long userId = userService.register(userRegisterDTO);
        return BaseResponse.success(userId);
    }
    /**
     * 用户登录
     * @param userLoginDTO 用户登录数据传输对象
     * @param request HttpServletRequest对象
     * @return 返回用户登录信息
     */
    @PostMapping("/login")
    public BaseResponse<UserLoginVO> userLogin(@RequestBody UserLoginDTO userLoginDTO, HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginDTO==null, ErrorCode.PARAMS_ERROR);
        UserLoginVO userLoginVO = userService.login(userLoginDTO,request);
        return BaseResponse.success(userLoginVO);
    }
    /**
     * 获取当前登录用户信息
     * @param request HttpServletRequest对象
     * @return 返回用户登录信息
     */
    @GetMapping("/get/login")
    @AuthCheck
    public BaseResponse<UserLoginVO> getUser(HttpServletRequest request) {
        UserLoginVO userLoginVO = userService.getUser(request);
        ThrowUtils.throwIf(userLoginVO == null, ErrorCode.NOT_LOGIN_ERROR);
        return BaseResponse.success(userLoginVO);
    }
    /**
     * 用户登出
     * @param request HttpServletRequest对象
     * @return 返回成功消息
     */
    @PostMapping("/logout")
    @AuthCheck
    public BaseResponse<String> userLogout(HttpServletRequest request) {
        userService.logout(request);
        return BaseResponse.success("登出成功");
    }
    /**
     * 获取用户信息
     * @param id 用户ID
     * @return 返回用户信息
     */
    @GetMapping("/get")
    @AuthCheck
    public BaseResponse<UserVO> getUser(Long id) {
        ThrowUtils.throwIf(id==0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        UserVO userVO= BeanUtil.copyProperties(user, UserVO.class);
        return BaseResponse.success(userVO);
    }
}
