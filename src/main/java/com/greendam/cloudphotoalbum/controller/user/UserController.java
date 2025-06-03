package com.greendam.cloudphotoalbum.controller.user;

import com.greendam.cloudphotoalbum.common.BaseResponse;
import com.greendam.cloudphotoalbum.exception.ErrorCode;
import com.greendam.cloudphotoalbum.exception.ThrowUtils;
import com.greendam.cloudphotoalbum.model.dto.UserRegisterDTO;
import com.greendam.cloudphotoalbum.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 用户控制器
 * @author ForeverGreenDam
 */
@RestController
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
}
