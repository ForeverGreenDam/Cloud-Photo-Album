package com.greendam.cloudphotoalbum.controller.common;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.greendam.cloudphotoalbum.annotation.AuthCheck;
import com.greendam.cloudphotoalbum.common.BaseResponse;
import com.greendam.cloudphotoalbum.common.DeleteRequest;
import com.greendam.cloudphotoalbum.common.auth.SpaceUserAuthManager;
import com.greendam.cloudphotoalbum.common.auth.StpKit;
import com.greendam.cloudphotoalbum.common.auth.annotation.SaSpaceCheckPermission;
import com.greendam.cloudphotoalbum.common.utils.ImageSearchUtils;
import com.greendam.cloudphotoalbum.common.utils.ThrowUtils;
import com.greendam.cloudphotoalbum.constant.SpaceUserPermissionConstant;
import com.greendam.cloudphotoalbum.constant.UserConstant;
import com.greendam.cloudphotoalbum.exception.ErrorCode;
import com.greendam.cloudphotoalbum.model.dto.*;
import com.greendam.cloudphotoalbum.model.entity.Picture;
import com.greendam.cloudphotoalbum.model.entity.Space;
import com.greendam.cloudphotoalbum.model.enums.PictureReviewStatusEnum;
import com.greendam.cloudphotoalbum.model.vo.*;
import com.greendam.cloudphotoalbum.service.PictureService;
import com.greendam.cloudphotoalbum.service.SpaceService;
import com.greendam.cloudphotoalbum.service.UserService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;
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
    @Resource
    private UserService userService;
    @Resource
    private SpaceUserAuthManager  spaceUserAuthManager;
    @Resource
    private SpaceService spaceService;

    /**
     * 图片上传服务接口
     * @param file 上传的图片文件
     * @param pictureUploadDTO 包含图片上传信息的数据传输对象
     * @param request HTTP请求对象，用于获取用户信息等
     * @return 包含上传后图片信息的响应对象
     */
    @PostMapping("/upload")
    @SaSpaceCheckPermission(SpaceUserPermissionConstant.PICTURE_UPLOAD)
    public BaseResponse<PictureVO> uploadPicture(@RequestParam("file")MultipartFile file,
                                                  PictureUploadDTO pictureUploadDTO,
                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(file.isEmpty(), ErrorCode.PARAMS_ERROR);
      PictureVO VO=  pictureService.uploadPicture(file,pictureUploadDTO,request);
      //清除缓存
       pictureService.flashAllPictureCache();
        return BaseResponse.success(VO);
    }
    /**
     * 通过 URL 上传图片
     * @param pictureUploadRequest 包含图片 URL 和其他上传信息的请求体
     * @param request HTTP 请求对象，用于获取用户信息
     */
    @PostMapping("/upload/url")
    @SaSpaceCheckPermission(SpaceUserPermissionConstant.PICTURE_UPLOAD)
    public BaseResponse<PictureVO> uploadPictureByUrl(
            @RequestBody PictureUploadDTO pictureUploadRequest,
            HttpServletRequest request) {
        UserLoginVO loginUser = userService.getUser(request);
        String fileUrl = pictureUploadRequest.getFileUrl();
        PictureVO pictureVO = pictureService.uploadPictureByUrl(fileUrl, pictureUploadRequest, loginUser);
        //清除缓存
        pictureService.flashAllPictureCache();
        return BaseResponse.success(pictureVO);
    }

    /**
     * 根据图片ID删除图片
     * @param deleteRequest
     * @param request
     */
    @PostMapping("/delete")
    @SaSpaceCheckPermission(SpaceUserPermissionConstant.PICTURE_DELETE)
    public BaseResponse deletePicture(@RequestBody DeleteRequest deleteRequest,HttpServletRequest request) {
        // 检查请求参数是否有效
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        Long pictureId = deleteRequest.getId();
        pictureService.deletePicture(pictureId, request);
        //清除缓存
        pictureService.flashAllPictureCache();
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
        //清除缓存
        pictureService.flashAllPictureCache();
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
    public BaseResponse<PictureVO> getPictureVOById(long id,HttpServletRequest request){
        // 检查请求参数是否有效
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        //检查图片权限
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        UserLoginVO user = userService.getUser(request);
        UserVO userVO = BeanUtil.copyProperties(user,UserVO.class);
        PictureVO pictureVO = pictureService.getPictureVO(picture);
        // 检查图片审核状态(公共图库)
        if(picture.getSpaceId() == null){
            ThrowUtils.throwIf(PictureReviewStatusEnum.PASS.getValue()!=picture.getReviewStatus(),ErrorCode.FORBIDDEN_ERROR,"图片未审核");
            //填充权限
            List<String> permissionList = spaceUserAuthManager.getPermissionList(null, userVO);
            pictureVO.setPermissionList(permissionList);
        }else{
            //如果是空间的图片，则检查用户权限
            boolean hasPermission= StpKit.SPACE.hasPermission(SpaceUserPermissionConstant.PICTURE_VIEW);
            ThrowUtils.throwIf(!hasPermission, ErrorCode.NOT_AUTH_ERROR);
            //填充权限
            Space space = spaceService.getById(picture.getSpaceId());
            List<String> permissionList = spaceUserAuthManager.getPermissionList(space, userVO);
            pictureVO.setPermissionList(permissionList);
        }
        return BaseResponse.success(pictureVO);
    }

    /**
     * 分页查询图片列表(管理员)
     * @param pictureQueryDTO
     * @return 分页查询结果
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPicturesByPage(@RequestBody  PictureQueryDTO pictureQueryDTO){
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
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryDTO pictureQueryDTO,HttpServletRequest request) {
        ThrowUtils.throwIf(pictureQueryDTO == null, ErrorCode.PARAMS_ERROR);
        return BaseResponse.success(pictureService.listPictureVOByPage(pictureQueryDTO,request));
    }
    /**
     * 编辑图片信息（用户）
     * @param pictureUpdateDTO
     * @param request
     */
    @PostMapping("/edit")
    @SaSpaceCheckPermission(SpaceUserPermissionConstant.PICTURE_EDIT)
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
        //补充：单独处理tags字段，将其转换为JSON字符串
        List<String> tags = pictureUpdateDTO.getTags();
        picture.setTags(JSONUtil.toJsonStr(tags));
        picture.setEditTime(LocalDateTime.now());
        //设置图片审核状态(如果是管理员使用则自动过审，否则设置为审核中)
        if (UserConstant.ADMIN_ROLE.equals(userRole)) {
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewerId(user.getId());
            picture.setReviewTime(LocalDateTime.now());
            picture.setReviewMessage("管理员自动通过");
        } else {
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
        boolean ok = pictureService.updateById(picture);
        ThrowUtils.throwIf(!ok, ErrorCode.OPERATION_ERROR);
        //清除缓存
        pictureService.flashAllPictureCache();
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
        //清除缓存
        pictureService.flashAllPictureCache();
        return BaseResponse.success();
    }
    /**
     * 批量抓取图片接口
     * @param pictureUploadByBatchDTO
     * @param request
     * @return 成功上传的图片数量
     */
    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> uploadPictureBatch(@RequestBody PictureUploadByBatchDTO pictureUploadByBatchDTO, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureUploadByBatchDTO==null, ErrorCode.PARAMS_ERROR);
        UserLoginVO user =userService.getUser(request);
        int uploadCount = pictureService.uploadPictureBatch(pictureUploadByBatchDTO, user);
        //清除缓存
        pictureService.flashAllPictureCache();
        return BaseResponse.success(uploadCount);
    }
  /**
     * 获取图片标签和分类信息
     * @return 包含标签和分类的 PictureTagCategory 对象
     */
    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> tagsAndCategory(){
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        String[] categories = {"风景", "人物", "动物", "生活", "美食", "艺术", "科技", "运动", "自然", "游戏","二次元"};
        String[] tags={"GalGame","射击","放松","恐怖","影视","动漫","校园","life"};
        pictureTagCategory.setCategoryList(Arrays.stream(categories).collect(Collectors.toList()));
        pictureTagCategory.setTagList(Arrays.stream(tags).collect(Collectors.toList()));
        return BaseResponse.success(pictureTagCategory);
    }

    /**
     * 按照颜色搜图（私人空间）
     * @param searchPictureByColorDTO 颜色搜索数据传输对象，包含颜色信息和分页信息
     * @param request HTTP 请求对象，用于获取用户信息
     * @return 包含符合颜色条件的图片视图对象列表
     */
    @PostMapping("/search/color")
    @SaSpaceCheckPermission(SpaceUserPermissionConstant.PICTURE_VIEW)
    public BaseResponse<List<PictureVO>> searchPictureByColor(@RequestBody SearchPictureByColorDTO searchPictureByColorDTO, HttpServletRequest request) {
        ThrowUtils.throwIf(searchPictureByColorDTO == null || searchPictureByColorDTO.getPicColor() == null, ErrorCode.PARAMS_ERROR);
        UserLoginVO user = userService.getUser(request);
        List<PictureVO> pictureVOList = pictureService.searchPictureByColor(searchPictureByColorDTO, user);
        return BaseResponse.success(pictureVOList);
    }

    /**
     * 批量编辑图片（私人空间）
     * @param pictureEditByBatchDTO 批量编辑图片数据传输对象，包含编辑信息和图片ID列表
     * @param request HTTP 请求对象，用于获取用户信息
     * @return 编辑是否成功
     */
    @PostMapping("/edit/batch")
    @SaSpaceCheckPermission(SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<Boolean> editPictureByBatch(@RequestBody PictureEditByBatchDTO pictureEditByBatchDTO, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureEditByBatchDTO == null, ErrorCode.PARAMS_ERROR);
        UserLoginVO loginUser = userService.getUser(request);
        pictureService.editPictureByBatch(pictureEditByBatchDTO, loginUser);
        //清除缓存
        pictureService.flashAllPictureCache();
        return BaseResponse.success(true);
    }

    /**
     * 以图搜图接口（仅限在私人空间使用）
     * @param searchPictureByPictureRequest 包含要搜索的图片ID
     * @return 包含搜索结果的图片视图对象列表
     */
    @PostMapping("/search/picture")
    @SaSpaceCheckPermission(SpaceUserPermissionConstant.PICTURE_VIEW)
    public BaseResponse<List<ImageSearchVO>> searchPictureByPicture(@RequestBody SearchPictureByPictureDTO searchPictureByPictureRequest) {
        ThrowUtils.throwIf(searchPictureByPictureRequest == null, ErrorCode.PARAMS_ERROR);
        Long pictureId = searchPictureByPictureRequest.getPictureId();
        ThrowUtils.throwIf(pictureId == null || pictureId <= 0, ErrorCode.PARAMS_ERROR);
        Picture oldPicture = pictureService.getById(pictureId);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        List<ImageSearchVO> resultList = ImageSearchUtils.searchImage(oldPicture.getUrl());
        return BaseResponse.success(resultList);
    }

    /**
     * 创建AI扩图任务接口
     * @param dto 包含外绘任务信息的请求体
     * @param request 用于获取当前登录用户
     * @return 任务响应
     */
    @PostMapping("/out_painting/create_task")
    @SaSpaceCheckPermission(SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<CreateOutPaintingTaskResponse> createOutPaintingTaskResponseBaseResponse(
            @RequestBody CreatePictureOutPaintingTaskRequest dto,HttpServletRequest request) {
        ThrowUtils.throwIf(dto == null||dto.getPictureId()==null, ErrorCode.PARAMS_ERROR);
        UserLoginVO user = userService.getUser(request);
        CreateOutPaintingTaskResponse response= pictureService.createOutPaintingTask(dto, user);
        return BaseResponse.success(response);
    }

    @GetMapping("/out_painting/get_task")
    public BaseResponse<GetOutPaintingTaskResponse> getOutPaintingTaskResponseBaseResponse(String taskId){
        ThrowUtils.throwIf(taskId == null, ErrorCode.PARAMS_ERROR);
        GetOutPaintingTaskResponse response = pictureService.getOutPaintingTask(taskId);
        return BaseResponse.success(response);
    }

}


