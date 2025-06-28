package com.greendam.cloudphotoalbum.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.greendam.cloudphotoalbum.model.dto.SpaceUserAddDTO;
import com.greendam.cloudphotoalbum.model.dto.SpaceUserQueryDTO;
import com.greendam.cloudphotoalbum.model.entity.SpaceUser;
import com.greendam.cloudphotoalbum.model.vo.SpaceUserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author ForeverGreenDam
* @description 针对表【space_user(空间用户关联)】的数据库操作Service
* @createDate 2025-06-28 16:49:52
*/
public interface SpaceUserService extends IService<SpaceUser> {
    /**
     * 新增团队成员
     * @param spaceUserAddRequest 团队成员添加请求
     * @return 新增的团队成员ID
     */
    long addSpaceUser(SpaceUserAddDTO spaceUserAddRequest);

    /**
     * 校验团队成员信息
     * @param spaceUser 团队成员信息
     * @param add 是否为新增操作
     */
    void validSpaceUser(SpaceUser spaceUser, boolean add);
    /**
     * 获取团队成员查询条件
     * @param spaceUserQueryRequest 团队成员查询请求
     * @return 查询条件包装器
     */
    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryDTO spaceUserQueryRequest);

    /**
     * 查询团队成员列表
     * @param spaceUser 团队成员信息
     * @param request HTTP请求对象
     * @return 团队成员视图对象
     */
    SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request);

    /**
     * 查询团队成员列表（不分页，因为数量不多）
     * @param spaceUserList 团队成员列表
     * @return 团队成员视图对象列表
     */
    List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList);
}
