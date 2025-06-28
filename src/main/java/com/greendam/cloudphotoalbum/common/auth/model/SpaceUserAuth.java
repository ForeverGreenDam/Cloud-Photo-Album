package com.greendam.cloudphotoalbum.common.auth.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 空间用户角色权限模型
 * @author ForeverGreenDam
 */
@Data
public class SpaceUserAuth implements Serializable {

    /**
     * 权限列表
     */
    private List<SpaceUserPermission> permissions;

    /**
     * 角色列表
     */
    private List<SpaceUserRole> roles;

    private static final long serialVersionUID = 1L;
}
