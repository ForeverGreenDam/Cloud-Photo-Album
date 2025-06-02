package com.greendam.cloudphotoalbum;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author ForeverGreenDam
 */
@SpringBootApplication
@MapperScan("com.greendam.cloudphotoalbum.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class CloudPhotoAlbumApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudPhotoAlbumApplication.class, args);
    }

}
