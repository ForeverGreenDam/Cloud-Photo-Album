package com.greendam.cloudphotoalbum.common.disruptor;

import com.greendam.cloudphotoalbum.common.websocket.model.PictureEditRequestMessage;
import com.greendam.cloudphotoalbum.model.vo.UserLoginVO;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

/**
 * 图片编辑事件生产者，用于发布图片编辑相关的事件
 * @author ForeverGreenDam
 */
@Component
@Slf4j
public class PictureEditEventProducer {

    @Resource
    Disruptor<PictureEditEvent> pictureEditEventDisruptor;

    public void publishEvent(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, UserLoginVO user, Long pictureId) {
        RingBuffer<PictureEditEvent> ringBuffer = pictureEditEventDisruptor.getRingBuffer();
        log.info("生产者已获得消息，准备发布事件");
        // 获取可以生成的位置
        long next = ringBuffer.next();
        PictureEditEvent pictureEditEvent = ringBuffer.get(next);
        pictureEditEvent.setSession(session);
        pictureEditEvent.setPictureEditRequestMessage(pictureEditRequestMessage);
        pictureEditEvent.setUser(user);
        pictureEditEvent.setPictureId(pictureId);
        // 发布事件
        ringBuffer.publish(next);
        log.info("生产者已发布事件，事件内容: {}", pictureEditEvent);
    }

    /**
     * 优雅停机
     */
    @PreDestroy
    public void close() {
        log.info("Disruptor正常关闭，正在处理消息队列中剩余的消息");
        pictureEditEventDisruptor.shutdown();
        log.info("Disruptor已关闭");
    }
}
