package com.greendam.cloudphotoalbum.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.greendam.cloudphotoalbum.exception.BusinessException;
import com.greendam.cloudphotoalbum.exception.ErrorCode;
import com.greendam.cloudphotoalbum.mapper.SpaceUserMapper;
import com.greendam.cloudphotoalbum.model.entity.Space;
import com.greendam.cloudphotoalbum.model.entity.SpaceUser;
import com.greendam.cloudphotoalbum.model.enums.SpaceTypeEnum;
import com.greendam.cloudphotoalbum.service.SpaceUserService;
import org.springframework.stereotype.Service;

/**
* @author ForeverGreenDam
* @description 针对表【space_user(空间用户关联)】的数据库操作Service实现
* @createDate 2025-06-28 16:49:52
*/
@Service
public class SpaceUserServiceImpl extends ServiceImpl<SpaceUserMapper, SpaceUser>
    implements SpaceUserService{

}




