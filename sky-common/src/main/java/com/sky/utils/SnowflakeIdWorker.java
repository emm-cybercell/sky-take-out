package com.sky.utils;

/**
 * 雪花算法 ID 生成器
 *
 * 64 位 Long 结构：1bit 符号位(0) + 41bit 毫秒时间戳 + 5bit 数据中心 + 5bit 机器 + 12bit 序列号
 * - 同一毫秒内最多生成 4096 个 ID，单实例吞吐约 40 万/秒
 * - 趋势递增、全局唯一，相比数据库自增/时间戳更适合作为订单号等业务主键
 *
 * 替代原先 `System.currentTimeMillis()` 生成订单号的方式：
 * 时间戳方案在并发下必然重复，且可被猜测，雪花 ID 在分布式场景下依旧唯一
 */
public class SnowflakeIdWorker {
    /** 起始时间戳（2022-01-01 00:00:00），用于缩短时间戳位数以延长可用年限 */
    private static final long START_TIMESTAMP = 1640995200000L;

    /** 各部分位数 */
    private static final long SEQUENCE_BITS = 12L;
    private static final long WORKER_ID_BITS = 5L;
    private static final long DATACENTER_ID_BITS = 5L;

    /** 各部分最大值 */
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);

    /** 各部分向左偏移量 */
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_LEFT_SHIFT = DATACENTER_ID_SHIFT + DATACENTER_ID_BITS;

    private final long datacenterId;
    private final long workerId;

    /** 同一毫秒内的序列号 */
    private long sequence = 0L;
    /** 上次生成 ID 的时间戳 */
    private long lastTimestamp = -1L;

    /** 全局单例：单体应用单机部署，workerId/datacenterId 固定为 1/1；分布式部署时按实例分配 */
    private static final SnowflakeIdWorker INSTANCE = new SnowflakeIdWorker(1, 1);

    private SnowflakeIdWorker(long datacenterId, long workerId) {
        if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId 超出范围: 0 ~ " + MAX_DATACENTER_ID);
        }
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException("workerId 超出范围: 0 ~ " + MAX_WORKER_ID);
        }
        this.datacenterId = datacenterId;
        this.workerId = workerId;
    }

    public static SnowflakeIdWorker getInstance() {
        return INSTANCE;
    }

    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();

        // 时钟回拨处理：容忍 5ms 以内的回拨（等待追平），超过则抛异常避免生成重复 ID
        if (timestamp < lastTimestamp) {
            long offset = lastTimestamp - timestamp;
            if (offset <= 5) {
                try {
                    wait(offset);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("等待时钟追平被中断", e);
                }
                timestamp = System.currentTimeMillis();
                if (timestamp < lastTimestamp) {
                    throw new RuntimeException("时钟回拨超过 5ms，拒绝生成 ID");
                }
            } else {
                throw new RuntimeException("时钟回拨超过 5ms，拒绝生成 ID");
            }
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            // 当前毫秒序列号用尽，等待下一毫秒
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - START_TIMESTAMP) << TIMESTAMP_LEFT_SHIFT)
                | (datacenterId << DATACENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    /** 阻塞到下一个毫秒 */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}