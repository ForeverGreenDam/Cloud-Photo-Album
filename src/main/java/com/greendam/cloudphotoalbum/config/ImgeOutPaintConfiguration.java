package com.greendam.cloudphotoalbum.config;

import com.greendam.cloudphotoalbum.common.utils.ImageOutPaintUtil;
import com.greendam.cloudphotoalbum.properties.ModelProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI扩图配置类，用于创建ImageOutPaintUtil对象
 * @author ForeverGreenDam
 */
@Configuration
@Slf4j
public class ImgeOutPaintConfiguration {
    /**
     *确保只有一个ImageOutPaintUtil实例
     */
    @Bean
    @ConditionalOnMissingBean
    public ImageOutPaintUtil imageOutPaintUtil(ModelProperties modelProperties) {
        log.info("开始创建AI扩图工具类对象:{}", modelProperties);
        return new ImageOutPaintUtil(
                modelProperties.getApiKey(), modelProperties.getModel()
        );
    }
}
