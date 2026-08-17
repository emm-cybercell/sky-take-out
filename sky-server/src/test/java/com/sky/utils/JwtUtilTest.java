package com.sky.utils;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JWT 工具类测试（jjwt 0.11 语法）
 */
class JwtUtilTest {

    /** 开发环境密钥，长度 >= 32 以满足 HS256 要求 */
    private static final String SECRET = "sky-admin-jwt-dev-secret-key-2024-0123456789abc";
    private static final long TTL = 60 * 1000; // 1 分钟

    @Test
    void createAndParseJwt() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("empId", 1001L);

        String token = JwtUtil.createJWT(SECRET, TTL, claims);
        Claims parsed = JwtUtil.parseJWT(SECRET, token);

        assertEquals(1001L, ((Number) parsed.get("empId")).longValue());
    }

    @Test
    void expiredTokenThrows() {
        String token = JwtUtil.createJWT(SECRET, -1000, new HashMap<>()); // 已过期
        assertThrows(Exception.class, () -> JwtUtil.parseJWT(SECRET, token));
    }

    @Test
    void wrongSecretCannotParse() {
        String token = JwtUtil.createJWT(SECRET, TTL, new HashMap<>());
        assertThrows(Exception.class, () -> JwtUtil.parseJWT("another-secret-key-123456789-abcdefghijk", token));
    }

    @Test
    void differentTtlProducesDifferentToken() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("empId", 1L);
        String t1 = JwtUtil.createJWT(SECRET, TTL, claims);
        String t2 = JwtUtil.createJWT(SECRET, TTL + 1000, claims);
        // 过期时间不同，生成的令牌不同
        assertTrue(!t1.equals(t2), "两个令牌不应相同");
    }
}