package com.greendam.cloudphotoalbum.controller.common;

import com.greendam.cloudphotoalbum.annotation.AuthCheck;
import com.greendam.cloudphotoalbum.common.AliOssUtil;
import com.greendam.cloudphotoalbum.common.BaseResponse;
import com.greendam.cloudphotoalbum.constant.UserConstant;
import com.greendam.cloudphotoalbum.exception.ErrorCode;
import com.greendam.cloudphotoalbum.exception.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.UUID;

/**
 * 文件通用控制器
 * @author ForeverGreenDam
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {
    @Resource
    private AliOssUtil aliOssUtil;
    /**
     * 测试文件上传
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/test/upload")
    public BaseResponse<String> upload(@RequestParam("file")MultipartFile file) {
        ThrowUtils.throwIf(file.isEmpty(), ErrorCode.PARAMS_ERROR);
        String originalFilename = file.getOriginalFilename();
        // 获取文件后缀
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        // 生成唯一文件名
        String newFileName = UUID.randomUUID().toString() + extension;
        // 上传文件到阿里云OSS
        try {
            // 上传文件到阿里云OSS
            String url= aliOssUtil.upload(file.getBytes(), newFileName);
            return BaseResponse.success(url);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


}
