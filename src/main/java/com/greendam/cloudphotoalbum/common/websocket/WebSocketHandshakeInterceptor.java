package com.greendam.cloudphotoalbum.common.websocket;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.greendam.cloudphotoalbum.common.auth.SpaceUserAuthManager;
import com.greendam.cloudphotoalbum.constant.SpaceUserPermissionConstant;
import com.greendam.cloudphotoalbum.model.entity.Picture;
import com.greendam.cloudphotoalbum.model.entity.Space;
import com.greendam.cloudphotoalbum.model.enums.SpaceTypeEnum;
import com.greendam.cloudphotoalbum.model.vo.UserLoginVO;
import com.greendam.cloudphotoalbum.model.vo.UserVO;
import com.greendam.cloudphotoalbum.service.PictureService;
import com.greendam.cloudphotoalbum.service.SpaceService;
import com.greendam.cloudphotoalbum.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * WebSocket 握手拦截器
 * @author ForeverGreenDam
 */
@Slf4j
@Component
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {
    @Resource
    UserService userService;
    @Resource
    PictureService pictureService;
    @Resource
    SpaceService spaceService;
    @Resource
    SpaceUserAuthManager spaceUserAuthManager;

    /**
     * 在握手之前执行，进行用户验证和权限校验
     * @param request 用于获取登录用户
     * @param response 响应对象
     * @param wsHandler WebSocket处理器
     * @param attributes 握手属性，用于存储用户信息等
     * @return 如果验证通过返回 true，拒绝连接返回 false
     * @throws Exception 如果发生异常，握手将被拒绝
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        //获取当前登录用户
        if(request instanceof ServletServerHttpRequest){
            HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
            //从请求中获取用户信息
            String pictureId = servletRequest.getParameter("pictureId");
            if(StrUtil.isBlank(pictureId)){
                log.error("缺少图片参数，拒绝连接");
                return false;
            }
            UserLoginVO user = userService.getUser(servletRequest);
            if(user == null){
                log.error("用户未登录，拒绝连接");
                return false;
            }
            //校验用户是否有编辑图片权限
            Picture picture = pictureService.getById(pictureId);
            if(picture == null){
                log.error("图片不存在，拒绝连接");
                return false;
            }
            //如果是团队空间，且有权限编辑，则建立连接
            Long spaceId = picture.getSpaceId();
            if(spaceId == null){
                log.error("公共图库空间，拒绝连接");
                return false;
            }
            Space  space = spaceService.getById(spaceId);
            if(space == null){
                log.error("空间不存在，拒绝连接");
                return false;
            }
            if (!space.getSpaceType().equals(SpaceTypeEnum.TEAM.getValue())){
                log.error("非团队空间，拒绝连接");
                return false;
            }
            List<String> permissionList = spaceUserAuthManager.getPermissionList(space, BeanUtil.copyProperties(user, UserVO.class));
            if(!permissionList.contains(SpaceUserPermissionConstant.PICTURE_EDIT)){
                log.error("用户无编辑图片权限，拒绝连接");
                return false;
            }
            //将用户信息存入 attributes 中
            attributes.put("user", user);
            attributes.put("userId",user.getId());
            attributes.put("pictureId", picture.getId());
            return true;
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
