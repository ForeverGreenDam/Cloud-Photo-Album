package com.greendam.cloudphotoalbum.aop;

import com.greendam.cloudphotoalbum.annotation.AuthCheck;
import com.greendam.cloudphotoalbum.exception.BusinessException;
import com.greendam.cloudphotoalbum.exception.ErrorCode;
import com.greendam.cloudphotoalbum.exception.ThrowUtils;
import com.greendam.cloudphotoalbum.model.enums.UserRoleEnum;
import com.greendam.cloudphotoalbum.model.vo.UserLoginVO;
import com.greendam.cloudphotoalbum.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 权限检查切面
 * @author ForeverGreenDam
 */
@Aspect
@Component
@Slf4j
public class AuthCheckAspect {
    @Resource
    private UserService userService;
    /**
     * 切点：匹配所有使用了 @AuthCheck 注解的方法
     */
    @Pointcut("@annotation(com.greendam.cloudphotoalbum.annotation.AuthCheck)")
    public void authCheck() {}

    /**
     * 在方法执行前进行权限检查
     * @param joinPoint 连接点，包含方法签名等信息
     */
    @Before("authCheck()")
    public void before(JoinPoint joinPoint) {
        MethodSignature signature =(MethodSignature) joinPoint.getSignature();
        AuthCheck annotation = signature.getMethod().getAnnotation(AuthCheck.class);
        //包装成枚举类型，方便日后比较
        UserRoleEnum mustRole=UserRoleEnum.getEnumByValue( annotation.mustRole());
        //获取当前用户角色
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        //getUser已经做了对未登录用户的判断，这里的user一定是已登录的用户
        UserLoginVO user= userService.getUser(request);
        UserRoleEnum userRole = UserRoleEnum.getEnumByValue(user.getUserRole());
        //校验权限
        //如果当前用户角色为空，则抛出未登录
        ThrowUtils.throwIf(userRole==null, ErrorCode.NOT_LOGIN_ERROR);
        //如果当前用户角色不符合要求，则抛出权限不足
        //不符合要求的情况有三种：user访问admin或vip接口，vip访问admin接口，admin访问vip接口
        if((UserRoleEnum.USER.equals(userRole)&&UserRoleEnum.ADMIN.equals(mustRole))
                || (UserRoleEnum.USER.equals(userRole)&&UserRoleEnum.VIP.equals(mustRole))
                || (UserRoleEnum.VIP.equals(userRole)&&UserRoleEnum.ADMIN.equals(mustRole))
                || (UserRoleEnum.ADMIN.equals(userRole)&&UserRoleEnum.VIP.equals(mustRole))) {
            log.info("用户角色不符合要求，当前角色：{}，必须角色：{}", userRole, mustRole);
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR);
        }
        //如果当前用户角色符合要求，则放行
    }


}
