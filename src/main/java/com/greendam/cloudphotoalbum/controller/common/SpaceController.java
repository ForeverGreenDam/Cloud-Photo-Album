package com.greendam.cloudphotoalbum.controller.common;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.greendam.cloudphotoalbum.annotation.AuthCheck;
import com.greendam.cloudphotoalbum.common.BaseResponse;
import com.greendam.cloudphotoalbum.common.DeleteRequest;
import com.greendam.cloudphotoalbum.constant.UserConstant;
import com.greendam.cloudphotoalbum.exception.ErrorCode;
import com.greendam.cloudphotoalbum.common.utils.ThrowUtils;
import com.greendam.cloudphotoalbum.model.dto.*;
import com.greendam.cloudphotoalbum.model.entity.Space;
import com.greendam.cloudphotoalbum.model.enums.SpaceLevelEnum;
import com.greendam.cloudphotoalbum.model.vo.SpaceVO;
import com.greendam.cloudphotoalbum.model.vo.UserLoginVO;
import com.greendam.cloudphotoalbum.model.vo.UserVO;
import com.greendam.cloudphotoalbum.service.SpaceService;
import com.greendam.cloudphotoalbum.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 图片控制器类，用于处理与图片相关的请求
 * @author ForeverGreenDam
 */
@RestController
@RequestMapping("/space")
public class SpaceController {
    @Resource
    private SpaceService spaceService;
    @Resource
    private UserService userService;

    @GetMapping("/list/level")
    public BaseResponse<List<SpaceLevel>> listSpaceLevel() {
        // 获取所有枚举
        List<SpaceLevel> spaceLevelList = Arrays.stream(SpaceLevelEnum.values())
                .map(spaceLevelEnum -> new SpaceLevel(
                        spaceLevelEnum.getValue(),
                        spaceLevelEnum.getText(),
                        spaceLevelEnum.getMaxCount(),
                        spaceLevelEnum.getMaxSize()))
                .collect(Collectors.toList());
        return BaseResponse.success(spaceLevelList);
    }
    /**
     * 新增空间
     * @param spaceAddDTO 空间添加数据传输对象，包含空间名称、描述等信息
     * @return 空间ID
     */
    @PostMapping("/add")
    @AuthCheck
    public BaseResponse<Long> addSpace(@RequestBody SpaceAddDTO spaceAddDTO, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceAddDTO==null, ErrorCode.PARAMS_ERROR);
        UserLoginVO loginUser = userService.getUser(request);
       Long spaceId=spaceService.addSpace(spaceAddDTO,loginUser);
        return BaseResponse.success(spaceId);
    }
    /**
     * 删除空间
     * @param deleteRequest 删除请求对象，包含要删除的空间ID
     * @return 删除是否成功
     */
    @PostMapping("/delete")
    @AuthCheck
    public BaseResponse<Boolean> deleteSpace(@RequestBody DeleteRequest deleteRequest,HttpServletRequest request){
        ThrowUtils.throwIf(deleteRequest==null, ErrorCode.PARAMS_ERROR);
        UserLoginVO user = userService.getUser(request);
        spaceService.deleteSpace(deleteRequest,user);
        return BaseResponse.success(true);
    }

    /**
     * 编辑空间
     * @return 空间ID
     */
    @PostMapping("/edit")
    @AuthCheck
    public BaseResponse<Long> edit(@RequestBody SpaceEditDTO spaceEditDTO,HttpServletRequest request) {
        ThrowUtils.throwIf(spaceEditDTO==null, ErrorCode.PARAMS_ERROR);
        UserLoginVO user = userService.getUser(request);
        spaceService.edit(spaceEditDTO,user);
        return BaseResponse.success(spaceEditDTO.getId());
    }
    /**
     * 获取空间详情（管理员）
     * @param spaceId 空间ID
     * @return 空间对象
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Space> getSpace(@RequestParam Long spaceId) {
        ThrowUtils.throwIf(spaceId == null, ErrorCode.PARAMS_ERROR, "空间ID不能为空");
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        return  BaseResponse.success(space);
    }
    /**
     * 获取空间视图对象（用户）
     * @param id 空间ID
     * @return 空间视图对象
     */
    @GetMapping("/get/vo")
    @AuthCheck
    public BaseResponse<SpaceVO> getSpaceVO(@RequestParam Long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "空间ID不能为空");
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        //鉴权
        UserLoginVO loginUser = userService.getUser(request);
        ThrowUtils.throwIf(!space.getUserId().equals(loginUser.getId())&& !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole()),
                ErrorCode.NOT_FOUND_ERROR, "没有权限查看该空间");
        SpaceVO spaceVO = SpaceVO.objToVo(space);
        //填充userVO数据
        UserVO userVO = BeanUtil.copyProperties(loginUser, UserVO.class);
        spaceVO.setUser(userVO);
        return  BaseResponse.success(spaceVO);
    }
    /**
     * 获取空间列表（管理员）
     * @param spaceQueryDTO  空间查询数据传输对象，包含分页信息和查询条件
     * @return 空间视图列表
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Space>> getSpacePage(@RequestBody SpaceQueryDTO spaceQueryDTO) {
        ThrowUtils.throwIf(spaceQueryDTO == null, ErrorCode.PARAMS_ERROR);
        Page<Space> page=spaceService.getSpacePage(spaceQueryDTO);
        return BaseResponse.success(page);
    }

    /**
     * 获取空间列表视图对象（用户）
     * @param spaceQueryDTO 空间查询数据传输对象，包含分页信息和查询条件
     * @return 空间视图对象的分页列表(仅限自己创建的)
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<SpaceVO>> getSpacePageVO(@RequestBody SpaceQueryDTO spaceQueryDTO,HttpServletRequest request) {
        ThrowUtils.throwIf(spaceQueryDTO == null, ErrorCode.PARAMS_ERROR);
        UserLoginVO user = userService.getUser(request);
        Page<SpaceVO> page=spaceService.getSpacePageVO(spaceQueryDTO,user);
        return BaseResponse.success(page);
    }

    /**
     * 更新空间信息(管理员)
     * @param spaceUpdateDTO 空间更新数据传输对象，包含空间ID和更新信息
     * @return 空间ID
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> update(@RequestBody SpaceUpdateDTO spaceUpdateDTO) {
        ThrowUtils.throwIf(spaceUpdateDTO==null, ErrorCode.PARAMS_ERROR);
        return BaseResponse.success(spaceService.updateSpace(spaceUpdateDTO));
    }
}


