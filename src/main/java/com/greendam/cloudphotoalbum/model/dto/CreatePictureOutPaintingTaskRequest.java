package com.greendam.cloudphotoalbum.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建图片扩图任务请求
 * @author ForeverGreenDam
 */
@Data
public class CreatePictureOutPaintingTaskRequest implements Serializable {

    /**
     * 图片 id
     */
    private Long pictureId;

    /**
     * 扩图参数
     */
    private CreateOutPaintingTaskRequest.Parameters parameters;

    private static final long serialVersionUID = 1L;
}
