package com.excycle.utils;

import java.util.Date;
import java.util.UUID;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.net.NetUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author oydl  2023/11/25
 */
@Slf4j
public class UUIDUtils {

    private static final String BASE62_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static int getServerPort(int defaultPort) {
        String portStr = System.getenv("SERVER_PORT");
        if (portStr != null) {
            try {
                return Integer.parseInt(portStr);
            } catch (NumberFormatException e) {
                System.err.println("Invalid SERVER_PORT: " + portStr + ", using default: " + defaultPort);
            }
        }
        return defaultPort;
    }

    // 2025-09-19 14:54:49
    private static final Snowflake snowflake = new Snowflake(new Date(1758264889646L), getWorkerId(), 1L, false);

    public static long getWorkerId() {

        String localIp = NetUtil.getLocalhostStr();
        int port = getServerPort(8080);
        // 如 "192.168.1.100"
        long ipLong = NetUtil.ipv4ToLong(localIp);
        long ipPart = ipLong & 0b11111; // 5 bits
        long portPart = getServerPort(8080) & 0b11111; // 5 bits
        // 合并：ipPart << 5 | portPart
        long workerId = ((ipPart << 5) | portPart) % 32;
        log.info("snowflake ip:port {}:{} workerId {}", localIp, port, workerId);
        return workerId;
    }

    public static String encodeBase62(long value) {
        if (value == 0) {
            return "0";
        }
        StringBuilder sb = new StringBuilder();
        while (value > 0) {
            sb.append(BASE62_CHARS.charAt((int) (value % 62)));
            value /= 62;
        }
        return sb.reverse().toString();
    }

    public static String nextBase62SnowflakeId() {
        return encodeBase62(snowflake.nextId());
    }

    public static long nextSnowflakeId() {
        return snowflake.nextId();
    }

    public static String randomUUID(){
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
