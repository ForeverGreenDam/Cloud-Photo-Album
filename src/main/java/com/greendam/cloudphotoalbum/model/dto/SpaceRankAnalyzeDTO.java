package com.greendam.cloudphotoalbum.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 空间排名分析 DTO
 * @author ForeverGreenDam
 */
@Data
public class SpaceRankAnalyzeDTO implements Serializable {

    /**
     * 排名前 N 的空间
     */
    private Integer topN = 10;

    private static final long serialVersionUID = 1L;
}
