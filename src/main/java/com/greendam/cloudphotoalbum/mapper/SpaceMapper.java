package com.greendam.cloudphotoalbum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.greendam.cloudphotoalbum.model.entity.Space;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author ForeverGreenDam
* @description 针对表【space(空间)】的数据库操作Mapper
* @createDate 2025-06-20 22:29:02
* @Entity com.greendam.cloudphotoalbum.model.entity.Space
*/
public interface SpaceMapper extends BaseMapper<Space> {
    /**
     * 根据用户ID查询空间
     * @param id 用户ID
     * @return 空间信息
     */
    @Select("select * from space where userId = #{id}")
    List<Space> selectByUserId(Long id);
}




