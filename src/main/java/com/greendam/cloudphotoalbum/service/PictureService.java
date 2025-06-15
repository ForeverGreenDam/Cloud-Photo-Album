package com.greendam.cloudphotoalbum.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.greendam.cloudphotoalbum.model.dto.PictureQueryDTO;
import com.greendam.cloudphotoalbum.model.dto.PictureReviewDTO;
import com.greendam.cloudphotoalbum.model.dto.PictureUpdateDTO;
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

    /**
     * 获取图片查询条件
     * @param pictureQueryRequest
     * @return 查询包装器
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryDTO pictureQueryRequest);

    /**
     * 根据图片ID删除图片
     * @param pictureId 图片ID
     * @param request HTTP请求对象，用于获取用户信息等
     */
    void deletePicture(Long pictureId, HttpServletRequest request);

    /**
     * 更新图片信息(管理员)
     * @param pictureUpdateDTO 图片更新数据传输对象
     * @return
     */
    boolean updatePicture(PictureUpdateDTO pictureUpdateDTO);
    /**
     * 获取图片视图对象
     * @param picture 图片实体
     * @return 图片视图对象
     */
    PictureVO getPictureVO(Picture picture);

    /**
     * 图片审核
     * @param pictureReviewDTO 图片审核数据传输对象
     * @param id 审核员id
     */
    void pictureReview(PictureReviewDTO pictureReviewDTO, Long id);
}
