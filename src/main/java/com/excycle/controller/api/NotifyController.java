package com.excycle.controller.api;

import com.excycle.entity.ThirdPartyNotify;
import com.excycle.mapper.ThirdPartyNotifyMapper;
import com.excycle.service.impl.TransferToUser;
import com.excycle.utils.UUIDUtils;
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


import static com.excycle.service.impl.TransferToUser.client;

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
        TransferToUser.TransferToUserResponse notifyResponse =
                GsonUtil.getGson().fromJson(notification.getPlaintext(), TransferToUser.TransferToUserResponse.class);
        log.info("notify resp {}", notifyResponse);
        // 保存通知数据到数据库
        savePaymentNotify(notification, notifyResponse);
        // 发布事件
        publisher.publishEvent(notifyResponse);
    }

    private void savePaymentNotify(WXPayUtility.Notification notification, TransferToUser.TransferToUserResponse notifyResponse) {
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

    @PostConstruct
    public void test() {

        WXPayUtility.Notification notification = new WXPayUtility.Notification();
        notification.setId("2");
        notification.setPlaintext("{}");

        TransferToUser.TransferToUserResponse response = new TransferToUser.TransferToUserResponse();
        response.setOutBillNo("p5Xnelyg0");
        response.setTransferBillNo("1330008146248192510040013444707619");
        response.setState(TransferToUser.TransferBillStatus.SUCCESS);
        response.setCreateTime(new Timestamp(System.currentTimeMillis()).toString());
        savePaymentNotify(notification, response);

        CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                publisher.publishEvent(response);
            }
        });
    }

}
