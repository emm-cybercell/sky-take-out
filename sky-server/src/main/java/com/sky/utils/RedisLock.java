package com.sky.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 的分布式锁（SETNX + 过期时间 + Lua 原子释放）
 *
 * 要点（面试常问）：
 * 1. 加锁用 `setIfAbsent(key, value, ttl)` —— 单条命令保证"占位 + 设置过期"原子性，
 *    避免 SETNX 后进程崩溃导致死锁
 * 2. value 使用 UUID：释放锁时先比对再删除（Lua 脚本原子执行），
 *    防止 A 线程执行业务超时锁过期后，误删 B 线程刚加的锁
 * 3. 业务实际执行时间超过锁过期时间时锁会提前释放，可结合 Redisson 的看门狗
 *    自动续期解决，本实现以"过期时间 = 业务预估耗时的数倍"为兜底
 */
@Component
public class RedisLock {

    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class);

    private final StringRedisTemplate redisTemplate;

    public RedisLock(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 尝试加锁
     *
     * @param key            锁的 key
     * @param expireSeconds  锁自动过期时间（秒）
     * @return 成功返回锁标识（释放时需要），失败返回 null
     */
    public String lock(String key, long expireSeconds) {
        String value = UUID.randomUUID().toString();
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, value, expireSeconds, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success) ? value : null;
    }

    /**
     * 释放锁（Lua 脚本保证"比对 + 删除"原子性，防止误删他人锁）
     */
    public void unlock(String key, String lockValue) {
        if (lockValue == null) {
            return;
        }
        redisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(key), lockValue);
    }
}