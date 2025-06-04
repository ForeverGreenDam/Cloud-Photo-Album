package com.greendam.cloudphotoalbum.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * @author ForeverGreenDam
 */
@Getter
public enum UserRoleEnum {
    /**
     * 用户角色枚举
     */
    USER("用户", "user"),
    ADMIN("管理员", "admin"),
    VIP("VIP用户", "vip");


    private final String text;
    private final String value;

    UserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }
    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static UserRoleEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (UserRoleEnum anEnum : UserRoleEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
