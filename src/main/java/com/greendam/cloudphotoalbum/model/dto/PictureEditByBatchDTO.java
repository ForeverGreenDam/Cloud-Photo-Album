package com.greendam.cloudphotoalbum.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 批量修改图片信息的数据传输对象
 * @author ForeverGreenDam
 */
@Data
public class PictureEditByBatchDTO implements Serializable {

    /**
     * 图片 id 列表
     */
    private List<Long> pictureIdList;

    /**
     * 空间 id
     */
    private Long spaceId;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签
     */
    private List<String> tags;
    /**
     * 命名规则
     */
    private String nameRule;


    private static final long serialVersionUID = 1L;
}
