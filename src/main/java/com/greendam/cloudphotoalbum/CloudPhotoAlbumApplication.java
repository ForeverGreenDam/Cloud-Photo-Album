package com.greendam.cloudphotoalbum;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author ForeverGreenDam
 */
@SpringBootApplication
@MapperScan("com.greendam.cloudphotoalbum.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableScheduling
@EnableAsync
public class CloudPhotoAlbumApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudPhotoAlbumApplication.class, args);
    }

}
