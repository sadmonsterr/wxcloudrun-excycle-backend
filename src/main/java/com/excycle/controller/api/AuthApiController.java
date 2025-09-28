package com.excycle.controller.api;

import com.excycle.entity.Admin;
import com.excycle.service.AdminService;
import com.excycle.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证RESTful API
 */
@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
public class AuthApiController {

    @Autowired
    private AdminService adminService;

    /**
     * 用户登录
     * POST /api/v1/auth/login
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(HttpServletRequest request, @RequestBody LoginRequest loginRequest) {
        log.info("用户登录请求: {}", loginRequest.getUsername());

        Admin admin = adminService.login(loginRequest.getUsername(), loginRequest.getPassword());

        if (admin != null) {
            // 创建认证令牌
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    admin,
                    null,
                    new ArrayList<>()
            );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);

            // 构建返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("id", admin.getId());
            data.put("username", admin.getUsername());
            data.put("role", "ADMIN");
            data.put("loginTime", System.currentTimeMillis());

            return Result.success("登录成功", data);
        } else {
            return Result.error("用户名或密码错误");
        }
    }

    /**
     * 用户登出
     * POST /api/v1/auth/logout
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        SecurityContextHolder.clearContext();
        return Result.success("登出成功");
    }

    /**
     * 获取当前用户信息
     * GET /api/v1/auth/me
     */
    @GetMapping("/me")
    public Result<Map<String, Object>> getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal != null && principal instanceof Admin) {
            Admin admin = (Admin) principal;
            Map<String, Object> data = new HashMap<>();
            data.put("id", admin.getId());
            data.put("username", admin.getUsername());
            data.put("role", "ADMIN");

            return Result.success(data);
        } else {
            return Result.error("用户未登录");
        }
    }

    /**
     * 检查用户是否已登录
     * GET /api/v1/auth/check
     */
    @GetMapping("/check")
    public Result<Boolean> checkAuth() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean isAuthenticated = principal != null && principal instanceof Admin;

        if (isAuthenticated) {
            return Result.success(true);
        } else {
            return Result.error("未认证");
        }
    }

    /**
     * 登录请求DTO
     */
    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}