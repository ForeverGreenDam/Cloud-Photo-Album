package com.greendam.cloudphotoalbum.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * DTO类，用于更新图片信息
 * @author ForeverGreenDam
 */
@Data
public class PictureUpdateDTO implements Serializable {
  
    /**  
     * id  
     */  
    private Long id;  
  
    /**  
     * 图片名称  
     */  
    private String name;  
  
    /**  
     * 简介  
     */  
    private String introduction;  
  
    /**  
     * 分类  
     */  
    private String category;  
  
    /**  
     * 标签  
     */  
    private List<String> tags;
  
    private static final long serialVersionUID = 1L;  
}
