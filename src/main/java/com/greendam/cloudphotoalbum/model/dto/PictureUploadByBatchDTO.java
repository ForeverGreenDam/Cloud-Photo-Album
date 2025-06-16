package com.greendam.cloudphotoalbum.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 批量抓取图片数据传输对象
 * @author ForeverGreenDam
 */
@Data
public class PictureUploadByBatchDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 抓取关键词
     */
    private String searchText;
    /**
     * 抓取图片数量
     */
    private Integer count=10;
}
