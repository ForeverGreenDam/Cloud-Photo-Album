package com.greendam.cloudphotoalbum.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 空间编辑数据DTO(用户使用)
 * @author ForeverGreenDam
 */
@Data
public class SpaceEditDTO implements Serializable {

    /**
     * 空间 id
     */
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;

    private static final long serialVersionUID = 1L;
}
