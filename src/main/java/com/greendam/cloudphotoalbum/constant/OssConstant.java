package com.greendam.cloudphotoalbum.constant;

/**
 * 常量类，定义与OSS相关的常量
 * @author ForeverGreenDam
 */
public interface OssConstant {
    /**
     * 图片处理参数，生成缩略图，生成策略：height固定为180px，宽度按比例缩放
     */
    String  THUMBNAIL = "?x-oss-process=image/resize,h_180,limit_0";
    /**
     * 图片处理参数，获取主色调
     */
    String AVERAGE_HUE = "?x-oss-process=image/average-hue";
    /**
     * 图片处理参数，获取PNG格式的图片
     */
    String GET_PNG="?x-oss-process=image/format,png";
}
