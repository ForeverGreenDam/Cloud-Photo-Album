package com.greendam.cloudphotoalbum.model.dto;

import com.greendam.cloudphotoalbum.model.enums.PictureReviewStatusEnum;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author ForeverGreenDam
 */
@Data
public class PictureReviewDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 图片 ID
     */
    private Long id;
    /**
     * 审核状态：0-待审核; 1-通过; 2-拒绝
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;
}
