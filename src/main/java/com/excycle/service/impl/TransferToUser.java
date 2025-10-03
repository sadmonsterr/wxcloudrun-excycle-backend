package com.excycle.service.impl;

import com.excycle.utils.WXPayUtility;
import com.google.gson.annotations.SerializedName;
import com.wechat.pay.java.core.util.GsonUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import java.util.Objects;

import static com.excycle.utils.WXPayUtility.verify;

/**
 * 发起转账
 */
@Slf4j
public class TransferToUser {

  private static String HOST = "https://api.mch.weixin.qq.com";

  private static String METHOD = "POST";

  private static String PATH = "/v3/fund-app/mch-transfer/transfer-bills";

//  // TODO: 请准备商户开发必要参数，参考：https://pay.weixin.qq.com/doc/v3/merchant/4013070756
//  private static TransferToUser client = new TransferToUser(
//          "1728813377",                    // 商户号，是由微信支付系统生成并分配给每个商户的唯一标识符，商户号获取方式参考 https://pay.weixin.qq.com/doc/v3/merchant/4013070756
//          "151578A8CE7158F03312FD35D31AB778755C2808",         // 商户API证书序列号，如何获取请参考 https://pay.weixin.qq.com/doc/v3/merchant/4013053053
//          "/Users/lanma/Downloads/excycle/1728813377_20251002_cert/apiclient_key.pem",     // 商户API证书私钥文件路径，本地文件路径
//          "PUB_KEY_ID_0117288133772025100200181886000802",      // 微信支付公钥ID，如何获取请参考 https://pay.weixin.qq.com/doc/v3/merchant/4013038816
//          "/Users/lanma/Downloads/excycle/pub_key.pem"           // 微信支付公钥文件路径，本地文件路径
//  );


  public static TransferToUser client;

  static {
    String mchId = System.getenv("MCH_ID");
    String apiClientCertificateSerialNo = System.getenv("API_CLIENT_SERIAL_NO");
    String apiClientPrivateKeyString = System.getenv("API_CLIENT_PRIVATE_KEY_STRING");
    String wechatPayPublicKeyId = System.getenv("WECHAT_PAY_PUBLIC_KEY_ID");
    String wechatPayPublicKeyString = System.getenv("WECHAT_PAY_PUBLIC_KEY_STRING");
    String apiV3Secret = System.getenv("API_V3_SECRET");
    log.info("mchId {}", mchId);
    log.info("apiClientCertificateSerialNo {}", apiClientCertificateSerialNo);
    log.info("apiClientPrivateKeyString {}", apiClientPrivateKeyString);
    log.info("wechatPayPublicKeyId {}", wechatPayPublicKeyId);
    log.info("wechatPayPublicKeyString {}", wechatPayPublicKeyString);

    client = new TransferToUser(
            mchId,                    // 商户号，是由微信支付系统生成并分配给每个商户的唯一标识符，商户号获取方式参考 https://pay.weixin.qq.com/doc/v3/merchant/4013070756
            apiClientCertificateSerialNo,         // 商户API证书序列号，如何获取请参考 https://pay.weixin.qq.com/doc/v3/merchant/4013053053
            WXPayUtility.loadPrivateKeyFromString(apiClientPrivateKeyString),     // 商户API证书私钥文件路径，本地文件路径
            wechatPayPublicKeyId,      // 微信支付公钥ID，如何获取请参考 https://pay.weixin.qq.com/doc/v3/merchant/4013038816
            WXPayUtility.loadPublicKeyFromString(wechatPayPublicKeyString),         // 微信支付公钥文件路径，本地文件路径
            apiV3Secret
    );
  }

