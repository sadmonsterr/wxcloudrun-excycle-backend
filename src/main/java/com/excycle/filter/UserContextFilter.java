package com.excycle.filter;

import com.excycle.context.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Slf4j
@WebFilter(urlPatterns = "/api/*")
@Component
public class UserContextFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("UserContextFilter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        try {
            // 从请求头获取 openId
            String openId = httpRequest.getHeader("x-wx-openid");
            String userId = httpRequest.getHeader("wx-userid");

            // 创建并设置用户上下文
            UserContext userContext = new UserContext();
            userContext.setOpenId(openId);
            userContext.setUserId(userId);
            UserContext.setCurrent(userContext);

            log.info("Set UserContext for openId: {}, userId {} ", openId, userId);

            // 继续过滤器链
            chain.doFilter(request, response);

        } finally {
            // 清理 ThreadLocal，防止内存泄漏
            UserContext.clear();
            log.debug("Cleared UserContext");
        }
    }

    @Override
    public void destroy() {
        log.info("UserContextFilter destroyed");
    }
}