package com.greendam.cloudphotoalbum.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.greendam.cloudphotoalbum.common.DeleteRequest;
import com.greendam.cloudphotoalbum.model.dto.SpaceAddDTO;
import com.greendam.cloudphotoalbum.model.dto.SpaceEditDTO;
import com.greendam.cloudphotoalbum.model.dto.SpaceQueryDTO;
import com.greendam.cloudphotoalbum.model.dto.SpaceUpdateDTO;
import com.greendam.cloudphotoalbum.model.entity.Space;
import com.greendam.cloudphotoalbum.model.vo.SpaceVO;
import com.greendam.cloudphotoalbum.model.vo.UserLoginVO;

/**
* @author ForeverGreenDam
* @description 针对表【space(空间)】的数据库操作Service
* @createDate 2025-06-20 22:29:02
*/
public interface SpaceService extends IService<Space> {
    /**
     * 填充空间数据
     * @param space
     */
    void fillSpaceBySpaceLevel(Space space);
    /**
     * 新增空间
     *
     * @param spaceAddDTO
     * @param loginUser
     * @return
     */
    Long addSpace(SpaceAddDTO spaceAddDTO, UserLoginVO loginUser);

    /**
     * 删除空间
     * @param deleteRequest 删除请求，包含空间ID
     * @param  user 当前登录用户信息
     */
    void deleteSpace(DeleteRequest deleteRequest, UserLoginVO user);

    /**
     * 编辑空间信息
     *
     * @param spaceEditDTO 空间编辑数据传输对象，包含空间ID和新的空间名称等信息
     * @param user
     */
    void edit(SpaceEditDTO spaceEditDTO, UserLoginVO user);

    /**
     * 更新空间信息（管理员）
     * @param spaceUpdateDTO 空间更新数据传输对象，包含空间ID和新的空间名称等信息
     */
    Long updateSpace(SpaceUpdateDTO spaceUpdateDTO);
    /**
     * 获取空间分页列表(管理员)
     *
     * @param spaceQueryDTO 空间查询数据传输对象，包含查询条件和分页信息
     * @return 分页结果
     */
    Page<Space> getSpacePage(SpaceQueryDTO spaceQueryDTO);

    /**
     * 获取空间分页列表视图对象(用户)
     *
     * @param spaceQueryDTO 空间查询数据传输对象，包含查询条件和分页信息
     * @param user
     * @return 分页结果，包含空间视图对象
     */
    Page<SpaceVO> getSpacePageVO(SpaceQueryDTO spaceQueryDTO, UserLoginVO user);
}
