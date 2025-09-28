package com.excycle.controller.api;

import com.excycle.service.UserService;
import com.excycle.service.OrderService;
import com.excycle.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 仪表盘RESTful API
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@CrossOrigin(origins = "*")
public class DashboardApiController {

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    /**
     * 获取仪表盘统计数据
     * GET /api/v1/dashboard/stats
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getDashboardStats() {
        long userCount = userService.count();
        long orderCount = orderService.count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("userCount", userCount);
        stats.put("orderCount", orderCount);
        stats.put("systemStatus", "normal");

        return Result.success(stats);
    }

    /**
     * 获取系统信息
     * GET /api/v1/dashboard/system-info
     */
    @GetMapping("/system-info")
    public Result<Map<String, Object>> getSystemInfo() {
        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("version", "1.0.0");
        systemInfo.put("databaseStatus", "normal");
        systemInfo.put("serverStatus", "normal");
        systemInfo.put("lastUpdate", "2024-01-01");

        return Result.success(systemInfo);
    }

    /**
     * 获取最近活动
     * GET /api/v1/dashboard/recent-activities
     */
    @GetMapping("/recent-activities")
    public Result<Map<String, Object>> getRecentActivities() {
        Map<String, Object> activities = new HashMap<>();
        // 这里可以添加从数据库查询最近活动的逻辑
        activities.put("recentActivities", new java.util.ArrayList<>());

        return Result.success(activities);
    }
}