package com.greendam.cloudphotoalbum.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 图片上传数据传输对象
 * @author ForeverGreenDam
 */
@Data
public class PictureUploadDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 图片ID
     */
    private Long id;
    /**
     * 图片地址（通过URL传递图片时使用）
     */
    private String fileUrl;
    /**
     * 图片名称（用于批量抓图命名）
     */
    private String picName;
    /**
     * 空间ID（用于标识图片所属的空间）
     */
    private Long spaceId;
}