  public static void main(String[] args) {
    String mchId = System.getenv("MCH_ID");
    String apiClientCertificateSerialNo = System.getenv("API_CLIENT_SERIAL_NO");
    String apiClientPrivateKeyString = System.getenv("API_CLIENT_PRIVATE_KEY_STRING");
    String wechatPayPublicKeyId = System.getenv("WECHAT_PAY_PUBLIC_KEY_ID");
    String wechatPayPublicKeyString = System.getenv("WECHAT_PAY_PUBLIC_KEY_STRING");
    String apiV3Secret = System.getenv("API_V3_SECRET");
    log.info("mchId {}", mchId);
    log.info("apiClientCertificateSerialNo {}", apiClientCertificateSerialNo);
    log.info("apiClientPrivateKeyString {}", apiClientPrivateKeyString);
    log.info("wechatPayPublicKeyId {}", wechatPayPublicKeyId);
    log.info("wechatPayPublicKeyString {}", wechatPayPublicKeyString);

    String timestamp = "1759511445";
    try {
      Instant responseTime = Instant.ofEpochSecond(Long.parseLong(timestamp));
      // 拒绝过期请求
      if (Duration.between(responseTime, Instant.now()).abs().toMinutes() >= 500000) {
        throw new IllegalArgumentException(
                String.format("Validate notification failed, timestamp[%s] is expired", timestamp));
      }
    } catch (DateTimeException | NumberFormatException e) {
      throw new IllegalArgumentException(
              String.format("Validate notification failed, timestamp[%s] is invalid", timestamp));
    }
    String serialNumber = "PUB_KEY_ID_0117288133772025100200181886000802";
    if (!Objects.equals(serialNumber, wechatPayPublicKeyId)) {
      throw new IllegalArgumentException(
              String.format("Validate notification failed, Invalid Wechatpay-Serial, Local: %s, " +
                              "Remote: %s",
                      wechatPayPublicKeyId,
                      serialNumber));
    }
    String body = "{\"id\":\"93c43dcb-36e5-596f-be20-4f23eeeece11\",\"create_time\":\"2025-10-04T01:10:39+08:00\",\"resource_type\":\"encrypt-resource\",\"event_type\":\"MCHTRANSFER.BILL.FINISHED\",\"summary\":\"商家转账单据终态通知\",\"resource\":{\"original_type\":\"mch_payment\",\"algorithm\":\"AEAD_AES_256_GCM\",\"ciphertext\":\"wkBS3bPUjgId5b9Q8nadZ6XG4d0NDBqj9Sssc6RsR5fEo5ONYEZlKbztYDHujsEco7NZrO8nHdC9y5CXGkNQ32DqLXUP3/YHK3rlONZYv1V9rcJ8B9vzHwduj6MP8WpLnnmL7xzbAVUGyzvP16S6qemnFudZKHaoxB0BjsV3sJXCe55h+IW8eiyyw2iFFe3Klrs6B2xepj2g/9p+jKxW4Uw71cSy/pCZMeX6pmINkQIxxiT5pjewwuheian3GsvX37B7TWHLtqh8LjFyV1MpsrhVRSDJtIAkoRSx8AznIbZGbIMX5vxASkaXMtBuyF+3XO6tQgmqsZrjqV+e48hcsGkS/gRCPC7q3ulGHxfqLylLV1c0NrtMUfkNBGe43yXomdQfVlvTss/64gw9nkK8R8E=\",\"associated_data\":\"mch_payment\",\"nonce\":\"Waf01GhiOsYd\"}}";
    String signature = "g7HxTkbVuQOMH3flusLHJuePlJp+uNwht1Bb/L7CDekAShtdAHExQuLp/so2Vztb7ahNP7UC7VErbeTblK9y7klAJFSUo2PSXhyqmEjjsPvG3MruOMg2oJrxV5VvRVJGKfWfdhnrrvOL+xtAkKjB9ksz1UYWi1Bb18Kkl+pls0s8eHKlBm75+wJ0SuXErZfg7QvizqTgZJJ79/8CgvbOvB2dyX8sb5hArYsOJJ+zrzxqXqtFghHjMlQlJhnpfHVWRl0AtwIDDPlZwxeXML03wy2vCJGRsNJ595QvsofGjxx66jiQVMnRU2OkypBZcoZ4mb3GDgvyCCEtb8FnouPpYQ==";
    String message = String.format("%s\n%s\n%s\n", timestamp, "SMhYjgSAkcoNmcul9LzDdCUPnqKa3VkU",
            body == null ? "" : body);

    boolean success = verify(message, signature, "SHA256withRSA", WXPayUtility.loadPublicKeyFromString(wechatPayPublicKeyString));
    if (!success) {
      throw new IllegalArgumentException(
              String.format("Validate notification failed, WechatPay signature is incorrect.\n"
                              + "responseHeader[%s]\tresponseBody[%.1024s]",
                      "headers", body));
    }
    WXPayUtility.Notification notification = WXPayUtility.decrypt(apiV3Secret,  body);
    log.info("{}", notification);
    TransferToUserResponse response = GsonUtil.getGson().fromJson(notification.getPlaintext(), TransferToUserResponse.class);

    log.info("{}", response);
//    WXPayUtility.validateNotification(wechatPayPublicKeyId , WXPayUtility.loadPublicKeyFromString(wechatPayPublicKeyString)  ,
//            "wechatpayPublicKey", request.getHeaders(), WXPayUtility.extractBody(request));

//    client = new TransferToUser(
//            mchId,                    // 商户号，是由微信支付系统生成并分配给每个商户的唯一标识符，商户号获取方式参考 https://pay.weixin.qq.com/doc/v3/merchant/4013070756
//            apiClientCertificateSerialNo,         // 商户API证书序列号，如何获取请参考 https://pay.weixin.qq.com/doc/v3/merchant/4013053053
//            WXPayUtility.loadPrivateKeyFromString(apiClientPrivateKeyString),     // 商户API证书私钥文件路径，本地文件路径
//            wechatPayPublicKeyId,      // 微信支付公钥ID，如何获取请参考 https://pay.weixin.qq.com/doc/v3/merchant/4013038816
//            WXPayUtility.loadPublicKeyFromString(wechatPayPublicKeyString)           // 微信支付公钥文件路径，本地文件路径
//    );
    response = client.transfer(10L, "oaWBO10x5LiiFSHsXZYOd8k03lWU", "plfk2020042013");
    // TODO: 请求成功，继续业务逻辑
    System.out.println(response);
  }

