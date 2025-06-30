package com.greendam.cloudphotoalbum.common.disruptor;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.lmax.disruptor.dsl.Disruptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * Disruptor 配置类，用于生成 PictureEditEvent 的 Disruptor 实例
 * @author ForeverGreenDam
 */
@Slf4j
@Configuration
public class PictureEditEventDisruptorConfig {

    @Resource
    private PictureEditEventWorkHandler pictureEditEventWorkHandler;

    @Bean("pictureEditEventDisruptor")
    public Disruptor<PictureEditEvent> messageModelRingBuffer() {
        // ringBuffer 的大小
        int bufferSize = 1024 * 256;
        Disruptor<PictureEditEvent> disruptor = new Disruptor<>(
                PictureEditEvent::new,
                bufferSize,
                ThreadFactoryBuilder.create().setNamePrefix("pictureEditEventDisruptor").build()
        );
        log.info("创建 PictureEditEventDisruptor，缓冲区大小: {}", bufferSize);
        // 设置消费者
        disruptor.handleEventsWithWorkerPool(pictureEditEventWorkHandler);
        log.info("消费者已设置：{}", pictureEditEventWorkHandler.getClass().getSimpleName());
        // 开启 disruptor
        disruptor.start();
        log.info("PictureEditEventDisruptor 已启动");
        return disruptor;
    }
}
