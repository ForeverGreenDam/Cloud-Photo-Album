package com.greendam.cloudphotoalbum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.greendam.cloudphotoalbum.model.dto.PictureUploadDTO;
import com.greendam.cloudphotoalbum.model.entity.Picture ;
import com.greendam.cloudphotoalbum.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
* @author ForeverGreenDam
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-06-06 14:26:53
*/
public interface PictureService extends IService<Picture> {
    /**
     * 上传文件
     * @param file 上传的文件
     * @param  pictureUploadDTO 图片上传数据传输对象
     * @param  request HTTP请求对象，用于获取userId等信息
     * @return 图片视图
     */
    PictureVO uploadPicture(MultipartFile file, PictureUploadDTO pictureUploadDTO, HttpServletRequest request);
}
