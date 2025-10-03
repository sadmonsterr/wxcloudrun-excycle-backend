package com.excycle.service.impl;

import com.excycle.utils.WXPayUtility;
import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.Expose;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
  private static TransferToUser client;

  public static void main(String[] args) {
    String mchId = System.getenv("MCH_ID");
    String apiClientCertificateSerialNo = System.getenv("API_CLIENT_SERIAL_NO");
    String apiClientPrivateKeyString = System.getenv("API_CLIENT_PRIVATE_KEY_STRING");
    String wechatPayPublicKeyId = System.getenv("WECHAT_PAY_PUBLIC_KEY_ID");
    String wechatPayPublicKeyString = System.getenv("WECHAT_PAY_PUBLIC_KEY_STRING");
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
            WXPayUtility.loadPublicKeyFromString(wechatPayPublicKeyString)           // 微信支付公钥文件路径，本地文件路径
    );
    TransferToUserResponse response = client.transfer(10L, "oaWBO10x5LiiFSHsXZYOd8k03lWU", "plfk2020042013");
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



  public TransferToUserResponse run(TransferToUserRequest request) {
    String uri = PATH;
    String reqBody = WXPayUtility.toJson(request);

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

  private String mchid;

  private String certificateSerialNo;

  private PrivateKey privateKey;

  private String wechatPayPublicKeyId;

  private PublicKey wechatPayPublicKey;

  public TransferToUser() {

  }

  public TransferToUser(String mchid, String certificateSerialNo, PrivateKey privateKey, String wechatPayPublicKeyId, PublicKey wechatPayPublicKey) {
    this.mchid = mchid;
    this.certificateSerialNo = certificateSerialNo;
    this.privateKey = privateKey;
    this.wechatPayPublicKeyId = wechatPayPublicKeyId;
    this.wechatPayPublicKey = wechatPayPublicKey;
  }

  public TransferToUser(String mchid, String certificateSerialNo, String privateKeyFilePath, String wechatPayPublicKeyId, String wechatPayPublicKeyFilePath) {
    this.mchid = mchid;
    this.certificateSerialNo = certificateSerialNo;
    this.privateKey = WXPayUtility.loadPrivateKeyFromPath(privateKeyFilePath);
    this.wechatPayPublicKeyId = wechatPayPublicKeyId;
    this.wechatPayPublicKey = WXPayUtility.loadPublicKeyFromPath(wechatPayPublicKeyFilePath);
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