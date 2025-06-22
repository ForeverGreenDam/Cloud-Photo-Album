package com.greendam.cloudphotoalbum.controller.common;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.greendam.cloudphotoalbum.annotation.AuthCheck;
import com.greendam.cloudphotoalbum.common.BaseResponse;
import com.greendam.cloudphotoalbum.common.DeleteRequest;
import com.greendam.cloudphotoalbum.constant.UserConstant;
import com.greendam.cloudphotoalbum.exception.ErrorCode;
import com.greendam.cloudphotoalbum.exception.ThrowUtils;
import com.greendam.cloudphotoalbum.model.dto.*;
import com.greendam.cloudphotoalbum.model.entity.Picture;
import com.greendam.cloudphotoalbum.model.enums.PictureReviewStatusEnum;
import com.greendam.cloudphotoalbum.model.enums.SpaceLevelEnum;
import com.greendam.cloudphotoalbum.model.vo.PictureVO;
import com.greendam.cloudphotoalbum.model.vo.SpaceVO;
import com.greendam.cloudphotoalbum.model.vo.UserLoginVO;
import com.greendam.cloudphotoalbum.service.PictureService;
import com.greendam.cloudphotoalbum.service.SpaceService;
import com.greendam.cloudphotoalbum.service.UserService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
    @PostMapping("add")
    @AuthCheck
    public BaseResponse<SpaceVO> addSpace(){return null;}
}


