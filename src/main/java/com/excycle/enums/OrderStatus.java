package com.excycle.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum OrderStatus {
    CANCELLED("CANCELLED", "已取消"),
    WAITING("WAITING", "待接单"),
    ASSIGNED("ASSIGNED", "已接单"),
    IN_PROGRESS("IN_PROGRESS", "进行中"),
    COLLECTED("COLLECTED", "已取货"),
    TRANSFERRING("TRANSFERRING", "转账"),
    COMPLETED("COMPLETED", "已完成"),
    UNKNOWN("UNKNOWN", "未知");
    // deprecated

    private final String key;
    private final String description;

    OrderStatus(String key, String description) {
        this.key = key;
        this.description = description;
    }

    @JsonValue
    public String getKey() {
        return key;
    }

    public String getDescription() {
        return description;
    }

    public static OrderStatus fromKey(String key) {
        for (OrderStatus status : OrderStatus.values()) {
            if (status.getKey().equalsIgnoreCase(key)) {
                return status;
            }
        }
        log.warn("Unknown order status: {}", key);
        return UNKNOWN;
    }
}