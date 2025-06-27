package com.greendam.cloudphotoalbum.controller.common;

import com.greendam.cloudphotoalbum.common.BaseResponse;
import com.greendam.cloudphotoalbum.common.utils.ThrowUtils;
import com.greendam.cloudphotoalbum.exception.ErrorCode;
import com.greendam.cloudphotoalbum.model.dto.SpaceCategoryAnalyzeDTO;
import com.greendam.cloudphotoalbum.model.dto.SpaceUsageAnalyzeDTO;
import com.greendam.cloudphotoalbum.model.vo.SpaceCategoryAnalyzeVO;
import com.greendam.cloudphotoalbum.model.vo.SpaceUsageAnalyzeVO;
import com.greendam.cloudphotoalbum.model.vo.UserLoginVO;
import com.greendam.cloudphotoalbum.service.SpaceAnalyzeService;
import com.greendam.cloudphotoalbum.service.SpaceService;
import com.greendam.cloudphotoalbum.service.UserService;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping("/category")
    public BaseResponse<List<SpaceCategoryAnalyzeVO>> getSpaceCategoryAnalyze(@RequestBody SpaceCategoryAnalyzeDTO spaceCategoryAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceCategoryAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        UserLoginVO loginUser = userService.getUser(request);
        List<SpaceCategoryAnalyzeVO> resultList = spaceAnalyzeService.getSpaceCategoryAnalyze(spaceCategoryAnalyzeRequest, loginUser);
        return BaseResponse.success(resultList);
    }



}


