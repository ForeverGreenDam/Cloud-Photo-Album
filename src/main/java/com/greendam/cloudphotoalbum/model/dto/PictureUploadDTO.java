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
}
