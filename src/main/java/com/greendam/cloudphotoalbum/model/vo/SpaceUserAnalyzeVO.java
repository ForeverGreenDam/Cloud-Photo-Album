package com.greendam.cloudphotoalbum.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户上传行为分析视图对象
 * @author ForeverGreenDam
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceUserAnalyzeVO implements Serializable {

    /**
     * 时间区间
     */
    private String period;

    /**
     * 上传数量
     */
    private Long count;

    private static final long serialVersionUID = 1L;
}
