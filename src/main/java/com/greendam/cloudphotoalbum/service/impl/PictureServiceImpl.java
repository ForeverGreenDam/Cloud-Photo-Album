package com.greendam.cloudphotoalbum.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.greendam.cloudphotoalbum.common.AliOssUtil;
import com.greendam.cloudphotoalbum.exception.ErrorCode;
import com.greendam.cloudphotoalbum.exception.ThrowUtils;
import com.greendam.cloudphotoalbum.model.dto.PictureUploadDTO;
import com.greendam.cloudphotoalbum.model.entity.Picture;
import com.greendam.cloudphotoalbum.mapper.PictureMapper;
import com.greendam.cloudphotoalbum.model.vo.PictureVO;
import com.greendam.cloudphotoalbum.service.PictureService ;
import com.greendam.cloudphotoalbum.service.UserService;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    public PictureVO uploadPicture(MultipartFile file, PictureUploadDTO pictureUploadDTO, HttpServletRequest request) {
        //判断是否为更新操作（即已经上传过图片，但是不满意，准备换为别的图片）
        Long pictureId = null;
        if(pictureUploadDTO!=null){pictureId=pictureUploadDTO.getId();}
        //如果pictureId不为空，说明是更新操作, 先查数据库该对象是否存在
        if(pictureId!=null){
            Picture exit = pictureMapper.selectById(pictureId);
            ThrowUtils.throwIf(exit==null,ErrorCode.PARAMS_ERROR);
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
}




