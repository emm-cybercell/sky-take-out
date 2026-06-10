package com.sky.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sky.properties.AliOssProperties;
import com.sky.utils.AliOssUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 阿里云 OSS 配置类
 */
@Configuration
@Slf4j
public class OssConfiguration {
    /**
     * 创建阿里云文件上传工具类的 Bean 对象
     * @param aliOssProperties
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public AliOssUtil aliOssUtils(AliOssProperties aliOssProperties) {
        log.info("开始创建阿里云文件上传工具类：{}", aliOssProperties);
        return new AliOssUtil(aliOssProperties.getEndpoint(), 
            aliOssProperties.getAccessKeyId(), 
            aliOssProperties.getAccessKeySecret(), 
            aliOssProperties.getBucketName());
    }
}
