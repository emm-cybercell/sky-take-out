package com.sky.utils;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 雪花算法 ID 生成器测试
 */
class SnowflakeIdWorkerTest {

    private final SnowflakeIdWorker idWorker = SnowflakeIdWorker.getInstance();

    @Test
    void singleThreadIdsAreUniqueAndIncreasing() {
        // 单线程 10000 个 ID：全部唯一且严格递增
        Set<Long> ids = new HashSet<>();
        long prev = -1;
        for (int i = 0; i < 10000; i++) {
            long id = idWorker.nextId();
            assertTrue(id > 0, "ID 必须为正数");
            assertTrue(id > prev, "ID 必须严格递增");
            assertTrue(ids.add(id), "ID 重复：" + id);
            prev = id;
        }
    }

    @Test
    void concurrentIdsAreUnique() throws InterruptedException {
        // 100 个线程 × 2000 个 ID：并发场景下不产生重复（序列号 + 时间戳保证）
        int threads = 100;
        int perThread = 2000;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        Set<Long> ids = java.util.Collections.synchronizedSet(new HashSet<>());

        for (int t = 0; t < threads; t++) {
            pool.execute(() -> {
                try {
                    for (int i = 0; i < perThread; i++) {
                        ids.add(idWorker.nextId());
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        assertTrue(latch.await(30, TimeUnit.SECONDS), "生成超时");
        pool.shutdownNow();

        assertEquals(threads * perThread, ids.size(), "并发下产生重复 ID");
    }

    @Test
    void idLengthFitsBigIntColumn() {
        // 订单号在 17~19 位之间，可安全存 varchar(50) 且不会溢出数据库数值列
        long id = idWorker.nextId();
        int len = String.valueOf(id).length();
        assertTrue(len >= 17 && len <= 19, "ID 位数异常：" + id + " len=" + len);
    }
}