package com.greendam.cloudphotoalbum.model.vo;

import lombok.Data;

/**
 * 以图搜图视图对象
 * @author ForeverGreenDam
 */
@Data
public class ImageSearchVO {
    /**
     * 缩略图URL
     */
    private String thumbUrl;
    /**
     * 来源URL
     */
    private String fromUrl;
}
