package com.greendam.cloudphotoalbum.common.websocket;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.greendam.cloudphotoalbum.common.websocket.model.PictureEditActionEnum;
import com.greendam.cloudphotoalbum.common.websocket.model.PictureEditMessageTypeEnum;
import com.greendam.cloudphotoalbum.common.websocket.model.PictureEditRequestMessage;
import com.greendam.cloudphotoalbum.common.websocket.model.PictureEditResponseMessage;
import com.greendam.cloudphotoalbum.exception.BusinessException;
import com.greendam.cloudphotoalbum.model.vo.UserLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 处理器，用于处理图片编辑相关的 WebSocket 消息
 * @author ForeverGreenDam
 */
@Component
@Slf4j
public class PictureEditHandler extends TextWebSocketHandler {
    /**
     * 每张图片的编辑状态，key: pictureId, value: 当前正在编辑的用户 ID
      */
    private final Map<Long, Long> pictureEditingUsers = new ConcurrentHashMap<>();
    /**
     * 保存所有连接的会话，key: pictureId, value: 用户会话集合
     */
    private final Map<Long, Set<WebSocketSession>> pictureSessions = new ConcurrentHashMap<>();

    /**
     * 建立连接成功的操作
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        //保存会话到集合中
        UserLoginVO user = (UserLoginVO)session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        pictureSessions.putIfAbsent(pictureId, ConcurrentHashMap.newKeySet());
        pictureSessions.get(pictureId).add(session);
        //构建 PictureEditResponseMessage
        PictureEditResponseMessage responseMessage = new PictureEditResponseMessage();
        responseMessage.setType(PictureEditMessageTypeEnum.INFO.getValue());
        responseMessage.setMessage(String.format("用户 %s 已经进入协同编辑", user.getUserName()));
        responseMessage.setUser(user);
        //广播发送给所有用户
        broadcastToPicture(pictureId, responseMessage);
    }

    /**
     * 建立连接中接受信息的操作
     */
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        //获取相关信息
        UserLoginVO user = (UserLoginVO) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        PictureEditRequestMessage pictureEditRequest = JSONUtil.toBean(message.getPayload().toString(), PictureEditRequestMessage.class);
        String type = pictureEditRequest.getType();
        PictureEditMessageTypeEnum typeEnum = PictureEditMessageTypeEnum.getEnumByValue(type);
        switch (typeEnum) {
            case ENTER_EDIT:
                handleEnterMessage(session, user, pictureId);
                break;
            case EDIT_ACTION:
                handleEditActionMessage(pictureEditRequest, session, user, pictureId);
                break;
            case EXIT_EDIT:
                handleExitMessage(session, user, pictureId);
                break;
            default:
               log.info("未知的消息类型: {}", type);
                PictureEditResponseMessage responseMessage = new PictureEditResponseMessage();
                responseMessage.setType(PictureEditMessageTypeEnum.ERROR.getValue());
                responseMessage.setMessage("未知的消息类型");
                responseMessage.setUser(user);
                session.sendMessage(new TextMessage(JSONUtil.toJsonStr(responseMessage)));
        }

    }

    /**
     * 处理退出编辑的消息
     */
    private void handleExitMessage(WebSocketSession session, UserLoginVO user, Long pictureId) throws Exception {
        Long userId = pictureEditingUsers.get(pictureId);
        if(userId != null&&userId.equals(user.getId())) {
            pictureEditingUsers.remove(pictureId);
            PictureEditResponseMessage responseMessage = new PictureEditResponseMessage();
            responseMessage.setType(PictureEditMessageTypeEnum.EXIT_EDIT.getValue());
            responseMessage.setMessage(String.format("用户 %s 退出编辑",user.getUserName()));
            responseMessage.setUser(user);
            broadcastToPicture(pictureId,responseMessage);
        }
    }

    /**
     * 处理编辑操作的消息
     */
    private void handleEditActionMessage(PictureEditRequestMessage pictureEditRequest, WebSocketSession session, UserLoginVO user, Long pictureId) throws Exception {
        String editAction = pictureEditRequest.getEditAction();
        PictureEditActionEnum enumByValue = PictureEditActionEnum.getEnumByValue(editAction);
        if(enumByValue==null){
            return;
        }
        //判断当前操作者是否为本人
        Long userId = pictureEditingUsers.get(pictureId);
        if(user.getId().equals(userId)){
            PictureEditResponseMessage responseMessage = new PictureEditResponseMessage();
            responseMessage.setType(PictureEditMessageTypeEnum.EDIT_ACTION.getValue());
            responseMessage.setEditAction(editAction);
            responseMessage.setMessage(String.format("用户 %s 正在执行 %s 操作",user.getUserName(),editAction));
            responseMessage.setUser(user);
            //广播，但是排除本人
            broadcastToPicture(pictureId,responseMessage,session);
        }
    }

    /**
     * 处理进入编辑的消息
     */
    private void handleEnterMessage( WebSocketSession session, UserLoginVO user, Long pictureId) throws Exception {
        //只有当前图片无人编辑时才可以编辑
        if (!pictureEditingUsers.containsKey(pictureId)) {
            pictureEditingUsers.put(pictureId, user.getId());
            PictureEditResponseMessage responseMessage = new PictureEditResponseMessage();
            responseMessage.setType(PictureEditMessageTypeEnum.ENTER_EDIT.getValue());
            responseMessage.setMessage(String.format("用户 %s 正在编辑图片",user.getUserName()));
            responseMessage.setUser(user);
            broadcastToPicture(pictureId,responseMessage);
        }
    }

    /**
     * 断开连接后的操作
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        //从集合中移除会话
        UserLoginVO user = (UserLoginVO)session.getAttributes().get("user");
        Long pictureId =(Long) session.getAttributes().get("pictureId");
        Set<WebSocketSession> webSocketSessions = pictureSessions.get(pictureId);
        if (webSocketSessions != null) {
            webSocketSessions.remove(session);
            if (webSocketSessions.isEmpty()) {
                pictureSessions.remove(pictureId);
            }
        }
        //用户直接退出网页而非点击退出编辑，需要执行退出编辑操作
        handleExitMessage(session, user, pictureId);
        //构建 PictureEditResponseMessage
        PictureEditResponseMessage responseMessage = new PictureEditResponseMessage();
        responseMessage.setType(PictureEditMessageTypeEnum.INFO.getValue());
        responseMessage.setMessage(String.format("用户 %s 已经退出协同编辑", user.getUserName()));
        responseMessage.setUser(user);
        //广播发送给所有用户
        broadcastToPicture(pictureId, responseMessage);
    }

    /**
     * 广播到指定图片的所有 WebSocket 会话
     */
    private void broadcastToPicture(Long pictureId, PictureEditResponseMessage pictureEditResponseMessage, WebSocketSession excludeSession) throws Exception {
        Set<WebSocketSession> sessionSet = pictureSessions.get(pictureId);
        if (CollUtil.isNotEmpty(sessionSet)) {
            // 创建 ObjectMapper
            ObjectMapper objectMapper = new ObjectMapper();
            // 配置序列化：将 Long 类型转为 String，解决丢失精度问题
            SimpleModule module = new SimpleModule();
            module.addSerializer(Long.class, ToStringSerializer.instance);
            // 支持 long 基本类型
            module.addSerializer(Long.TYPE, ToStringSerializer.instance);
            objectMapper.registerModule(module);
            // 序列化为 JSON 字符串
            String message = objectMapper.writeValueAsString(pictureEditResponseMessage);
            TextMessage textMessage = new TextMessage(message);
            for (WebSocketSession session : sessionSet) {
                // 排除掉的 session 不发送
                if (excludeSession != null && excludeSession.equals(session)) {
                    continue;
                }
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                }
            }
        }
    }
    /**
     *  全部广播
      */
    private void broadcastToPicture(Long pictureId, PictureEditResponseMessage pictureEditResponseMessage) throws Exception {
        broadcastToPicture(pictureId, pictureEditResponseMessage, null);
    }


}