  public TransferToUserResponse transfer(Long amount, String openid, String outBillNo) {
    TransferToUserRequest request = new TransferToUserRequest();
    request.appid = "wx764480a6fa9e8719";
    request.outBillNo = outBillNo;
    request.openid = openid;
//    request.userName = client.encrypt("韩煜贇");
    request.transferAmount = amount;
    request.transferRemark = "二手回收货款";
    request.notifyUrl = "https://excycle-backend-189832-5-1378998210.sh.run.tcloudbase.com/excycle/api/v1/notify/transfer";
    request.transferSceneId = "1010";
    request.userRecvPerception = "二手回收货款";
    request.transferSceneReportInfos = new ArrayList<>();
    TransferSceneReportInfo transferSceneReportInfosItem0 = new TransferSceneReportInfo();
    transferSceneReportInfosItem0.infoType = "回收商品名称";
    transferSceneReportInfosItem0.infoContent = "轮胎";
    request.transferSceneReportInfos.add(transferSceneReportInfosItem0);
    try {
      TransferToUserResponse response = run(request);
      // TODO: 请求成功，继续业务逻辑
      System.out.println(response);
      return response;
    } catch (WXPayUtility.ApiException e) {
      // TODO: 请求失败，根据状态码执行不同的逻辑
      log.error("WeChat Cloud API request failed with status: {}, response: {}", e.getStatusCode(), e.getBody(), e);
      throw new RuntimeException("微信云服务请求失败，状态码: " + e.getStatusCode());
    }
  }

  public static void validateNotification() {

//    WXPayUtility.validateNotification(, "wechatpayPublicKeyId",
//            "wechatpayPublicKey", request.getHeaders(), WXPayUtility.extractBody(request));
  }


