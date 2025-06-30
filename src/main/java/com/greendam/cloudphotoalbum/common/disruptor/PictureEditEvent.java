package com.greendam.cloudphotoalbum.common.disruptor;

import com.greendam.cloudphotoalbum.common.websocket.model.PictureEditRequestMessage;
import com.greendam.cloudphotoalbum.model.vo.UserLoginVO;
import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

/**
 * 图片编辑事件（消息模型）
 * @author ForeverGreenDam
 */
@Data
public class PictureEditEvent {

    /**
     * 消息
     */
    private PictureEditRequestMessage pictureEditRequestMessage;

    /**
     * 当前用户的 session
     */
    private WebSocketSession session;
    
    /**
     * 当前用户
     */
    private UserLoginVO user;

    /**
     * 图片 id
     */
    private Long pictureId;

}
