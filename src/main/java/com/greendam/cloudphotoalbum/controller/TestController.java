package com.greendam.cloudphotoalbum.controller;

import com.greendam.cloudphotoalbum.common.BaseResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试控制器
 * @author ForeverGreenDam
 */
@RestController
@RequestMapping("/")
public class TestController {
    /**
     * 健康检查
     */
    @GetMapping("/health")
    public BaseResponse<String> health(){
        return BaseResponse.success("ok");
    }
}
