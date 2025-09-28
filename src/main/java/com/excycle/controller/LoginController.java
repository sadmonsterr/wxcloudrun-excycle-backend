package com.excycle.controller;

import com.excycle.entity.Admin;
import com.excycle.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;

@Slf4j
@Controller
@RequestMapping("/auth")
public class LoginController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private ServletContext servletContext;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(HttpServletRequest request, @RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        log.info("{} 用户名：{}，密码：{}", request.getServerName(), username, password);
        Admin admin = adminService.login(username, password);


        if (admin != null) {
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    admin,
                    null,
                    new ArrayList<>()
            );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
            session.setAttribute("admin", admin);
            String contextPath = servletContext.getContextPath();
            String forwardedProto = request.getHeader("X-Forwarded-Proto");
            log.info("{} X-Forwarded-Proto: {} contextPath {}", request.getServerName(), forwardedProto, contextPath);
            if ("https".equals(forwardedProto)) {
                return "redirect:https://" + request.getServerName() + contextPath + "/index";
            } else {
                return "redirect:/index";
            }
        } else {
            model.addAttribute("error", "用户名或密码错误");
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/login";
    }
}