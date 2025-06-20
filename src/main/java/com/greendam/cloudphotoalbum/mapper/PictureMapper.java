package com.greendam.cloudphotoalbum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.greendam.cloudphotoalbum.model.entity.Picture ;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
* @author ForeverGreenDam
* @description 针对表【picture(图片)】的数据库操作Mapper
* @createDate 2025-06-06 14:26:53
* @Entity generator.domain.Picture
*/
public interface PictureMapper extends BaseMapper<Picture> {
    /**
     * 查询所有逻辑删除的图片URL
     * @return 逻辑删除的图片URL列表
     */
    @Select("select url from picture where isDelete = 1")
    List<String> selectDeleteUrls();
    /**
     * 将逻辑删除的图片转为物理删除
     */
    @Update("update picture set isDelete = 2 where isDelete = 1")
    void updateDeleteStatus();
}