  public TransferToUserResponse run(TransferToUserRequest request) {
    String uri = PATH;
    String reqBody = WXPayUtility.toJson(request);
    log.info("request body: {}", reqBody);

    Request.Builder reqBuilder = new Request.Builder().url(HOST + uri);
    reqBuilder.addHeader("Accept", "application/json");
    reqBuilder.addHeader("Wechatpay-Serial", wechatPayPublicKeyId);
    reqBuilder.addHeader("Authorization", WXPayUtility.buildAuthorization(mchid, certificateSerialNo,privateKey, METHOD, uri, reqBody));
    reqBuilder.addHeader("Content-Type", "application/json");


    RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), reqBody);
    reqBuilder.method(METHOD, requestBody);
    Request httpRequest = reqBuilder.build();

    // 发送HTTP请求
    OkHttpClient client = new OkHttpClient.Builder().build();
    try (Response httpResponse = client.newCall(httpRequest).execute()) {
      String respBody = WXPayUtility.extractBody(httpResponse);
      if (httpResponse.code() >= 200 && httpResponse.code() < 300) {
        // 2XX 成功，验证应答签名
        WXPayUtility.validateResponse(this.wechatPayPublicKeyId, this.wechatPayPublicKey,
            httpResponse.headers(), respBody);

        // 从HTTP应答报文构建返回数据
        return WXPayUtility.fromJson(respBody, TransferToUserResponse.class);
      } else {
        throw new WXPayUtility.ApiException(httpResponse.code(), respBody, httpResponse.headers());
      }
    } catch (IOException e) {
      throw new UncheckedIOException("Sending request to " + uri + " failed.", e);
    }
  }

  public TransferToUserResponse parseNotification(HttpServletRequest request, String body) {
    WXPayUtility.Notification notification = WXPayUtility.parseNotification(apiV3Secret,
            wechatPayPublicKeyId, wechatPayPublicKey, request, body);
    return GsonUtil.getGson().fromJson(notification.getPlaintext(), TransferToUserResponse.class);
  }

  private String mchid;

  private String certificateSerialNo;

  private PrivateKey privateKey;

  private String wechatPayPublicKeyId;

  private PublicKey wechatPayPublicKey;

  private String apiV3Secret;

  public TransferToUser() {

  }

  public TransferToUser(String mchid, String certificateSerialNo, PrivateKey privateKey, String wechatPayPublicKeyId, PublicKey wechatPayPublicKey, String apiV3Secret) {
    this.mchid = mchid;
    this.certificateSerialNo = certificateSerialNo;
    this.privateKey = privateKey;
    this.wechatPayPublicKeyId = wechatPayPublicKeyId;
    this.wechatPayPublicKey = wechatPayPublicKey;
    this.apiV3Secret = apiV3Secret;
  }

  public TransferToUser(String mchid, String certificateSerialNo, String privateKeyFilePath, String wechatPayPublicKeyId, String wechatPayPublicKeyFilePath,
                        String apiV3Secret) {
    this.mchid = mchid;
    this.certificateSerialNo = certificateSerialNo;
    this.privateKey = WXPayUtility.loadPrivateKeyFromPath(privateKeyFilePath);
    this.wechatPayPublicKeyId = wechatPayPublicKeyId;
    this.wechatPayPublicKey = WXPayUtility.loadPublicKeyFromPath(wechatPayPublicKeyFilePath);
    this.apiV3Secret = apiV3Secret;
  }


  public String encrypt(String plainText) {
    return WXPayUtility.encrypt(this.wechatPayPublicKey, plainText);
  }

  public static class TransferToUserRequest {
    @SerializedName("appid")
    public String appid;
  
    @SerializedName("out_bill_no")
    public String outBillNo;
  
    @SerializedName("transfer_scene_id")
    public String transferSceneId;
  
    @SerializedName("openid")
    public String openid;
  
    @SerializedName("user_name")
    public String userName;
  
    @SerializedName("transfer_amount")
    public Long transferAmount;
  
    @SerializedName("transfer_remark")
    public String transferRemark;
  
    @SerializedName("notify_url")
    public String notifyUrl;
  
    @SerializedName("user_recv_perception")
    public String userRecvPerception;
  
    @SerializedName("transfer_scene_report_infos")
    public List<TransferSceneReportInfo> transferSceneReportInfos = new ArrayList<TransferSceneReportInfo>();
  }

  @Data
  public static class TransferToUserResponse {

    @SerializedName("out_bill_no")
    public String outBillNo;
  
    @SerializedName("transfer_bill_no")
    public String transferBillNo;
  
    @SerializedName("create_time")
    public String createTime;
  
    @SerializedName("state")
    public TransferBillStatus state;
  
    @SerializedName("package_info")
    public String packageInfo;
  }
  
  public static class TransferSceneReportInfo {
    @SerializedName("info_type")
    public String infoType;
  
    @SerializedName("info_content")
    public String infoContent;
  }
  
  public enum TransferBillStatus {
    @SerializedName("ACCEPTED")
    ACCEPTED,
    @SerializedName("PROCESSING")
    PROCESSING,
    @SerializedName("WAIT_USER_CONFIRM")
    WAIT_USER_CONFIRM,
    @SerializedName("TRANSFERING")
    TRANSFERING,
    @SerializedName("SUCCESS")
    SUCCESS,
    @SerializedName("FAIL")
    FAIL,
    @SerializedName("CANCELING")
    CANCELING,
    @SerializedName("CANCELLED")
    CANCELLED
  }
  
}