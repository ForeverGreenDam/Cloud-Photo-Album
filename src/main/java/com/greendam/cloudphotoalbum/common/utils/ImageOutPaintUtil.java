package com.greendam.cloudphotoalbum.common.utils;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.greendam.cloudphotoalbum.exception.BusinessException;
import com.greendam.cloudphotoalbum.exception.ErrorCode;
import com.greendam.cloudphotoalbum.model.dto.CreateOutPaintingTaskRequest;
import com.greendam.cloudphotoalbum.model.vo.CreateOutPaintingTaskResponse;
import com.greendam.cloudphotoalbum.model.vo.GetOutPaintingTaskResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * AI扩图工具类
 * @author ForeverGreenDam
 */
@Data
@Slf4j
@AllArgsConstructor
public class ImageOutPaintUtil {
    private  String apiKey;
    private  String model;
    /**
     * 创建扩图任务URL
     */
    public static final String CREATE_TASK = "https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/out-painting";
    /**
     * 获取任务结果URL
     */
    public static final String GET_TASK_RESULT = "https://dashscope.aliyuncs.com/api/v1/tasks/%s";
    /**
     * 任务状态常量
     */
    public static final String FAILED = "FAILED";
    public static final String SUCCEEDED = "SUCCEEDED";

    /**
     * 创建扩图任务
     * @param request 扩图任务请求
     * @return 扩图任务响应
     */
    public  CreateOutPaintingTaskResponse createOutPaintingTaskResponse(CreateOutPaintingTaskRequest request) {
        ThrowUtils.throwIf(request==null, ErrorCode.PARAMS_ERROR, "扩图任务请求参数不能为空");

        HttpResponse response = HttpRequest.post(CREATE_TASK)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .header("X-DashScope-Async", "enable")
                .body(JSONUtil.toJsonStr(request))
                .execute();
        if (!response.isOk()) {
            log.error("创建扩图任务失败，响应码：{}, 响应体：{}", response.getStatus(), response.body());
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        String body = response.body();
        CreateOutPaintingTaskResponse taskResponse = JSONUtil.toBean(body, CreateOutPaintingTaskResponse.class);
        response.close();
        if(taskResponse.getCode()!=null){
            log.error("创建扩图任务失败，错误码：{}，错误信息：{}，请求id：{}", taskResponse.getCode(), taskResponse.getMessage(),taskResponse.getRequestId());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, taskResponse.getMessage());
        }
        log.info("创建扩图任务成功，任务id：{}", taskResponse.getOutput().getTaskId());
        return taskResponse;
    }
    /**
     * 获取扩图任务结果
     * @param response 扩图任务响应
     * @return 扩图任务结果响应
     */
    public GetOutPaintingTaskResponse getOutPaintingTaskResponse(CreateOutPaintingTaskResponse response) {
        // 检查响应是否为空
        ThrowUtils.throwIf(response == null, ErrorCode.PARAMS_ERROR);
        //发起请求
        HttpResponse response1 = HttpRequest.get(String.format(GET_TASK_RESULT, response.getOutput().getTaskId()))
                .header("Authorization", "Bearer " + apiKey)
                .execute();
        // 检查响应状态
        if(!response1.isOk()) {
            log.error("获取扩图任务结果失败，响应码：{}, 响应体：{}", response1.getStatus(), response1.body());
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        // 解析响应体
        String body = response1.body();
        GetOutPaintingTaskResponse taskResponse = JSONUtil.toBean(body, GetOutPaintingTaskResponse.class);
        //判断任务状态
        String taskStatus = taskResponse.getOutput().getTaskStatus();
        if(FAILED.equals(taskStatus)) {
            log.error("扩图任务失败，任务ID：{}，错误码：{}，错误信息：{}", taskResponse.getOutput().getTaskId(), taskResponse.getOutput().getCode(), taskResponse.getOutput().getMessage());
            response1.close();
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        } else if (SUCCEEDED.equals(taskStatus)) {
            log.info("创建扩图任务成功，图片URL：{}",taskResponse.getOutput().getOutputImageUrl());
            response1.close();
            return taskResponse;
        }else {
            log.warn("扩图任务进行中，任务ID：{}，状态：{}", taskResponse.getOutput().getTaskId(), taskStatus);
            response1.close();
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "扩图任务进行中，请稍后再试");
        }
    }
}
