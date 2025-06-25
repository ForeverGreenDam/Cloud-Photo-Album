package com.greendam.cloudphotoalbum.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 以图搜图数据传输对象
 * @author ForeverGreenDam
 */
@Data
public class SearchPictureByPictureDTO implements Serializable {
        /**
         * 图片 id
         */
        private Long pictureId;

        private static final long serialVersionUID = 1L;
}
