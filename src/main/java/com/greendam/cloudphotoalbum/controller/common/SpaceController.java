package com.greendam.cloudphotoalbum.controller.common;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.greendam.cloudphotoalbum.annotation.AuthCheck;
import com.greendam.cloudphotoalbum.common.BaseResponse;
import com.greendam.cloudphotoalbum.common.DeleteRequest;
import com.greendam.cloudphotoalbum.constant.UserConstant;
import com.greendam.cloudphotoalbum.model.dto.*;
import com.greendam.cloudphotoalbum.model.entity.Space;
import com.greendam.cloudphotoalbum.model.enums.SpaceLevelEnum;
import com.greendam.cloudphotoalbum.model.vo.SpaceVO;
import com.greendam.cloudphotoalbum.service.SpaceService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
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
     * @return 空间视图
     */
    @PostMapping("/add")
    @AuthCheck
    public BaseResponse<Long> addSpace(@RequestBody SpaceAddDTO spaceAddDTO) {

        return null;
    }
    /**
     * 删除空间
     * @param deleteRequest 删除请求对象，包含要删除的空间ID
     * @return 删除是否成功
     */
    @PostMapping("/delete")
    @AuthCheck
    public BaseResponse<Boolean> deleteSpace(@RequestBody DeleteRequest deleteRequest){

        return null;
    }

    /**
     * 编辑空间
     * @return 编辑是否成功
     */
    @PostMapping("/edit")
    @AuthCheck
    public BaseResponse<Boolean> edit(@RequestBody SpaceEditDTO spaceEditDTO) {

        return null;
    }
    /**
     * 获取空间详情（管理员）
     * @param spaceId 空间ID
     * @return 空间视图对象
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Space> getSpace(@RequestParam Long spaceId) {

        return  null;
    }
    /**
     * 获取空间视图对象（用户）
     * @param spaceId 空间ID
     * @return 空间视图对象
     */
    @GetMapping("/get/vo")
    @AuthCheck
    public BaseResponse<SpaceVO> getSpaceVO(@RequestParam Long spaceId) {
        return  null;
    }
    /**
     * 获取空间列表（管理员）
     * @param spaceQueryDTO  空间查询数据传输对象，包含分页信息和查询条件
     * @return 空间视图列表
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Space>> getSpacePage(@RequestBody SpaceQueryDTO spaceQueryDTO) {
        return null;
    }

    /**
     * 获取空间列表视图对象（用户）
     * @param spaceQueryDTO 空间查询数据传输对象，包含分页信息和查询条件
     * @return 空间视图对象的分页列表
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<SpaceVO>> getSpacePageVO(@RequestBody SpaceQueryDTO spaceQueryDTO) {
        return null;
    }

    /**
     * 更新空间信息
     * @param spaceUpdateDTO 空间更新数据传输对象，包含空间ID和更新信息
     * @return 更新是否成功
     */
    @PostMapping("/update")
    @AuthCheck
    public BaseResponse<Boolean> update(@RequestBody SpaceUpdateDTO spaceUpdateDTO) {
        return null;
    }
}


