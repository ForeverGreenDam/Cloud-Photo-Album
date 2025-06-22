package com.greendam.cloudphotoalbum.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 图片分类标签类
 * @author ForeverGreenDam
 */
@Data
public class PictureTagCategory {
    /**
     * 图片分类列表
     */
    private List<String> categoryList;
    /**
     * 图片标签列表
     */
    private List<String> tagList;
}
