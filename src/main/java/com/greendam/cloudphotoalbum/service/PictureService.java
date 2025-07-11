package com.greendam.cloudphotoalbum.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.greendam.cloudphotoalbum.model.dto.*;
import com.greendam.cloudphotoalbum.model.entity.Picture;
import com.greendam.cloudphotoalbum.model.vo.CreateOutPaintingTaskResponse;
import com.greendam.cloudphotoalbum.model.vo.GetOutPaintingTaskResponse;
import com.greendam.cloudphotoalbum.model.vo.PictureVO;
import com.greendam.cloudphotoalbum.model.vo.UserLoginVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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
     * 分页查询图片视图对象
     * @param pictureQueryDTO  图片查询数据传输对象
     * @return 分页的图片视图对象列表
     */
    Page<PictureVO> listPictureVOByPage(PictureQueryDTO pictureQueryDTO,HttpServletRequest request);

    /**
     * 图片审核
     * @param pictureReviewDTO 图片审核数据传输对象
     * @param id 审核员id
     */
    void pictureReview(PictureReviewDTO pictureReviewDTO, Long id);

    /**
     * 通过URL上传图片
     * @param fileUrl 图片的URL地址
     * @param pictureUploadDTO 包含图片上传信息的请求体
     * @param loginUser 登录用户信息
     * @return
     */
    PictureVO uploadPictureByUrl(String fileUrl, PictureUploadDTO pictureUploadDTO, UserLoginVO loginUser);

    /**
     * 批量抓取图片
     * @param pictureUploadByBatchDTO 批量抓取图片数据传输对象
     * @param user 登录用户信息
     * @return 成功上传的图片数量
     */
    int uploadPictureBatch(PictureUploadByBatchDTO pictureUploadByBatchDTO, UserLoginVO user);

    /**
     * 清除图片缓存
     */
    void flashAllPictureCache( ) ;
    /**
     * 检查图片的权限
     * @param loginUser 登录用户信息
     * @param picture 图片实体
     */
    void checkPictureAuth(UserLoginVO loginUser, Picture picture);

    /**
     * 根据颜色搜索图片
     * @param searchPictureByColorDTO 颜色搜索数据传输对象，包含颜色信息和分页信息
     * @param user 登录用户信息
     * @return 图片视图对象列表
     */
    List<PictureVO> searchPictureByColor(SearchPictureByColorDTO searchPictureByColorDTO, UserLoginVO user);

    /**
     * 批量编辑图片信息
     * @param pictureEditByBatchDTO 批量编辑图片数据传输对象，包含编辑信息和图片ID列表
     * @param loginUser 登录用户信息
     */
    void editPictureByBatch(PictureEditByBatchDTO pictureEditByBatchDTO, UserLoginVO loginUser);
    /**
     * 创建AI扩图任务
     * @param dto 包含外绘任务信息的请求体
     * @param  loginUser 登录用户信息
     * @return 创建外绘任务的响应对象
     */
    CreateOutPaintingTaskResponse createOutPaintingTask(CreatePictureOutPaintingTaskRequest dto, UserLoginVO loginUser);

    /**
     * 获取AI扩图处理任务结果
     * @param taskId 任务ID
     * @return 获取外绘任务结果的响应对象
     */
    GetOutPaintingTaskResponse getOutPaintingTask(String taskId);
}
