package com.greendam.cloudphotoalbum.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.greendam.cloudphotoalbum.common.DeleteRequest;
import com.greendam.cloudphotoalbum.constant.UserConstant;
import com.greendam.cloudphotoalbum.exception.BusinessException;
import com.greendam.cloudphotoalbum.exception.ErrorCode;
import com.greendam.cloudphotoalbum.exception.ThrowUtils;
import com.greendam.cloudphotoalbum.mapper.SpaceMapper;
import com.greendam.cloudphotoalbum.model.dto.*;
import com.greendam.cloudphotoalbum.model.entity.Space;
import com.greendam.cloudphotoalbum.model.enums.SpaceLevelEnum;
import com.greendam.cloudphotoalbum.model.vo.SpaceVO;
import com.greendam.cloudphotoalbum.model.vo.UserLoginVO;
import com.greendam.cloudphotoalbum.model.vo.UserVO;
import com.greendam.cloudphotoalbum.service.SpaceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
* @author ForeverGreenDam
* @description 针对表【space(空间)】的数据库操作Service实现
* @createDate 2025-06-20 22:29:02
*/
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
    implements SpaceService{
    @Resource
    private SpaceMapper spaceMapper;
    @Resource
    private TransactionTemplate transactionTemplate;
    /**
     * 锁对象，防止并发创建空间
     * @key: userId
     * @value: 锁对象
     */
    private final Map<Long, Object> lockMap=new ConcurrentHashMap<>();

    @Override
    public Long addSpace(SpaceAddDTO spaceAddDTO, UserLoginVO loginUser) {
        // 校验参数
        Space space = new Space();
        BeanUtil.copyProperties(spaceAddDTO, space);
        validSpace(space, true);
        //校验spaceLevel，普通用户只能创建普通版
        ThrowUtils.throwIf(SpaceLevelEnum.COMMON.getValue()!=space.getSpaceLevel()
                        && UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole()),
                ErrorCode.NOT_FOUND_ERROR, "普通用户只能创建普通版空间，请联系管理员开通专业版或旗舰版空间");
        //填充数据
        fillSpaceBySpaceLevel(space);
        space.setUserId(loginUser.getId());
        //加锁，防止并发操作导致重复创建空间
        Long userId = loginUser.getId();
        Object lock = lockMap.computeIfAbsent(userId, k -> new Object());
        //锁粒度仅限同一个用户重复创建空间时使用
        synchronized (lock) {
            Long spaceId = transactionTemplate.execute(status -> {
                //限制每个用户只能创建一个空间
                List<Space> spaces = spaceMapper.selectByUserId(loginUser.getId());
                ThrowUtils.throwIf(!spaces.isEmpty(), ErrorCode.OPERATION_ERROR, "每个用户只能创建一个空间");
                // 执行插入操作
                int insert = spaceMapper.insert(space);
                ThrowUtils.throwIf(insert == 0, ErrorCode.OPERATION_ERROR);
                return space.getId();
            });
            return spaceId;
        }
    }

    @Override
    public void deleteSpace(DeleteRequest deleteRequest, UserLoginVO user) {
        Long spaceId = deleteRequest.getId();
        Long userId = user.getId();
        // 获取空间
        Space space = spaceMapper.selectById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        // 校验空间是否属于当前用户(或者管理员)
        ThrowUtils.throwIf(!space.getUserId().equals(userId) && !UserConstant.ADMIN_ROLE.equals(user.getUserRole()),
                ErrorCode.NOT_FOUND_ERROR, "没有权限删除该空间");
        // 删除空间
        int i = spaceMapper.deleteById(spaceId);
        ThrowUtils.throwIf(i == 0, ErrorCode.OPERATION_ERROR, "删除空间失败");
    }

    @Override
    public void edit(SpaceEditDTO spaceEditDTO, UserLoginVO user) {
        //校验参数
        String spaceName = spaceEditDTO.getSpaceName();
        Long spaceId = spaceEditDTO.getId();
        ThrowUtils.throwIf(spaceName.isEmpty() || spaceName.length() > 30, ErrorCode.PARAMS_ERROR, "空间名称不合法");
        ThrowUtils.throwIf(spaceId == null, ErrorCode.PARAMS_ERROR, "空间ID不能为空");
        //校验权限
        Space space = spaceMapper.selectById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        Long userId = user.getId();
        ThrowUtils.throwIf(!space.getUserId().equals(userId) && !UserConstant.ADMIN_ROLE.equals(user.getUserRole()),
                ErrorCode.NOT_FOUND_ERROR, "没有权限编辑该空间");
        // 更新空间信息
        space.setEditTime(LocalDateTime.now());
        spaceMapper.updateById(space);
    }

    @Override
    public Long updateSpace(SpaceUpdateDTO spaceUpdateDTO) {
        Space space = new Space();
        BeanUtil.copyProperties(spaceUpdateDTO, space);
        // 校验参数
        validSpace(space, false);
        Long spaceId = space.getId();
        String spaceName = space.getSpaceName();
        ThrowUtils.throwIf(spaceId == null, ErrorCode.PARAMS_ERROR, "空间ID不能为空");
        ThrowUtils.throwIf(spaceName.isEmpty() || spaceName.length() > 30, ErrorCode.PARAMS_ERROR, "空间名称不合法");
        // 判断空间是否存在
        Space existingSpace = spaceMapper.selectById(spaceId);
        ThrowUtils.throwIf(existingSpace == null, ErrorCode.NOT_FOUND_ERROR,"空间不存在");
        //填充参数
        fillSpaceBySpaceLevel(space);
        // 更新空间信息
        spaceMapper.updateById(space);
        return spaceId;
    }

    @Override
    public Page<Space> getSpacePage(SpaceQueryDTO spaceQueryDTO) {
        int current = spaceQueryDTO.getCurrent();
        int pageSize = spaceQueryDTO.getPageSize();
        ThrowUtils.throwIf(current <= 0 || pageSize <= 0, ErrorCode.PARAMS_ERROR, "分页参数不合法");
        QueryWrapper<Space> queryWrapper = getQueryWrapper(spaceQueryDTO);
        Page<Space> page = this.page(new Page<>(current, pageSize), queryWrapper);
        ThrowUtils.throwIf(page== null, ErrorCode.NOT_FOUND_ERROR, "没有找到空间数据");
        return page;
    }

    @Override
    public Page<SpaceVO> getSpacePageVO(SpaceQueryDTO spaceQueryDTO, UserLoginVO user) {
        //鉴权，仅能查询自己的空间列表
        Long userId = user.getId();
        ThrowUtils.throwIf(spaceQueryDTO.getUserId()==null
                || (!spaceQueryDTO.getUserId().equals(userId) && !UserConstant.ADMIN_ROLE.equals(user.getUserRole()))
                , ErrorCode.PARAMS_ERROR, "仅限查询自己的空间列表");
        //获取分页数据
        Page<Space> spacePage = getSpacePage(spaceQueryDTO);
        ThrowUtils.throwIf(spacePage == null, ErrorCode.NOT_FOUND_ERROR, "没有找到空间数据");
        //脱敏
        UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
        List<Space> records = spacePage.getRecords();
        List<SpaceVO> voList = records.stream().map(space -> {
            SpaceVO spaceVO = new SpaceVO();
            BeanUtil.copyProperties(space, spaceVO);
            spaceVO.setUser(userVO);
            return spaceVO;
        }).collect(Collectors.toList());
        // 封装分页结果
        Page<SpaceVO> spaceVOPage = new Page<>();
        BeanUtil.copyProperties(spacePage, spaceVOPage,true);
        spaceVOPage.setRecords(voList);
        return spaceVOPage;
    }

    /**
     * 校验空间数据
     * @param space 空间数据
     * @param add 是否是新增操作
     */
    private void validSpace(Space space, boolean add) {
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
        // 要创建
        if (add) {
            if (StrUtil.isBlank(spaceName)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称不能为空");
            }
            if (spaceLevel == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不能为空");
            }
        }
        // 修改数据时，如果要改空间级别
        if (spaceLevel != null && spaceLevelEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不存在");
        }
        if (StrUtil.isNotBlank(spaceName) && spaceName.length() > 30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称过长");
        }
    }
    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        // 根据空间级别，自动填充限额
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if (spaceLevelEnum != null) {
            long maxSize = spaceLevelEnum.getMaxSize();
            if (space.getMaxSize() == null) {
                space.setMaxSize(maxSize);
            }
            long maxCount = spaceLevelEnum.getMaxCount();
            if (space.getMaxCount() == null) {
                space.setMaxCount(maxCount);
            }
        }
    }
    private QueryWrapper<Space> getQueryWrapper(SpaceQueryDTO spaceQueryDTO){
        Long id = spaceQueryDTO.getId();
        Long userId = spaceQueryDTO.getUserId();
        String spaceName = spaceQueryDTO.getSpaceName();
        Integer spaceLevel = spaceQueryDTO.getSpaceLevel();
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id)
                .eq(userId != null, "userId", userId)
                .like(StrUtil.isNotBlank(spaceName), "spaceName", spaceName)
                .eq(spaceLevel != null, "spaceLevel", spaceLevel)
                .orderByDesc("createTime");
        return queryWrapper;
    }
}




