package com.greendam.cloudphotoalbum.controller.admin;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.greendam.cloudphotoalbum.annotation.AuthCheck;
import com.greendam.cloudphotoalbum.common.BaseResponse;
import com.greendam.cloudphotoalbum.common.DeleteRequest;
import com.greendam.cloudphotoalbum.constant.UserConstant;
import com.greendam.cloudphotoalbum.exception.ErrorCode;
import com.greendam.cloudphotoalbum.exception.ThrowUtils;
import com.greendam.cloudphotoalbum.model.dto.UserAddDTO;
import com.greendam.cloudphotoalbum.model.dto.UserQueryDTO;
import com.greendam.cloudphotoalbum.model.dto.UserUpdateDTO;
import com.greendam.cloudphotoalbum.model.entity.User;
import com.greendam.cloudphotoalbum.model.vo.UserVO;
import com.greendam.cloudphotoalbum.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户控制器(管理员端)
 * @author ForeverGreenDam
 */
@RestController("AdminUserController")
@RequestMapping("/admin/user")
public class UserController {
    @Resource
    private UserService userService;

    /**
     * 添加用户
     * @param userDTO
     * @return 返回用户ID
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddDTO userDTO) {
        final String defaultPassword="123456";
        User user= BeanUtil.copyProperties(userDTO, User.class);
        user.setUserPassword(userService.getEncryptPassword(defaultPassword));
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        boolean save = userService.save(user);
        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR);
        return BaseResponse.success(user.getId());
    }
    /**
     * 更新用户信息
     * @param userDTO 用户更新数据传输对象
     * @return 返回更新后的用户ID
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse updateUser(@RequestBody UserUpdateDTO userDTO) {
        ThrowUtils.throwIf(userDTO == null || userDTO.getId() == null, ErrorCode.PARAMS_ERROR);
        User user = BeanUtil.copyProperties(userDTO, User.class);
        user.setUpdateTime(LocalDateTime.now());
        boolean update = userService.updateById(user);
        ThrowUtils.throwIf(!update, ErrorCode.SYSTEM_ERROR);
        return BaseResponse.success();
    }
    /**
     * 删除用户
     * @param deleteRequest 删除请求对象，包含用户ID
     * @return 返回删除结果
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse deleteUser(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        Long id = deleteRequest.getId();
        boolean remove = userService.removeById(id);
        ThrowUtils.throwIf(!remove, ErrorCode.SYSTEM_ERROR);
        return BaseResponse.success();
    }
    /**
     * 获取用户信息(完整的)
     * @param id 用户ID
     * @return 返回用户信息
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUser(long id) {
     ThrowUtils.throwIf(id==0, ErrorCode.PARAMS_ERROR);
     User user = userService.getById(id);
     ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
     return BaseResponse.success(user);
    }
    /**
     * 按条件分页查询用户信息
     * @param pageRequest 分页请求对象
     * @return 返回分页后的用户信息
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserVoByPage(@RequestBody UserQueryDTO pageRequest) {
        ThrowUtils.throwIf(pageRequest == null, ErrorCode.PARAMS_ERROR);
        long current = pageRequest.getCurrent();
        long pageSize = pageRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, pageSize), userService.getQueryWrapper(pageRequest));
        // 将User对象转换为UserVO对象
        List<UserVO> userVoList=new ArrayList<>();
        userPage.getRecords().forEach(user ->{
            userVoList.add(BeanUtil.copyProperties(user, UserVO.class));
        });
        Page<UserVO> userVoPage = new Page<>(current,pageSize, userPage.getTotal());
        userVoPage.setRecords(userVoList);
        return BaseResponse.success(userVoPage);
    }
}

