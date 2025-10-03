package com.excycle.controller.api;

import com.excycle.service.impl.TransferToUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collections;


import static com.excycle.service.impl.TransferToUser.client;

@Slf4j
@RestController
@RequestMapping("/api/v1/notify")
public class NotifyController {

    @PostMapping("/transfer")
    public void transferNotify(@RequestBody String body, HttpServletRequest request) {
        Collections.list(request.getHeaderNames()).forEach(s -> log.info("header {}, value {}", s, request.getHeader(s)));
        log.info("rawBody {}", body);
        TransferToUser.TransferToUserResponse notifyResponse = client.parseNotification(request, body);
        log.info("notify resp {}", notifyResponse);
    }

}
