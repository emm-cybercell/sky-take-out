package com.sky.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class RedisConfiguration {
    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory){
        log.info("开始创建redis模板对象");
        RedisTemplate redisTemplate = new RedisTemplate();
        //设置redis的连接工厂对象，负责创建 Redis 连接的核心接口
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        //设置redis key的序列化器，Java 对象与 Redis 中存储的字节数据转换
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        return redisTemplate;
    }
}
