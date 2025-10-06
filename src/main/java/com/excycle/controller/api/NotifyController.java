package com.excycle.controller.api;

import com.excycle.entity.ThirdPartyNotify;
import com.excycle.mapper.ThirdPartyNotifyMapper;
import com.excycle.service.impl.TransferService;
import com.excycle.utils.WXPayUtility;
import com.wechat.pay.java.core.util.GsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.concurrent.CompletableFuture;


import static com.excycle.service.impl.TransferService.client;

@Slf4j
@RestController
@RequestMapping("/api/v1/notification")
public class NotifyController {

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private ThirdPartyNotifyMapper thirdPartyNotifyMapper;

    @PostMapping("/transfer")
    @Transactional(rollbackFor = Exception.class)
    public void transferNotify(@RequestBody String body, HttpServletRequest request) {
        log.info("rawBody {}", body);
        // 解析通知数据
        WXPayUtility.Notification notification = client.parseNotification(request, body);
        TransferService.TransferToUserResponse notifyResponse =
                GsonUtil.getGson().fromJson(notification.getPlaintext(), TransferService.TransferToUserResponse.class);
        log.info("notify resp {}", notifyResponse);
        // 保存通知数据到数据库
        savePaymentNotify(notification, notifyResponse);
        // 发布事件
        publisher.publishEvent(notifyResponse);
    }

    private void savePaymentNotify(WXPayUtility.Notification notification, TransferService.TransferToUserResponse notifyResponse) {
        ThirdPartyNotify thirdPartyNotify = new ThirdPartyNotify();
        thirdPartyNotify.setNotifyId(notification.getId());
        thirdPartyNotify.setNotifyBody(notification.getPlaintext());
        thirdPartyNotify.setRequestId(notifyResponse.getOutBillNo());
        thirdPartyNotify.setOutTradeNo(notifyResponse.getTransferBillNo());
        thirdPartyNotify.setStatus(notifyResponse.getState().name());
        thirdPartyNotify.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        thirdPartyNotify.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        thirdPartyNotifyMapper.insert(thirdPartyNotify);
    }

}
