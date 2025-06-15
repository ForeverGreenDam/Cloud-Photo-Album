package com.greendam.cloudphotoalbum.controller.common;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.greendam.cloudphotoalbum.annotation.AuthCheck;
import com.greendam.cloudphotoalbum.common.BaseResponse;
import com.greendam.cloudphotoalbum.common.DeleteRequest;
import com.greendam.cloudphotoalbum.constant.UserConstant;
import com.greendam.cloudphotoalbum.exception.ErrorCode;
import com.greendam.cloudphotoalbum.exception.ThrowUtils;
import com.greendam.cloudphotoalbum.model.dto.PictureQueryDTO;
import com.greendam.cloudphotoalbum.model.dto.PictureReviewDTO;
import com.greendam.cloudphotoalbum.model.dto.PictureUpdateDTO;
import com.greendam.cloudphotoalbum.model.dto.PictureUploadDTO;
import com.greendam.cloudphotoalbum.model.entity.Picture;
import com.greendam.cloudphotoalbum.model.vo.PictureVO;
import com.greendam.cloudphotoalbum.model.vo.UserLoginVO;
import com.greendam.cloudphotoalbum.service.PictureService;
import com.greendam.cloudphotoalbum.service.UserService;
import com.sun.istack.internal.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 图片控制器类，用于处理与图片相关的请求
 * @author ForeverGreenDam
 */
@RestController
@RequestMapping("/picture")
public class PictureController {
    @Resource
    private PictureService pictureService;
    @Autowired
    private UserService userService;

    /**
     * 图片上传服务接口
     * @param file
     * @param pictureUploadDTO
     * @param request
     * @return
     */
    @PostMapping("/upload")
    public BaseResponse<PictureVO> uploadPicture(@RequestParam("file")MultipartFile file,
                                                 PictureUploadDTO pictureUploadDTO,
                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(file.isEmpty(), ErrorCode.PARAMS_ERROR);
      PictureVO VO=  pictureService.uploadPicture(file,pictureUploadDTO,request);
        return BaseResponse.success(VO);
    }
    /**
     * 根据图片ID删除图片
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck
    public BaseResponse deletePicture(@RequestBody DeleteRequest deleteRequest,HttpServletRequest request) {
        // 检查请求参数是否有效
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        Long pictureId = deleteRequest.getId();
        pictureService.deletePicture(pictureId, request);

        return BaseResponse.success();
    }
    /**
     * 更新图片信息（管理员）
     * @param pictureUpdateDTO
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse updatePicture(@RequestBody PictureUpdateDTO pictureUpdateDTO) {
        // 检查请求参数是否有效
        ThrowUtils.throwIf(pictureUpdateDTO == null || pictureUpdateDTO.getId() == null, ErrorCode.PARAMS_ERROR);
        boolean ok = pictureService.updatePicture(pictureUpdateDTO);
        ThrowUtils.throwIf(!ok, ErrorCode.OPERATION_ERROR);
        return BaseResponse.success();
    }
    /**
     * 根据ID获取图片信息(管理员，无需脱敏)
     * @param id 图片ID
     * @return 图片信息
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPictureById(long id){
    // 检查请求参数是否有效
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        return BaseResponse.success(picture);
    }
    /**
     * 根据ID获取图片信息(用户，脱敏)
     * @param id 图片ID
     * @return 图片视图对象
     */
    @GetMapping("/get/vo")
    @AuthCheck
    public BaseResponse<PictureVO> getPictureVOById(long id){
        // 检查请求参数是否有效
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        return BaseResponse.success(pictureService.getPictureVO(picture));
    }

    /**
     * 分页查询图片列表(管理员)
     * @param pictureQueryDTO
     * @return 分页查询结果
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPicturesByPage(@RequestBody @NotNull PictureQueryDTO pictureQueryDTO){
        long current = pictureQueryDTO.getCurrent();
        long pageSize = pictureQueryDTO.getPageSize();
        Page<Picture> page = pictureService.page(new Page<>(current, pageSize),
                pictureService.getQueryWrapper(pictureQueryDTO));
        return BaseResponse.success(page);
    }
    /**
     * 分页查询图片列表(均可使用，脱敏)
     * @param pictureQueryDTO
     * @return 分页查询结果
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody @NotNull PictureQueryDTO pictureQueryDTO) {
        long current = pictureQueryDTO.getCurrent();
        long pageSize = pictureQueryDTO.getPageSize();
        Page<Picture> page = pictureService.page(new Page<>(current, pageSize),
                pictureService.getQueryWrapper(pictureQueryDTO));
        // 将查询结果转换为VO对象
        List<PictureVO> collect = page.getRecords().stream()
                        .map(picture -> pictureService.getPictureVO(picture))
                .collect(Collectors.toList());
        // 创建新的Page对象用于返回
        Page<PictureVO> pictureVOPage = new Page<>();
        BeanUtil.copyProperties(page, pictureVOPage,true);
        pictureVOPage.setRecords(collect);
        return BaseResponse.success(pictureVOPage);
    }
    @PostMapping("/edit")
    @AuthCheck
    public BaseResponse editPicture(@RequestBody PictureUpdateDTO pictureUpdateDTO,HttpServletRequest request) {
        // 检查请求参数是否有效
        ThrowUtils.throwIf(pictureUpdateDTO == null || pictureUpdateDTO.getId() == null, ErrorCode.PARAMS_ERROR);
        //检查图片是否存在
        Picture picture = pictureService.getById(pictureUpdateDTO.getId());
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        // 检查用户是否有权限编辑图片
        UserLoginVO user =userService.getUser(request);
        String userRole= user.getUserRole();
        ThrowUtils.throwIf((!UserConstant.ADMIN_ROLE.equals(userRole))&&
                !picture.getUserId().equals(user.getId()), ErrorCode.NOT_AUTH_ERROR, "无权限编辑该图片");
        // 更新图片信息
        BeanUtil.copyProperties(pictureUpdateDTO, picture);
        picture.setTags(JSONUtil.toJsonStr(picture.getTags()));
        picture.setEditTime(LocalDateTime.now());
        boolean ok = pictureService.updateById(picture);
        ThrowUtils.throwIf(!ok, ErrorCode.OPERATION_ERROR);
        return BaseResponse.success();

    }
    /**
     * 管理员审核接口
     * @param pictureReviewDTO
     * @Param request
     */
    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse reviewPicture(@RequestBody PictureReviewDTO pictureReviewDTO,HttpServletRequest request) {
        ThrowUtils.throwIf(pictureReviewDTO == null || pictureReviewDTO.getId() == null, ErrorCode.PARAMS_ERROR);
        UserLoginVO user =userService.getUser(request);
        pictureService.pictureReview(pictureReviewDTO, user.getId());
        return BaseResponse.success();
    }
}


