package com.greendam.cloudphotoalbum.service.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.greendam.cloudphotoalbum.common.utils.ThrowUtils;
import com.greendam.cloudphotoalbum.constant.UserConstant;
import com.greendam.cloudphotoalbum.exception.ErrorCode;
import com.greendam.cloudphotoalbum.mapper.SpaceMapper;
import com.greendam.cloudphotoalbum.model.dto.SpaceAnalyzeDTO;
import com.greendam.cloudphotoalbum.model.dto.SpaceCategoryAnalyzeDTO;
import com.greendam.cloudphotoalbum.model.dto.SpaceTagAnalyzeDTO;
import com.greendam.cloudphotoalbum.model.dto.SpaceUsageAnalyzeDTO;
import com.greendam.cloudphotoalbum.model.entity.Picture;
import com.greendam.cloudphotoalbum.model.entity.Space;
import com.greendam.cloudphotoalbum.model.vo.SpaceCategoryAnalyzeVO;
import com.greendam.cloudphotoalbum.model.vo.SpaceTagAnalyzeVO;
import com.greendam.cloudphotoalbum.model.vo.SpaceUsageAnalyzeVO;
import com.greendam.cloudphotoalbum.model.vo.UserLoginVO;
import com.greendam.cloudphotoalbum.service.PictureService;
import com.greendam.cloudphotoalbum.service.SpaceAnalyzeService;
import com.greendam.cloudphotoalbum.service.SpaceService;
import com.greendam.cloudphotoalbum.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @Override
    public List<SpaceCategoryAnalyzeVO> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeDTO spaceCategoryAnalyzeRequest, UserLoginVO loginUser) {
        ThrowUtils.throwIf(spaceCategoryAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        // 检查权限
        checkSpaceAnalyzeAuth(spaceCategoryAnalyzeRequest, loginUser);
        // 构造查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        // 根据分析范围补充查询条件
        fillAnalyzeQueryWrapper(spaceCategoryAnalyzeRequest, queryWrapper);

        // 使用 MyBatis-Plus 分组查询
        queryWrapper.select("category AS category",
                        "COUNT(*) AS count",
                        "SUM(picSize) AS totalSize")
                .groupBy("category");

        // 查询并转换结果
        return pictureService.getBaseMapper().selectMaps(queryWrapper)
                .stream()
                .map(result -> {
                    String category = result.get("category") != null ? result.get("category").toString() : "未分类";
                    Long count = ((Number) result.get("count")).longValue();
                    Long totalSize = ((Number) result.get("totalSize")).longValue();
                    return new SpaceCategoryAnalyzeVO(category, count, totalSize);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<SpaceTagAnalyzeVO> getSpaceTagAnalyze(SpaceTagAnalyzeDTO spaceTagAnalyzeRequest, UserLoginVO loginUser) {
            ThrowUtils.throwIf(spaceTagAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
            // 检查权限
            checkSpaceAnalyzeAuth(spaceTagAnalyzeRequest, loginUser);
            // 构造查询条件
            QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
            fillAnalyzeQueryWrapper(spaceTagAnalyzeRequest, queryWrapper);
            // 查询所有符合条件的标签
            queryWrapper.select("tags");
            List<String> tagsJsonList = pictureService.getBaseMapper().selectObjs(queryWrapper)
                    .stream()
                    .filter(ObjUtil::isNotNull)
                    .map(Object::toString)
                    .collect(Collectors.toList());
            // 合并所有标签并统计使用次数
            Map<String, Long> tagCountMap = tagsJsonList.stream()
                    .flatMap(tagsJson -> JSONUtil.toList(tagsJson, String.class).stream())
                    .collect(Collectors.groupingBy(tag -> tag, Collectors.counting()));
            // 转换为响应对象，按使用次数降序排序
            return tagCountMap.entrySet().stream()
                    .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                    .map(entry -> new SpaceTagAnalyzeVO(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
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




