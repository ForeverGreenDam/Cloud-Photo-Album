package com.greendam.cloudphotoalbum.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.greendam.cloudphotoalbum.common.AliOssUtil;
import com.greendam.cloudphotoalbum.constant.UserConstant;
import com.greendam.cloudphotoalbum.exception.ErrorCode;
import com.greendam.cloudphotoalbum.exception.ThrowUtils;
import com.greendam.cloudphotoalbum.mapper.UserMapper;
import com.greendam.cloudphotoalbum.model.dto.PictureQueryDTO;
import com.greendam.cloudphotoalbum.model.dto.PictureReviewDTO;
import com.greendam.cloudphotoalbum.model.dto.PictureUpdateDTO;
import com.greendam.cloudphotoalbum.model.dto.PictureUploadDTO;
import com.greendam.cloudphotoalbum.model.entity.Picture;
import com.greendam.cloudphotoalbum.mapper.PictureMapper;
import com.greendam.cloudphotoalbum.model.entity.User;
import com.greendam.cloudphotoalbum.model.enums.PictureReviewStatusEnum;
import com.greendam.cloudphotoalbum.model.vo.PictureVO;
import com.greendam.cloudphotoalbum.model.vo.UserLoginVO;
import com.greendam.cloudphotoalbum.model.vo.UserVO;
import com.greendam.cloudphotoalbum.service.PictureService ;
import com.greendam.cloudphotoalbum.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
* @author ForeverGreenDam
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2025-06-06 14:26:53
*/
@Service
@Slf4j
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService{
    @Resource
    private AliOssUtil aliOssUtil;
    @Resource
    private UserService userService;
    @Resource
    private PictureMapper pictureMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public PictureVO uploadPicture(MultipartFile file, PictureUploadDTO pictureUploadDTO, HttpServletRequest request) {
        //判断是否为更新操作（即已经上传过图片，但是不满意，准备换为别的图片）
        Long pictureId = null;
        if(pictureUploadDTO!=null){pictureId=pictureUploadDTO.getId();}
        //如果pictureId不为空，说明是更新操作, 先查数据库该对象是否存在
        if(pictureId!=null){
            Picture exit = pictureMapper.selectById(pictureId);
            ThrowUtils.throwIf(exit==null,ErrorCode.PARAMS_ERROR);
            //如果存在，检查用户是否有权限更新该图片
            ThrowUtils.throwIf(!exit.getUserId().equals(userService.getUser(request).getId()) &&
                    !UserConstant.ADMIN_ROLE.equals(userService.getUser(request).getUserRole()),
                    ErrorCode.NOT_AUTH_ERROR, "无权限更新该图片");
        }
        String originalFilename = file.getOriginalFilename();
        // 获取文件后缀
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        // 生成唯一文件名
        String newFileName = UUID.randomUUID().toString() + extension;
        File pictureFile=null;
        try {
            //上传文件获得url
            String url = aliOssUtil.upload(file.getBytes(), newFileName);
            //创建图片对象
            Picture picture = new Picture();
            //设置基本信息
            picture.setUrl(url);
            picture.setName(originalFilename);
            picture.setPicFormat(extension);
            picture.setUserId(userService.getUser(request).getId());

            //将MultipartFile转换为File,供ImageIO读取
            pictureFile=File.createTempFile(newFileName,null);
            FileCopyUtils.copy(file.getBytes(),pictureFile);
            //图片解析
            BufferedImage image = ImageIO.read(pictureFile);
            picture.setPicSize(file.getSize());
            picture.setPicWidth(image.getWidth());
            picture.setPicHeight(image.getHeight());
            picture.setPicScale((double)image.getWidth()/ (double) image.getHeight());
            if(pictureId!=null){
                //更新操作，需要手动设置图片id以及更新时间
                picture.setId(pictureId);
                picture.setEditTime(LocalDateTime.now());
                pictureMapper.updateById(picture);
            }else{
                //新建操作，直接插入
                pictureMapper.insert(picture);
            }
            return PictureVO.objToVo(picture);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            if(pictureFile!=null){
                boolean delete = pictureFile.delete();
                if(!delete){
                    log.error("临时文件删除失败，路径：{}", pictureFile.getAbsolutePath());
                }
            }
        }
    }
    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryDTO pictureQueryRequest) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (pictureQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();
        // 从多字段中搜索
        if (StrUtil.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("name", searchText)
                    .or()
                    .like("introduction", searchText)
            );
        }
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);
        // JSON 数组查询
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public void deletePicture(Long pictureId, HttpServletRequest request) {
        UserLoginVO user=userService.getUser(request);
        String userRole = user.getUserRole();
        Picture picture = pictureMapper.selectById(pictureId);
        //如果图片不存在，抛出异常
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR, "图片不存在");
        //如果用户不是管理员，并且不是图片的上传者，抛出异常
        ThrowUtils.throwIf((!UserConstant.ADMIN_ROLE.equals(userRole))&&
                !picture.getUserId().equals(user.getId()), ErrorCode.NOT_AUTH_ERROR, "无权限删除该图片");
        //删除图片
        pictureMapper.deleteById(pictureId);
    }

    @Override
    public boolean updatePicture(PictureUpdateDTO pictureUpdateDTO) {
        //首先查询是否存在该图片
        Picture oldPicture = pictureMapper.selectById(pictureUpdateDTO.getId());
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        //更新图片信息
        Picture newPicture = new Picture();
        BeanUtils.copyProperties(pictureUpdateDTO, newPicture);
        newPicture.setTags(JSONUtil.toJsonStr(pictureUpdateDTO.getTags()));
        //更新到数据库
        int i = pictureMapper.updateById(newPicture);
        ThrowUtils.throwIf(i == 0, ErrorCode.OPERATION_ERROR);
        return true;
    }

    @Override
    public PictureVO getPictureVO(Picture picture) {
        PictureVO pictureVO = PictureVO.objToVo(picture);
        //补充userVO字段
        //检查用户id有效性
        ThrowUtils.throwIf(pictureVO.getUserId() == null, ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        User user = userMapper.selectById(pictureVO.getUserId());
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
        pictureVO.setUser(userVO);
        return pictureVO;
    }

    @Override
    public void pictureReview(PictureReviewDTO pictureReviewDTO, Long id) {
        //1.获取旧图片
        Picture oldPicture = pictureMapper.selectById(pictureReviewDTO.getId());
        ThrowUtils.throwIf(oldPicture==null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        //2.检查审核状态
        Integer reviewStatus = pictureReviewDTO.getReviewStatus();
        PictureReviewStatusEnum statusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
        ThrowUtils.throwIf( !PictureReviewStatusEnum.REVIEWING.equals(statusEnum), ErrorCode.PARAMS_ERROR, "请勿重复审核");
        //3.更新审核状态
        Picture newPicture = new Picture();
        BeanUtil.copyProperties(pictureReviewDTO, newPicture);
        int i = pictureMapper.updateById(newPicture);
        ThrowUtils.throwIf(i == 0, ErrorCode.OPERATION_ERROR, "审核失败");
    }

}




