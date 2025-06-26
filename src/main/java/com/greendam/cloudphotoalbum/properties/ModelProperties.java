package com.greendam.cloudphotoalbum.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 模型配置类，用于存储模型相关的配置信息
 * @author ForeverGreenDam
 */
@Data
@Component
@ConfigurationProperties(prefix = "greendam.model")
public class ModelProperties {
    /**
     * 模型apiKey
     */
    private String apiKey;
    /**
     * 模型名称
     */
    private  String model;
}
