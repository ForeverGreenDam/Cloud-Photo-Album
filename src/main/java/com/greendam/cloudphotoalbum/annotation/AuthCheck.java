package com.greendam.cloudphotoalbum.annotation;

import com.greendam.cloudphotoalbum.constant.UserConstant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限检查注解
 * @author ForeverGreenDam
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthCheck {

    String mustRole() default UserConstant.DEFAULT_ROLE;
}
