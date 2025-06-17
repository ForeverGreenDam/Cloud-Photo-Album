package com.greendam.cloudphotoalbum.constant;

/**
 * 缓存常量接口
 * @author ForeverGreenDam
 */
public interface CacheConstant {
    /**
     * Redis图片缓存前缀
     */
    String REDIS_PICTURE_KEY = "cloudPhotoAlbum:picture:";
    /**
     * Redis用户缓存前缀
     */
    String REDIS_USER_KEY = "cloudPhotoAlbum:user:";
    /**
     * 本地图片缓存前缀
     */
    String CAFFEINE_PICTURE_KEY = "picture:";
    /**
     * 图片缓存过期时间前缀
     */
    int PICTURE_EXPIRE= 300; ;
}
