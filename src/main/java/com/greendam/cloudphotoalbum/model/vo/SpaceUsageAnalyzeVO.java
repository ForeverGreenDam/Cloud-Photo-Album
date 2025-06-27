package com.greendam.cloudphotoalbum.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 空间使用情况分析视图类
 * @author ForeverGreenDam
 */
@Data
public class SpaceUsageAnalyzeVO implements Serializable {

    /**
     * 已使用大小
     */
    private Long usedSize;

    /**
     * 总大小
     */
    private Long maxSize;

    /**
     * 空间使用比例
     */
    private Double sizeUsageRatio;

    /**
     * 当前图片数量
     */
    private Long usedCount;

    /**
     * 最大图片数量
     */
    private Long maxCount;

    /**
     * 图片数量占比
     */
    private Double countUsageRatio;

    private static final long serialVersionUID = 1L;
}
