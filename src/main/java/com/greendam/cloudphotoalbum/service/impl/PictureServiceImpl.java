package com.greendam.cloudphotoalbum.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.greendam.cloudphotoalbum.common.AliOssUtil;
import com.greendam.cloudphotoalbum.constant.UserConstant;
import com.greendam.cloudphotoalbum.exception.BusinessException;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Arrays;
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
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewerId = pictureQueryRequest.getReviewerId();


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
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.like(StrUtil.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), "reviewerId", reviewerId);
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
        Integer reviewStatus = oldPicture.getReviewStatus();
        PictureReviewStatusEnum statusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
        ThrowUtils.throwIf( !PictureReviewStatusEnum.REVIEWING.equals(statusEnum), ErrorCode.PARAMS_ERROR, "请勿重复审核");
        //3.更新审核状态
        Picture newPicture = new Picture();
        BeanUtil.copyProperties(pictureReviewDTO, newPicture);
        newPicture.setReviewerId(id);
        newPicture.setReviewTime(LocalDateTime.now());
        int i = pictureMapper.updateById(newPicture);
        ThrowUtils.throwIf(i == 0, ErrorCode.OPERATION_ERROR, "审核失败");
    }

    @Override
    public PictureVO uploadPictureByUrl(String fileUrl, PictureUploadDTO pictureUploadDTO, UserLoginVO loginUser) {
        //判断是否为更新操作（即已经上传过图片，但是不满意，准备换为别的图片）
        Long pictureId = null;
        if(pictureUploadDTO!=null){pictureId=pictureUploadDTO.getId();}
        //如果pictureId不为空，说明是更新操作, 先查数据库该对象是否存在
        if(pictureId!=null){
            Picture exit = pictureMapper.selectById(pictureId);
            ThrowUtils.throwIf(exit==null,ErrorCode.PARAMS_ERROR);
            //如果存在，检查用户是否有权限更新该图片
            ThrowUtils.throwIf(!exit.getUserId().equals(loginUser.getId()) &&
                            !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole()),
                    ErrorCode.NOT_AUTH_ERROR, "无权限更新该图片");
        }
        //验证图片URL的合法性,同时获取文件后缀
        String extension=validPicture(fileUrl);
        ThrowUtils.throwIf(extension==null, ErrorCode.PARAMS_ERROR, "不支持HEAD请求的图片地址");
        File file=null;
        try {
        //从URL获取文件
        String uuid = UUID.randomUUID().toString();
        file=File.createTempFile(uuid,null);
        HttpUtil.downloadFile(fileUrl, file);
        // 生成唯一文件名
        String newFileName = uuid +'.'+ extension;
            //上传文件获得url
            String url = aliOssUtil.upload(Files.readAllBytes(file.toPath()), newFileName);
            //创建图片对象
            Picture picture = new Picture();
            //设置基本信息
            picture.setUrl(url);
            picture.setName(newFileName);
            picture.setPicFormat(extension);
            picture.setUserId(loginUser.getId());
            //图片解析
            BufferedImage image = ImageIO.read(file);
            picture.setPicSize(file.length());
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
            if(file!=null){
                boolean delete = file.delete();
                if(!delete){
                    log.error("临时文件删除失败，路径：{}", file.getAbsolutePath());
                }
            }
        }
    }
    /**
     * 验证图片URL的合法性
     * @param fileUrl 图片的URL地址
     */
    private String validPicture(String fileUrl) {
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "文件地址不能为空");

        try {
            // 1. 验证 URL 格式
            // 验证是否是合法的 URL
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件地址格式不正确");
        }

        // 2. 校验 URL 协议
        ThrowUtils.throwIf(!(fileUrl.startsWith("http://") || fileUrl.startsWith("https://")),
                ErrorCode.PARAMS_ERROR, "仅支持 HTTP 或 HTTPS 协议的文件地址");

        // 3. 发送 HEAD 请求以验证文件是否存在
        HttpResponse response = null;
        try {
            response = HttpUtil.createRequest(Method.HEAD, fileUrl).execute();
            // 未正常返回，无需执行其他判断
            if (response.getStatus() != HttpStatus.HTTP_OK) {
                return null;
            }
            // 4. 校验文件类型
            String contentType = response.header("Content-Type");
            if (StrUtil.isNotBlank(contentType)) {
                // 允许的图片类型
                final List<String> ALLOW_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/webp");
                ThrowUtils.throwIf(!ALLOW_CONTENT_TYPES.contains(contentType.toLowerCase()),
                        ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
            // 5. 校验文件大小
            String contentLengthStr = response.header("Content-Length");
            if (StrUtil.isNotBlank(contentLengthStr)) {
                try {
                    long contentLength = Long.parseLong(contentLengthStr);
                    // 限制文件大小为 10MB
                    final long TWO_MB = 10 * 1024 * 1024L;
                    ThrowUtils.throwIf(contentLength > TWO_MB, ErrorCode.PARAMS_ERROR, "文件大小不能超过 10M");
                } catch (NumberFormatException e) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小格式错误");
                }
            }
            // 6. 返回文件类型
            return contentType.toLowerCase().substring(contentType.lastIndexOf('/')+1);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }


}




