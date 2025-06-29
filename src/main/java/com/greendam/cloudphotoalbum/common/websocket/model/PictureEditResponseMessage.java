package com.greendam.cloudphotoalbum.common.websocket.model;

import com.greendam.cloudphotoalbum.model.vo.UserLoginVO;
import com.greendam.cloudphotoalbum.model.vo.UserVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图片编辑响应消息模型
 * @author ForeverGreenDam
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PictureEditResponseMessage {

    /**
     * 消息类型，例如 "INFO", "ERROR", "ENTER_EDIT", "EXIT_EDIT", "EDIT_ACTION"
     */
    private String type;

    /**
     * 信息
     */
    private String message;

    /**
     * 执行的编辑动作
     */
    private String editAction;

    /**
     * 用户信息
     */
    private UserLoginVO user;
}
