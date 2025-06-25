package com.greendam.cloudphotoalbum.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.greendam.cloudphotoalbum.common.AliOssUtil;
import com.greendam.cloudphotoalbum.common.ColorSimilarUtils;
import com.greendam.cloudphotoalbum.constant.CacheConstant;
import com.greendam.cloudphotoalbum.constant.OssConstant;
import com.greendam.cloudphotoalbum.constant.UserConstant;
import com.greendam.cloudphotoalbum.exception.BusinessException;
import com.greendam.cloudphotoalbum.exception.ErrorCode;
import com.greendam.cloudphotoalbum.exception.ThrowUtils;
import com.greendam.cloudphotoalbum.mapper.PictureMapper;
import com.greendam.cloudphotoalbum.mapper.UserMapper;
import com.greendam.cloudphotoalbum.model.dto.*;
import com.greendam.cloudphotoalbum.model.entity.Picture;
import com.greendam.cloudphotoalbum.model.entity.Space;
import com.greendam.cloudphotoalbum.model.entity.User;
import com.greendam.cloudphotoalbum.model.enums.PictureReviewStatusEnum;
import com.greendam.cloudphotoalbum.model.vo.PictureVO;
import com.greendam.cloudphotoalbum.model.vo.UserLoginVO;
import com.greendam.cloudphotoalbum.model.vo.UserVO;
import com.greendam.cloudphotoalbum.service.PictureService;
import com.greendam.cloudphotoalbum.service.SpaceService;
import com.greendam.cloudphotoalbum.service.UserService;
import com.luciad.imageio.webp.WebPWriteParam;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
    private SpaceService spaceService;
    @Resource
    private PictureMapper pictureMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    private final Cache<String, String> LOCAL_CACHE =
            Caffeine.newBuilder().initialCapacity(1024)
                    .maximumSize(10000L)
                    // 缓存 5 分钟移除
                    .expireAfterWrite(5L, TimeUnit.MINUTES)
                    .build();

    @Override
    public PictureVO uploadPicture(MultipartFile file, PictureUploadDTO pictureUploadDTO, HttpServletRequest request) {
        //参数校验
        ThrowUtils.throwIf(pictureUploadDTO==null, ErrorCode.PARAMS_ERROR, "图片上传参数不能为空");
        UserLoginVO user = userService.getUser(request);
        //校验是否是空间内操作
        Long spaceId = pictureUploadDTO.getSpaceId();
        if(spaceId!=null){
            //校验是否有权限对空间操作
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space==null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            Long spaceUserId = space.getUserId();
            //仅限空间所有者操作
            ThrowUtils.throwIf(!spaceUserId.equals(user.getId()),ErrorCode.NOT_AUTH_ERROR, "无权限对该空间操作");
            //检查空间容量
            ThrowUtils.throwIf(space.getMaxSize()<=space.getTotalSize(),ErrorCode.OPERATION_ERROR,"空间容量已满");
            ThrowUtils.throwIf(space.getMaxCount()<=space.getTotalCount(),ErrorCode.OPERATION_ERROR,"空间条目已满");
        }
        //判断是否为更新操作（即已经上传过图片，但是不满意，准备换为别的图片）
        Long pictureId;
        pictureId=pictureUploadDTO.getId();
        //如果pictureId不为空，说明是更新操作, 先查数据库该对象是否存在
        if(pictureId!=null){
            Picture exit = pictureMapper.selectById(pictureId);
            ThrowUtils.throwIf(exit==null,ErrorCode.PARAMS_ERROR);
            //如果存在，检查用户是否有权限更新该图片
            ThrowUtils.throwIf(!exit.getUserId().equals(user.getId()) &&
                    !UserConstant.ADMIN_ROLE.equals(user.getUserRole()),
                    ErrorCode.NOT_AUTH_ERROR, "无权限更新该图片");
            //更新操作时，防止传入空的spaceId导致私有图片传到公共图库
            spaceId=exit.getSpaceId();
        }
        String originalFilename = file.getOriginalFilename();
        // 获取文件后缀
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        // 生成唯一文件名
        String newFileName = UUID.randomUUID() + extension;
        File pictureFile=null;
        try {
            //将MultipartFile转换为File,供ImageIO读取
            pictureFile=File.createTempFile(newFileName,extension);
            FileCopyUtils.copy(file.getBytes(),pictureFile);
            //将图片转换为WebP格式
            boolean webp = toWebp(pictureFile);
            //如果转换成功，则更改文件名和后缀
            if(webp){
                extension = ".webp";
                newFileName = newFileName.substring(0, newFileName.lastIndexOf(".")) + extension;
            }
            //上传文件获得url
            String url = aliOssUtil.upload(Files.readAllBytes(pictureFile.toPath()), newFileName);
            //创建图片对象
            Picture picture = new Picture();
            //设置基本信息
            picture.setUrl(url);
            picture.setName(originalFilename);
            picture.setPicFormat(extension);
            picture.setUserId(user.getId());
            picture.setSpaceId(spaceId);
            picture.setPicColor(getPicColor(url));
            //图片解析
            BufferedImage image = ImageIO.read(pictureFile);
            picture.setPicSize(pictureFile.length());
            picture.setPicWidth(image.getWidth());
            picture.setPicHeight(image.getHeight());
            picture.setPicScale((double)image.getWidth()/ (double) image.getHeight());
            picture.setThumbnailUrl(url+ OssConstant.THUMBNAIL);
            //如果是管理员上传，直接通过审核
            if(UserConstant.ADMIN_ROLE.equals(user.getUserRole()))
            {
                picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
                picture.setReviewerId(user.getId());
                picture.setReviewMessage("管理员上传，自动通过审核");
                picture.setReviewTime(LocalDateTime.now());
            }
            //满足lambda表达式需求
            Long finalSpaceId = spaceId;
            transactionTemplate.execute(status -> {
                if(pictureId!=null){
                    //更新操作，需要手动设置图片id以及更新时间
                    picture.setId(pictureId);
                    picture.setEditTime(LocalDateTime.now());
                    pictureMapper.updateById(picture);
                }else{
                    //新建操作，直接插入
                    pictureMapper.insert(picture);
                }
                //如果是空间内操作，更新空间的总大小和总数量
                if(finalSpaceId !=null){
                    Space space = spaceService.getById(finalSpaceId);
                    space.setTotalCount(space.getTotalCount()+1);
                    space.setTotalSize(space.getTotalSize()+file.getSize());
                    boolean updated = spaceService.updateById(space);
                    ThrowUtils.throwIf(!updated,ErrorCode.OPERATION_ERROR);
                }
                return null;
            });

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
        Long spaceId = pictureQueryRequest.getSpaceId();
        boolean nullSpaceId = pictureQueryRequest.isNullSpaceId();
        Date startEditTime = pictureQueryRequest.getStartEditTime();
        Date endEditTime = pictureQueryRequest.getEndEditTime();

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
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "spaceId", spaceId);
        queryWrapper.isNull(nullSpaceId, "spaceId");
        queryWrapper.ge(ObjUtil.isNotEmpty(startEditTime), "editTime", startEditTime);
        queryWrapper.lt(ObjUtil.isNotEmpty(endEditTime), "editTime", endEditTime);

        // JSON 数组查询
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), "ascend".equals(sortOrder), sortField);
        return queryWrapper;
    }

    @Override
    public void deletePicture(Long pictureId, HttpServletRequest request) {
        UserLoginVO user=userService.getUser(request);
        Picture picture = pictureMapper.selectById(pictureId);
        //如果图片不存在，抛出异常
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR, "图片不存在");
        //验证操作权限
        checkPictureAuth(user,picture);
        Long spaceId = picture.getSpaceId();
        transactionTemplate.execute(status -> {
            if(spaceId == null){return null;}
            else{
                Space space = spaceService.getById(spaceId);
                space.setTotalSize(space.getTotalSize()-picture.getPicSize());
                space.setTotalCount(space.getTotalCount()-1);
                spaceService.updateById(space);
                //删除图片
                pictureMapper.deleteById(pictureId);
            }
            return null;
        });
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
    public Page<PictureVO> listPictureVOByPage(PictureQueryDTO pictureQueryDTO,HttpServletRequest request) {
        long current = pictureQueryDTO.getCurrent();
        long pageSize = pictureQueryDTO.getPageSize();
        //权限控制
        Long spaceId=pictureQueryDTO.getSpaceId();
        if(spaceId==null){
            //公共图库,仅查询审核通过的图片
            pictureQueryDTO.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            pictureQueryDTO.setNullSpaceId(true);
        }else{
            //私人空间，审核不受限
            UserLoginVO user = userService.getUser(request);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space==null,ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            ThrowUtils.throwIf(!space.getUserId().equals(user.getId()),ErrorCode.NOT_AUTH_ERROR,"无权限访问该空间");
        }
        //先查询本地缓存
        String localKey = CacheConstant.CAFFEINE_PICTURE_KEY+ DigestUtils.md5DigestAsHex(JSONUtil.toJsonStr(pictureQueryDTO).getBytes());
        String localValue = LOCAL_CACHE.getIfPresent(localKey);
        if (localValue != null) {
            // 如果本地缓存存在，直接返回
            return JSONUtil.toBean(localValue, Page.class);
        }
        // 如果本地缓存不存在，则查询Redis缓存
        String redisKey = CacheConstant.REDIS_PICTURE_KEY + DigestUtils.md5DigestAsHex(JSONUtil.toJsonStr(pictureQueryDTO).getBytes());
        String redisValue = stringRedisTemplate.opsForValue().get(redisKey);
        if (redisValue != null) {
            // 如果Redis缓存存在，返回，并更新本地缓存，重置Redis缓存的过期时间
            Page<PictureVO> cachedPage = JSONUtil.toBean(redisValue, Page.class);
            LOCAL_CACHE.put(localKey, JSONUtil.toJsonStr(cachedPage));
            stringRedisTemplate.opsForValue().set(redisKey, JSONUtil.toJsonStr(cachedPage),
                    CacheConstant.PICTURE_EXPIRE + RandomUtil.randomInt(0,300), TimeUnit.SECONDS);
            return cachedPage;
        }
        // 如果Redis缓存也不存在，则进行数据库查询
        Page<Picture> page = this.page(new Page<>(current, pageSize),
                this.getQueryWrapper(pictureQueryDTO));
        // 将查询结果转换为VO对象
        List<PictureVO> collect = page.getRecords().stream()
                .map(this::getPictureVO)
                .collect(Collectors.toList());
        // 创建新的Page对象用于返回
        Page<PictureVO> pictureVOPage = new Page<>();
        BeanUtil.copyProperties(page, pictureVOPage,true);
        pictureVOPage.setRecords(collect);
        // 将结果存入本地缓存和Redis缓存(Redis记得设置过期时间)
        LOCAL_CACHE.put(localKey, JSONUtil.toJsonStr(pictureVOPage));
        stringRedisTemplate.opsForValue().set(redisKey, JSONUtil.toJsonStr(pictureVOPage),
                CacheConstant.PICTURE_EXPIRE + RandomUtil.randomInt(0,300), TimeUnit.SECONDS);
        return pictureVOPage;
    }

    @Override
    public void pictureReview(PictureReviewDTO pictureReviewDTO, Long id) {
        //1.获取旧图片
        Picture oldPicture = pictureMapper.selectById(pictureReviewDTO.getId());
        ThrowUtils.throwIf(oldPicture==null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        //2.检查审核状态
        Integer reviewStatus = oldPicture.getReviewStatus();
        PictureReviewStatusEnum statusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
        ThrowUtils.throwIf(statusEnum == null, ErrorCode.PARAMS_ERROR, "非法数据");
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
        Long pictureId;
        Long spaceId = null;
        if(pictureUploadDTO!=null){
            pictureId=pictureUploadDTO.getId();
            spaceId=pictureUploadDTO.getSpaceId();} else {
            pictureId = null;
        }
        if(spaceId!=null){
            //校验是否有权限对空间操作
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space==null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            Long spaceUserId = space.getUserId();
            //仅限空间所有者操作
            ThrowUtils.throwIf(!spaceUserId.equals(loginUser.getId()),ErrorCode.NOT_AUTH_ERROR, "无权限对该空间操作");
            //检查空间容量
            ThrowUtils.throwIf(space.getMaxSize()<=space.getTotalSize(),ErrorCode.OPERATION_ERROR,"空间容量已满");
            ThrowUtils.throwIf(space.getMaxCount()<=space.getTotalCount(),ErrorCode.OPERATION_ERROR,"空间条目已满");
        }
        //如果pictureId不为空，说明是更新操作, 先查数据库该对象是否存在
        if(pictureId!=null){
            Picture exit = pictureMapper.selectById(pictureId);
            ThrowUtils.throwIf(exit==null,ErrorCode.PARAMS_ERROR);
            //如果存在，检查用户是否有权限更新该图片
            ThrowUtils.throwIf(!exit.getUserId().equals(loginUser.getId()) &&
                            !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole()),
                    ErrorCode.NOT_AUTH_ERROR, "无权限更新该图片");
            spaceId=exit.getSpaceId();
        }
        //验证图片URL的合法性,同时获取文件后缀
        String extension=validPicture(fileUrl);
        ThrowUtils.throwIf(extension==null, ErrorCode.PARAMS_ERROR, "不支持HEAD请求的图片地址");
        File file=null;
        try {
        //从URL获取文件
        String uuid = UUID.randomUUID().toString();
        file=File.createTempFile(uuid,extension);
        HttpUtil.downloadFile(fileUrl, file);
        //转为webp格式
            boolean webp = toWebp(file);
            if(webp){
                extension = "webp";
            }
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
            picture.setSpaceId(spaceId);
            picture.setPicColor(getPicColor(url));
            //图片解析
            BufferedImage image = ImageIO.read(file);
            picture.setPicSize(file.length());
            picture.setPicWidth(image.getWidth());
            picture.setPicHeight(image.getHeight());
            picture.setPicScale((double)image.getWidth()/ (double) image.getHeight());
            picture.setThumbnailUrl(url+ OssConstant.THUMBNAIL);
            //如果是管理员上传，直接通过审核
            if(UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole()))
            {
                picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
                picture.setReviewerId(loginUser.getId());
                picture.setReviewMessage("管理员批量上传，自动通过审核");
                picture.setReviewTime(LocalDateTime.now());
            }
            //如果是批量抓图，则图片名称重置
            if(pictureUploadDTO!=null&&pictureUploadDTO.getPicName()!=null){
                picture.setName(pictureUploadDTO.getPicName());
            }
            //满足lambda表达式需求
            Long finalSpaceId = spaceId;
            File finalFile = file;
            transactionTemplate.execute(status -> {
                if(pictureId!=null){
                    //更新操作，需要手动设置图片id以及更新时间
                    picture.setId(pictureId);
                    picture.setEditTime(LocalDateTime.now());
                    pictureMapper.updateById(picture);
                }else{
                    //新建操作，直接插入
                    pictureMapper.insert(picture);
                }
                //如果是空间内操作，更新空间的总大小和总数量
                if(finalSpaceId !=null){
                    Space space = spaceService.getById(finalSpaceId);
                    space.setTotalCount(space.getTotalCount()+1);
                    space.setTotalSize(space.getTotalSize()+ finalFile.length());
                    boolean updated = spaceService.updateById(space);
                    ThrowUtils.throwIf(!updated,ErrorCode.OPERATION_ERROR);
                }
                return null;
            });
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

    @Override
    public int uploadPictureBatch(PictureUploadByBatchDTO pictureUploadByBatchDTO, UserLoginVO user) {
        //获取请求参数
        String searchText = pictureUploadByBatchDTO.getSearchText();
        ThrowUtils.throwIf(StrUtil.isBlank(searchText), ErrorCode.PARAMS_ERROR, "抓取关键词不能为空");
        Integer count = pictureUploadByBatchDTO.getCount();
        ThrowUtils.throwIf(count > 30, ErrorCode.PARAMS_ERROR," 抓取图片数量不能超过30");
        //使用jsoup抓取图片
        String url="https://cn.bing.com/images/async?q="+searchText+"&mmasync=1";
        Document doc;
        try {
            // 发送 GET 请求获取HTML文档
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"获取页面失败");
        }
        Element div = doc.getElementsByClass("dgControl").first();
        ThrowUtils.throwIf(div==null, ErrorCode.OPERATION_ERROR, "获取元素失败");
        Elements imgElementList = div.select("img.mimg");
        int successCount = 1;
        for (Element element : imgElementList) {
            String src = element.attr("src");
            if (StrUtil.isBlank(src)) {
                // 如果没有src属性，跳过
                log.info("当前连接为空，已跳过");
                continue;
            }
            //处理地址，剪切额外参数
            int index = src.indexOf("?");
            if(index>-1){
                src=src.substring(0,index);
            }
            try {
                //上传图片, 设置图片名称
                PictureUploadDTO pictureUploadDTO = new PictureUploadDTO();
                pictureUploadDTO.setPicName(searchText+successCount);
                PictureVO pictureVO = uploadPictureByUrl(src, pictureUploadDTO, user);
                log.info("上传图片成功，图片id：{}",pictureVO.getId());
                successCount++;
            } catch (Exception e) {
               log.info("图片上传失败：{}",e.getMessage());
               continue;
            }
            //达到指定数量就退出循环
            if(successCount>count){break;}
        }
        return successCount-1;
    }

    @Override
    public List<PictureVO> searchPictureByColor(SearchPictureByColorDTO searchPictureByColorDTO, UserLoginVO user) {
        String picColor = searchPictureByColorDTO.getPicColor();
        Long spaceId = searchPictureByColorDTO.getSpaceId();
        ThrowUtils.throwIf(StrUtil.isBlank(picColor)||spaceId==null, ErrorCode.PARAMS_ERROR);
        //鉴权
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space==null, ErrorCode.OPERATION_ERROR, "空间不存在");
        ThrowUtils.throwIf(!space.getUserId().equals(user.getId()), ErrorCode.NOT_AUTH_ERROR, "无权限访问该空间");
        //查询出该空间所有的图片
        List<Picture> pictures = pictureMapper.selectList(new QueryWrapper<Picture>().eq("spaceId", spaceId));
        //保留相似度大于0.8的前十张图片
        List<Picture> collect = pictures.stream().sorted(Comparator.comparingDouble(picture -> {
            String color = picture.getPicColor();
            // 计算颜色相似度
            log.info("当前图片名称：{}，图片主色调：{}，搜索主色调：{}，当前相似度：{}",
                    picture.getName(), color, picColor, ColorSimilarUtils.calculateSimilarity(picColor, color));
            return -ColorSimilarUtils.calculateSimilarity(picColor, color);
        })).filter(picture -> {
            // 过滤出相似度大于0.8的图片
            String color = picture.getPicColor();
            return ColorSimilarUtils.calculateSimilarity(picColor, color) > 0.8;
        }).limit(10).collect(Collectors.toList());
        //脱敏
        List<PictureVO> pictureVOList = collect.stream()
                .map(this::getPictureVO)
                .collect(Collectors.toList());
        return pictureVOList;
    }

    @Override
    public void flashAllPictureCache() {
        // 清除本地缓存
        LOCAL_CACHE.invalidateAll();
        // 清除Redis缓存
        stringRedisTemplate.delete(stringRedisTemplate.keys(CacheConstant.REDIS_PICTURE_KEY + "*"));
        log.info("图片缓存已清除");
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
    /**
     * 将图片转换为WebP格式
     * @param file 图片文件
     */
    private boolean toWebp(File file) {
       File webpFile = null;
        try {
            BufferedImage image = ImageIO.read(file);
            ImageWriter writer = ImageIO.getImageWritersByMIMEType("image/webp").next();
            WebPWriteParam writeParam = new WebPWriteParam(writer.getLocale());
            writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            // 设置有损压缩
            writeParam.setCompressionType(writeParam.getCompressionTypes()[WebPWriteParam.LOSSY_COMPRESSION]);
            //设置 80% 的质量. 设置范围 0-1
            writeParam.setCompressionQuality(0.8f);
            // Save the image
            webpFile = File.createTempFile(UUID.randomUUID().toString(), ".webp");
            FileImageOutputStream output = new FileImageOutputStream(webpFile);
            writer.setOutput(output);
            writer.write(null, new IIOImage(image, null, null), writeParam);
            // 转换成功，则将原始文件替换为WebP文件(转换失败的话会直接报错，不会执行下面的语句)
           FileCopyUtils.copy(webpFile, file);
           output.close();
            return true;
        } catch (IOException e) {
           log.info("转换图片格式失败，使用原始格式上传：{}", e.getMessage());
           return false;
        }finally {
            // 删除临时文件
            if (webpFile != null && webpFile.exists()) {
                boolean delete = webpFile.delete();
                if (!delete) {
                    log.error("临时文件删除失败，路径：{}", webpFile.getAbsolutePath());
                }
            }
        }
    }
    /**
     * 获取图片主色调
     * @param fileUrl 图片的URL地址
     * @return 主色调
     */
    private String getPicColor(String fileUrl) {
        String url= fileUrl+ OssConstant.AVERAGE_HUE;
        HttpResponse execute = HttpUtil.createGet(url).execute();
        if (execute.getStatus() != HttpStatus.HTTP_OK) {throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取图片主色调失败");}
        String body= execute.body();
        if (StrUtil.isBlank(body)) {throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取图片主色调失败");}
        // 解析JSON
        Map RGB = JSONUtil.toBean(body, Map.class);
        // 获取RGB值
        return RGB.get("RGB").toString();
    }
    /**
     * 检查图片查看权限
     */
    @Override
    public void checkPictureAuth(UserLoginVO loginUser, Picture picture) {
        Long spaceId = picture.getSpaceId();
        if (spaceId == null) {
            // 公共图库，仅本人或管理员可操作
            if (!picture.getUserId().equals(loginUser.getId()) && UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
                throw new BusinessException(ErrorCode.NOT_AUTH_ERROR);
            }
        } else {
            // 私有空间，仅空间管理员可操作
            if (!picture.getUserId().equals(loginUser.getId())) {
                throw new BusinessException(ErrorCode.NOT_AUTH_ERROR);
            }
        }
    }
    /**
     *定时任务：每月十五号夜间删除逻辑删除的图片
     */
    @Async
    @Scheduled(cron = "59 59 23 15 * ? ")
    public void cleanDataBase(){
        //首先查询数据库，找出所有逻辑删除的图片
        List<String> urls = pictureMapper.selectDeleteUrls();
        //然后调用阿里云OSS删除图片
        aliOssUtil.delete(urls);
        //将逻辑删除的图片删除状态转为物理删除
        pictureMapper.updateDeleteStatus();
    }
}




