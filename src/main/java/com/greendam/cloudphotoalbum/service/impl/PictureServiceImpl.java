package com.greendam.cloudphotoalbum.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.greendam.cloudphotoalbum.model.entity.Picture;
import com.greendam.cloudphotoalbum.mapper.PictureMapper;
import com.greendam.cloudphotoalbum.service.PictureService ;
import org.springframework.stereotype.Service;

/**
* @author ForeverGreenDam
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2025-06-06 14:26:53
*/
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService{

}




