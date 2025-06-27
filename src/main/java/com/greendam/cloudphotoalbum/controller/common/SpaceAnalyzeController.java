package com.greendam.cloudphotoalbum.controller.common;

import com.greendam.cloudphotoalbum.annotation.AuthCheck;
import com.greendam.cloudphotoalbum.common.BaseResponse;
import com.greendam.cloudphotoalbum.common.utils.ThrowUtils;
import com.greendam.cloudphotoalbum.constant.UserConstant;
import com.greendam.cloudphotoalbum.exception.ErrorCode;
import com.greendam.cloudphotoalbum.model.dto.*;
import com.greendam.cloudphotoalbum.model.entity.Space;
import com.greendam.cloudphotoalbum.model.vo.*;
import com.greendam.cloudphotoalbum.service.SpaceAnalyzeService;
import com.greendam.cloudphotoalbum.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 图片控制器类，用于处理与图片相关的请求
 * @author ForeverGreenDam
 */
@RestController
@RequestMapping("/space/analyze")
public class SpaceAnalyzeController {
    @Resource
    private SpaceAnalyzeService spaceAnalyzeService;
    @Resource
    private UserService userService;

    /**
     * 分析空间使用情况
     * @param spaceUsageAnalyzeDTO 空间使用情况分析数据传输对象，包含空间ID、是否查询公共图库等信息
     * @param request HTTP请求对象，用于获取当前登录用户信息
     * @return 空间使用情况分析视图对象
     */
    @PostMapping("/usage")
    public BaseResponse<SpaceUsageAnalyzeVO> analyzeSpaceUsage(@RequestBody SpaceUsageAnalyzeDTO spaceUsageAnalyzeDTO, HttpServletRequest request) {
        //参数校验
        ThrowUtils.throwIf(spaceUsageAnalyzeDTO==null, ErrorCode.PARAMS_ERROR);
        // 调用服务层方法进行空间使用情况分析
        UserLoginVO user = userService.getUser(request);
        SpaceUsageAnalyzeVO spaceUsageAnalyzeVO = spaceAnalyzeService.analyzeSpaceUsage(spaceUsageAnalyzeDTO,user);
        // 返回分析结果
        return BaseResponse.success(spaceUsageAnalyzeVO);
    }

    /**
     * 获取空间分类分析结果
     * @param spaceCategoryAnalyzeRequest 空间分类分析请求数据传输对象，包含空间ID、是否查询公共图库等信息
     * @param request HTTP请求对象，用于获取当前登录用户信息
     * @return 空间分类分析视图对象列表
     */
    @PostMapping("/category")
    public BaseResponse<List<SpaceCategoryAnalyzeVO>> getSpaceCategoryAnalyze(@RequestBody SpaceCategoryAnalyzeDTO spaceCategoryAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceCategoryAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        UserLoginVO loginUser = userService.getUser(request);
        List<SpaceCategoryAnalyzeVO> resultList = spaceAnalyzeService.getSpaceCategoryAnalyze(spaceCategoryAnalyzeRequest, loginUser);
        return BaseResponse.success(resultList);
    }

    /**
     * 获取空间标签分析结果
     * @param spaceTagAnalyzeRequest 空间标签分析请求数据传输对象，包含空间ID、是否查询公共图库等信息
     * @param request HTTP请求对象，用于获取当前登录用户信息
     * @return 空间标签分析视图对象列表
     */
    @PostMapping("/tag")
    public BaseResponse<List<SpaceTagAnalyzeVO>> getSpaceTagAnalyze(@RequestBody SpaceTagAnalyzeDTO spaceTagAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceTagAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        UserLoginVO loginUser = userService.getUser(request);
        List<SpaceTagAnalyzeVO> resultList = spaceAnalyzeService.getSpaceTagAnalyze(spaceTagAnalyzeRequest, loginUser);
        return BaseResponse.success(resultList);
    }

    /**
     * 获取空间图片大小分析结果
     * @param spaceSizeAnalyzeRequest 空间图片大小分析请求数据传输对象，包含空间ID、是否查询公共图库等信息
     * @param request  HTTP请求对象，用于获取当前登录用户信息
     * @return 空间图片大小分析视图对象列表
     */
    @PostMapping("/size")
    public BaseResponse<List<SpaceSizeAnalyzeVO>> getSpaceSizeAnalyze(@RequestBody SpaceSizeAnalyzeDTO spaceSizeAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceSizeAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        UserLoginVO loginUser = userService.getUser(request);
        List<SpaceSizeAnalyzeVO> resultList = spaceAnalyzeService.getSpaceSizeAnalyze(spaceSizeAnalyzeRequest, loginUser);
        return BaseResponse.success(resultList);
    }

    /**
     * 获取空间用户上传行为分析结果
     * @param spaceUserAnalyzeRequest 用户上传行为分析请求数据传输对象，包含用户ID、时间维度等信息
     * @param request HTTP请求对象，用于获取当前登录用户信息
     * @return 用户上传行为分析视图对象列表
     */
    @PostMapping("/user")
    public BaseResponse<List<SpaceUserAnalyzeVO>> getSpaceUserAnalyze(@RequestBody SpaceUserAnalyzeDTO spaceUserAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUserAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        UserLoginVO loginUser = userService.getUser(request);
        List<SpaceUserAnalyzeVO> resultList = spaceAnalyzeService.getSpaceUserAnalyze(spaceUserAnalyzeRequest, loginUser);
        return BaseResponse.success(resultList);
    }

    /**
     * 获取空间存储量排名分析结果
     * @param spaceRankAnalyzeRequest 空间排名分析请求数据传输对象，包含空间ID、时间维度等信息
     * @param request HTTP请求对象，用于获取当前登录用户信息
     * @return 空间列表
     */
    @PostMapping("/rank")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<List<Space>> getSpaceRankAnalyze(@RequestBody SpaceRankAnalyzeDTO spaceRankAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceRankAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        UserLoginVO loginUser = userService.getUser(request);
        List<Space> resultList = spaceAnalyzeService.getSpaceRankAnalyze(spaceRankAnalyzeRequest, loginUser);
        return BaseResponse.success(resultList);
    }
}


