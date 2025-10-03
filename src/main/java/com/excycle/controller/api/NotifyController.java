package com.excycle.controller.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/notify")
public class NotifyController {

    @PostMapping("/transfer")
    public void transferNotify(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Collections.list(request.getHeaderNames()).forEach(s -> log.info("header {}, value {}", s, request.getHeader(s)));
        log.info("transferNotify: {}", body);
    }

}
