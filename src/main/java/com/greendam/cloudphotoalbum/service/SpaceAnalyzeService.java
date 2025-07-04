package com.greendam.cloudphotoalbum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.greendam.cloudphotoalbum.model.dto.*;
import com.greendam.cloudphotoalbum.model.entity.Space;
import com.greendam.cloudphotoalbum.model.vo.*;

import java.util.List;

/**
* @author ForeverGreenDam
* @description 针对表【space(空间)】的数据库操作Service
* @createDate 2025-06-20 22:29:02
*/
public interface SpaceAnalyzeService extends IService<Space> {

    /**
     * 分析空间使用情况
     * @param spaceUsageAnalyzeDTO 空间使用情况分析数据传输对象，包含空间ID、是否查询公共图库等信息
     * @param user 当前登录用户信息
     * @return 空间使用情况分析视图对象
     */
    SpaceUsageAnalyzeVO analyzeSpaceUsage(SpaceUsageAnalyzeDTO spaceUsageAnalyzeDTO, UserLoginVO user);

    /**
     *  分析空间分类情况
     * @param spaceCategoryAnalyzeRequest 空间分类分析请求数据传输对象，包含空间ID、是否查询公共图库等信息
     * @param loginUser 当前登录用户信息
     * @return 空间分类分析视图对象列表
     */
    List<SpaceCategoryAnalyzeVO> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeDTO spaceCategoryAnalyzeRequest, UserLoginVO loginUser);

    /**
     * 分析空间标签情况
     * @param spaceTagAnalyzeRequest 空间标签分析请求数据传输对象，包含空间ID、是否查询公共图库等信息
     * @param loginUser 当前登录用户信息
     * @return 空间标签分析视图对象列表
     */
    List<SpaceTagAnalyzeVO> getSpaceTagAnalyze(SpaceTagAnalyzeDTO spaceTagAnalyzeRequest, UserLoginVO loginUser);

    /**
     * 分析空间图片大小情况
     * @param spaceSizeAnalyzeRequest 空间图片大小分析请求数据传输对象，包含空间ID、是否查询公共图库等信息
     * @param loginUser 当前登录用户信息
     * @return 空间图片大小分析视图对象列表
     */
    List<SpaceSizeAnalyzeVO> getSpaceSizeAnalyze(SpaceSizeAnalyzeDTO spaceSizeAnalyzeRequest, UserLoginVO loginUser);

    /**
     * 分析用户上传行为
     * @param spaceUserAnalyzeRequest 用户上传行为分析请求数据传输对象，包含用户ID、时间维度等信息
     * @param loginUser 当前登录用户信息
     * @return 用户上传行为分析视图对象列表
     */
    List<SpaceUserAnalyzeVO> getSpaceUserAnalyze(SpaceUserAnalyzeDTO spaceUserAnalyzeRequest, UserLoginVO loginUser);

    /**
     * 获取空间存储量排名
     * @param spaceRankAnalyzeRequest 空间排名分析请求数据传输对象，包含空间ID、时间维度等信息
     * @param loginUser 当前登录用户信息
     * @return 空间列表
     */
    List<Space> getSpaceRankAnalyze(SpaceRankAnalyzeDTO spaceRankAnalyzeRequest, UserLoginVO loginUser);
}
