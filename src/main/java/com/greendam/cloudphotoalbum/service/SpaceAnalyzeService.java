package com.greendam.cloudphotoalbum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.greendam.cloudphotoalbum.model.dto.SpaceUsageAnalyzeDTO;
import com.greendam.cloudphotoalbum.model.entity.Space;
import com.greendam.cloudphotoalbum.model.vo.SpaceUsageAnalyzeVO;
import com.greendam.cloudphotoalbum.model.vo.UserLoginVO;

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
}
