package com.greendam.cloudphotoalbum.common.auth;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.json.JSONUtil;
import com.greendam.cloudphotoalbum.common.auth.model.SpaceUserAuthContext;
import com.greendam.cloudphotoalbum.constant.SpaceUserPermissionConstant;
import com.greendam.cloudphotoalbum.constant.UserConstant;
import com.greendam.cloudphotoalbum.exception.BusinessException;
import com.greendam.cloudphotoalbum.exception.ErrorCode;
import com.greendam.cloudphotoalbum.model.entity.Picture;
import com.greendam.cloudphotoalbum.model.entity.Space;
import com.greendam.cloudphotoalbum.model.entity.SpaceUser;
import com.greendam.cloudphotoalbum.model.entity.User;
import com.greendam.cloudphotoalbum.model.enums.SpaceRoleEnum;
import com.greendam.cloudphotoalbum.model.enums.SpaceTypeEnum;
import com.greendam.cloudphotoalbum.service.PictureService;
import com.greendam.cloudphotoalbum.service.SpaceService;
import com.greendam.cloudphotoalbum.service.SpaceUserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static com.greendam.cloudphotoalbum.constant.UserConstant.USER_LOGIN_STATE;

/**
 * StpInterface 实现类，提供权限列表
 * @author ForeverGreenDam
 */
@Component
public class StpInterfaceImpl implements StpInterface {
    @Resource
    SpaceUserAuthManager userAuthManager;
    @Resource
    SpaceUserService spaceUserService;
    @Resource
    SpaceService spaceService;
    @Resource
    PictureService pictureService;
    /**
     * 获取权限列表
     * @param id 各种类型的ID
     * @param loginType 登录类型
     * @return 权限列表
     */
    @Override
    public List<String> getPermissionList(Object id, String loginType) {
        if(!StpKit.SPACE_TYPE.equals(loginType)){
            return Collections.emptyList();
        }
        //管理员权限单独列出，后续会经常用到
        List<String> ADMIN_PERMISSION = userAuthManager.getPermissionsByRole(SpaceRoleEnum.ADMIN.getValue());
        User user =(User) StpKit.SPACE.getSessionByLoginId(id).get(USER_LOGIN_STATE);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录或会话已过期");
        }
        // 如果用户为管理员，返回管理员权限
        if(Objects.equals(user.getUserRole(), SpaceRoleEnum.ADMIN.getValue())){
            return ADMIN_PERMISSION;
        }
        // 获取请求中的上下文对象
        SpaceUserAuthContext authContext = getAuthContextByRequest();
        // 如果上下文对象为空或所有字段都为空，返回管理员权限
        if(isAllFieldsNull(authContext)){
            return ADMIN_PERMISSION;
        }
        // 如果上下文对象中有空间用户信息，获取该用户的权限列表
        if(authContext.getSpaceUser()!=null){
            return userAuthManager.getPermissionsByRole(authContext.getSpaceUser().getSpaceRole());
        }
        // 如果上下文对象中有空间用户 ID，操作的一定是团队空间
        if(authContext.getSpaceUserId()!=null){
            SpaceUser spaceUser = spaceUserService.getById(authContext.getSpaceUserId());
            if(spaceUser ==null){
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"空间用户不存在");
            }
            if(!user.getId().equals(spaceUser.getUserId())){
                // 如果请求中的用户 ID 与空间用户的用户 ID 不匹配，返回空权限
                return Collections.emptyList();
            }
            return userAuthManager.getPermissionsByRole(spaceUser.getSpaceRole());
        }
        // 如果没有空间用户信息或空间用户 ID，则通过图片 ID 或空间 ID 推断空间用户信息
        Long spaceId=authContext.getSpaceId();
        Long pictureId = authContext.getPictureId();
        if(pictureId !=null){
            Picture picture = pictureService.getById(pictureId);
            if (picture == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            }
            spaceId = picture.getSpaceId();
            if (spaceId == null) {
                //公共图库的图片，检查是否为本人
                if (Objects.equals(user.getId(), picture.getUserId()) || Objects.equals(user.getUserRole(), SpaceRoleEnum.ADMIN.getValue())) {
                    return ADMIN_PERMISSION;
                } else {
                    // 其他用户仅查看
                    return Collections.singletonList(SpaceUserPermissionConstant.PICTURE_VIEW);
                }
            }
        }
        //先写验证pictureId，为了复用下面的验证spaceId的逻辑
        if(spaceId!=null){
            Space space = spaceService.getById(spaceId);
            System.out.println("获取空间信息：" + space);
            if(space == null){
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"空间不存在");
            }
            Integer spaceType = space.getSpaceType();
            System.out.println("获取空间类型：" + spaceType);
            if (spaceType == SpaceTypeEnum.PRIVATE.getValue()){
                //私有空间，本人和管理员返回管理员权限
                if (user.getId().equals(space.getUserId()) || UserConstant.ADMIN_ROLE.equals(user.getUserRole())) {
                    return ADMIN_PERMISSION;
                } else {
                    // 其他用户没有权限
                    System.out.println("空权限返回");
                    return Collections.emptyList();
                }
            }
            //团队空间，查询空间成员返回权限
            SpaceUser spaceUser = spaceUserService.lambdaQuery()
                    .eq(SpaceUser::getSpaceId, space.getId())
                    .eq(SpaceUser::getUserId, user.getId())
                    .one();
            if(spaceUser == null){
                // 如果空间用户不存在，返回空权限
                return Collections.emptyList();
            }
            return userAuthManager.getPermissionsByRole(spaceUser.getSpaceRole());
        }
        return Collections.emptyList();
    }
    /**
     * 本项目不使用角色体系，返回空列表
     */
    @Override
    public List<String> getRoleList(Object o, String s) {
        return getPermissionList(o, s);
    }
    @Value("${server.servlet.context-path}")
    private String contextPath;
    /**
     * 从请求中获取上下文对象
     */
    private SpaceUserAuthContext getAuthContextByRequest() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String contentType = request.getHeader(Header.CONTENT_TYPE.getValue());
        SpaceUserAuthContext authRequest;
        // 兼容 get 和 post 操作
        if (ContentType.JSON.getValue().equals(contentType)) {
            String body = ServletUtil.getBody(request);
            authRequest = JSONUtil.toBean(body, SpaceUserAuthContext.class);
        } else {
            Map<String, String> paramMap = ServletUtil.getParamMap(request);
            authRequest = BeanUtil.toBean(paramMap, SpaceUserAuthContext.class);
        }
        // 根据请求路径区分 id 字段的含义
        Long id = authRequest.getId();
        if (ObjUtil.isNotNull(id)) {
            String requestUri = request.getRequestURI();
            String partUri = requestUri.replace(contextPath + "/", "");
            String moduleName = StrUtil.subBefore(partUri, "/", false);
            switch (moduleName) {
                case "picture":
                    authRequest.setPictureId(id);
                    break;
                case "spaceUser":
                    authRequest.setSpaceUserId(id);
                    break;
                case "space":
                    authRequest.setSpaceId(id);
                    break;
                default:
            }
        }
        return authRequest;
    }
    private boolean isAllFieldsNull(Object object) {
        if (object == null) {
            // 对象本身为空
            return true;
        }
        // 获取所有字段并判断是否所有字段都为空
        return Arrays.stream(ReflectUtil.getFields(object.getClass()))
                // 获取字段值
                .map(field -> ReflectUtil.getFieldValue(object, field))
                // 检查是否所有字段都为空
                .allMatch(ObjectUtil::isEmpty);
    }
}
