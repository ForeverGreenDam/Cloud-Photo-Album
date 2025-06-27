package com.greendam.cloudphotoalbum.service.impl;

import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.greendam.cloudphotoalbum.common.utils.ThrowUtils;
import com.greendam.cloudphotoalbum.constant.UserConstant;
import com.greendam.cloudphotoalbum.exception.ErrorCode;
import com.greendam.cloudphotoalbum.mapper.SpaceMapper;
import com.greendam.cloudphotoalbum.model.dto.SpaceAnalyzeDTO;
import com.greendam.cloudphotoalbum.model.dto.SpaceUsageAnalyzeDTO;
import com.greendam.cloudphotoalbum.model.entity.Picture;
import com.greendam.cloudphotoalbum.model.entity.Space;
import com.greendam.cloudphotoalbum.model.vo.SpaceUsageAnalyzeVO;
import com.greendam.cloudphotoalbum.model.vo.UserLoginVO;
import com.greendam.cloudphotoalbum.service.PictureService;
import com.greendam.cloudphotoalbum.service.SpaceAnalyzeService;
import com.greendam.cloudphotoalbum.service.SpaceService;
import com.greendam.cloudphotoalbum.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
* @author ForeverGreenDam
* @description 针对表【space(空间)】的数据库操作Service实现
* @createDate 2025-06-20 22:29:02
*/
@Service
public class SpaceAnalyzeServiceImpl extends ServiceImpl<SpaceMapper, Space>
    implements SpaceAnalyzeService {
    @Resource
    UserService userService;
    @Resource
    SpaceService spaceService;
    @Resource
    private PictureService pictureService;

    @Override
    public SpaceUsageAnalyzeVO analyzeSpaceUsage(SpaceUsageAnalyzeDTO spaceUsageAnalyzeDTO, UserLoginVO user) {
        // 参数校验
        Long spaceId = spaceUsageAnalyzeDTO.getSpaceId();
        boolean queryAll = spaceUsageAnalyzeDTO.isQueryAll();
        boolean queryPublic = spaceUsageAnalyzeDTO.isQueryPublic();
        ThrowUtils.throwIf((!queryAll&&!queryPublic)&&spaceId==null,ErrorCode.PARAMS_ERROR);
        // 校验权限
        checkSpaceAnalyzeAuth(spaceUsageAnalyzeDTO, user);
        //如果查询公共空间或全空间则在Picture表中查询
        if(queryAll || queryPublic){
            QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("picSize");
            fillAnalyzeQueryWrapper(spaceUsageAnalyzeDTO, queryWrapper);
            List<Object> pictureSizes = pictureService.getBaseMapper().selectObjs(queryWrapper);
            // 计算已使用大小
            long usedSize = pictureSizes.stream().mapToLong(result -> result instanceof Long ? (Long) result : 0).sum();
            long count = pictureSizes.size();
            //封装VO
            SpaceUsageAnalyzeVO spaceUsageAnalyzeVO = new SpaceUsageAnalyzeVO();
            spaceUsageAnalyzeVO.setUsedSize(usedSize);
            spaceUsageAnalyzeVO.setUsedCount(count);
            return spaceUsageAnalyzeVO;
        }else{
            //查询指定空间使用清况在space表中直接查询
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space==null,ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            spaceService.checkSpaceAuth(space, user);
            //封装VO
            SpaceUsageAnalyzeVO spaceUsageAnalyzeVO = new SpaceUsageAnalyzeVO();
            spaceUsageAnalyzeVO.setUsedCount(space.getTotalCount());
            spaceUsageAnalyzeVO.setUsedSize(space.getTotalSize());
            spaceUsageAnalyzeVO.setMaxSize(space.getMaxSize());
            spaceUsageAnalyzeVO.setMaxCount(space.getMaxCount());
            double sizeUsageRatio = NumberUtil.round(space.getTotalSize()*100.0 / space.getMaxSize(),2).doubleValue();
            double countUsageRatio = NumberUtil.round(space.getTotalCount()*100.0 / space.getMaxCount(),2).doubleValue();
            spaceUsageAnalyzeVO.setSizeUsageRatio(sizeUsageRatio);
            spaceUsageAnalyzeVO.setCountUsageRatio(countUsageRatio);
            return spaceUsageAnalyzeVO;
        }
    }
    /**
     * 校验空间分析权限
     * @param spaceAnalyzeDTO 空间分析 DTO
     * @param user 用户登录信息
     */
    private void checkSpaceAnalyzeAuth(SpaceAnalyzeDTO spaceAnalyzeDTO,UserLoginVO user) {
        boolean queryAll = spaceAnalyzeDTO.isQueryAll();
        boolean queryPublic = spaceAnalyzeDTO.isQueryPublic();
        Long spaceId = spaceAnalyzeDTO.getSpaceId();
        if(queryAll || queryPublic){
            ThrowUtils.throwIf(!UserConstant.ADMIN_ROLE.equals(user.getUserRole()),ErrorCode.NOT_AUTH_ERROR);
        }else{
            ThrowUtils.throwIf(spaceId == null || spaceId <= 0, ErrorCode.PARAMS_ERROR, "空间 ID 不能为空");
            // 校验空间是否存在
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            // 校验用户是否有权限访问该空间
            spaceService.checkSpaceAuth(space, user);
        }
    }

    /**
     * 填充空间分析查询条件
     * @param spaceAnalyzeDTO 空间分析 DTO
     * @param queryWrapper 待填充的查询条件构造器
     * @return 填充后的查询条件构造器
     */
    private QueryWrapper<Picture> fillAnalyzeQueryWrapper(SpaceAnalyzeDTO spaceAnalyzeDTO, QueryWrapper<Picture> queryWrapper) {
        boolean queryPublic = spaceAnalyzeDTO.isQueryPublic();
        boolean queryAll = spaceAnalyzeDTO.isQueryAll();
        Long spaceId = spaceAnalyzeDTO.getSpaceId();
        if(queryAll){
            // 全空间分析
            return queryWrapper;
        }else if(queryPublic){
            // 查询公共图库
            queryWrapper.isNull("spaceId");
        }else{
            // 查询指定空间
            queryWrapper.eq("spaceId", spaceId);
        }
        return queryWrapper;
    }
}




