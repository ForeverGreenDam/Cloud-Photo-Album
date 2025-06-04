package com.greendam.cloudphotoalbum.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.greendam.cloudphotoalbum.constant.UserConstant;
import com.greendam.cloudphotoalbum.exception.BusinessException;
import com.greendam.cloudphotoalbum.exception.ErrorCode;
import com.greendam.cloudphotoalbum.exception.ThrowUtils;
import com.greendam.cloudphotoalbum.model.dto.UserLoginDTO;
import com.greendam.cloudphotoalbum.model.dto.UserQueryDTO;
import com.greendam.cloudphotoalbum.model.dto.UserRegisterDTO;
import com.greendam.cloudphotoalbum.model.entity.User;
import com.greendam.cloudphotoalbum.model.enums.UserRoleEnum;
import com.greendam.cloudphotoalbum.model.vo.UserLoginVO;
import com.greendam.cloudphotoalbum.service.UserService;
import com.greendam.cloudphotoalbum.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
* @author ForeverGreenDam
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-06-03 16:59:18
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{
        @Resource
        UserMapper userMapper;

    @Override
    public Long register(UserRegisterDTO userRegisterDTO) {
        String userAccount = userRegisterDTO.getUserAccount();
        String userPassword = userRegisterDTO.getUserPassword();
        String checkPassword = userRegisterDTO.getCheckPassword();
        ThrowUtils.throwIf(userAccount.isEmpty() || userPassword.isEmpty() || checkPassword.isEmpty(), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(!userPassword.equals(checkPassword), ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        // 检查用户账号是否已存在
        Long count = userMapper.selectCount(new QueryWrapper<User>().eq("userAccount", userAccount));
        ThrowUtils.throwIf(count > 0, ErrorCode.OPERATION_ERROR,"用户已存在");
        //密码加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 创建用户对象
        User user = User.builder()
                .userAccount(userAccount)
                .userPassword(encryptPassword)
                .userRole(UserRoleEnum.USER.getValue())
                .userName("新用户")
                .createTime(LocalDateTime.now())
                .editTime(LocalDateTime.now())
                .build();
        // 插入用户数据
        boolean saveResult = this.save(user);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR);
        return  user.getId();
    }

    @Override
    public UserLoginVO login(UserLoginDTO userLoginDTO, HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginDTO.getUserAccount().isEmpty()||userLoginDTO.getUserPassword().isEmpty(), ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginDTO.getUserAccount();
        String userPassword =getEncryptPassword(userLoginDTO.getUserPassword());
        // 查询用户
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("userAccount", userAccount).eq("userPassword", userPassword));
        ThrowUtils.throwIf(user == null, ErrorCode.OPERATION_ERROR, "用户不存在或密码错误");
        //在session中存储用户信息
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        return BeanUtil.copyProperties(user, UserLoginVO.class);
    }
    @Override
    public UserLoginVO getUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        ThrowUtils.throwIf(userObj == null, ErrorCode.NOT_LOGIN_ERROR);
        User user = (User) userObj;
        return BeanUtil.copyProperties(user, UserLoginVO.class);
    }

    @Override
    public void logout(HttpServletRequest request) {
        Object user = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_LOGIN_ERROR);
        // 清除session中的用户信息
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
    }
    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryDTO userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);
        queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
        queryWrapper.like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);
        // 设置排序条件  参数一：是否需要排序，参数二：升序还是降序，参数三：排序字段
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), "ascend".equals(sortOrder), sortField);
        return queryWrapper;
    }

    /**
     * 获取加密后的密码
     * @param password 用户密码
     * @return 返回加密后的密码
     */
    @Override
    public String getEncryptPassword(String password) {
        // 加盐
        final String salt = "ForeverGreenDam";
        String result = DigestUtils.md5DigestAsHex((salt + password).getBytes());
        return result;
    }
}




