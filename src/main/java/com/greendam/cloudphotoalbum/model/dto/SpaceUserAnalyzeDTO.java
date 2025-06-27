package com.greendam.cloudphotoalbum.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户上传行为分析数据传输对象
 * @author ForeverGreenDam
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceUserAnalyzeDTO extends SpaceAnalyzeDTO {

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 时间维度：day / week / month
     */
    private String timeDimension;
}
