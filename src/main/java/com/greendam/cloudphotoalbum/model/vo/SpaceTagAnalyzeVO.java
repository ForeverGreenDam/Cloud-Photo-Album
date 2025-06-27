package com.greendam.cloudphotoalbum.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 空间标签分析响应对象
 * @author ForeverGreenDam
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceTagAnalyzeVO implements Serializable {

    /**
     * 标签名称
     */
    private String tag;

    /**
     * 使用次数
     */
    private Long count;

    private static final long serialVersionUID = 1L;
}
