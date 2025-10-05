package com.excycle.utils;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import okhttp3.Headers;
import okhttp3.Response;
import okio.BufferedSource;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.security.MessageDigest;
import java.io.InputStream;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.Security;

public class WXPayUtility {
    private static final Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .addSerializationExclusionStrategy(new ExclusionStrategy() {
                @Override
                public boolean shouldSkipField(FieldAttributes fieldAttributes) {
                    final Expose expose = fieldAttributes.getAnnotation(Expose.class);
                    return expose != null && !expose.serialize();
                }

                @Override
                public boolean shouldSkipClass(Class<?> aClass) {
                    return false;
                }
            })
            .addDeserializationExclusionStrategy(new ExclusionStrategy() {
                @Override
                public boolean shouldSkipField(FieldAttributes fieldAttributes) {
                    final Expose expose = fieldAttributes.getAnnotation(Expose.class);
                    return expose != null && !expose.deserialize();
                }

                @Override
                public boolean shouldSkipClass(Class<?> aClass) {
                    return false;
                }
            })
            .create();
    private static final char[] SYMBOLS =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final SecureRandom random = new SecureRandom();

    /**
     * 将 Object 转换为 JSON 字符串
     */
    public static String toJson(Object object) {
        return gson.toJson(object);
    }

    /**
     * 将 JSON 字符串解析为特定类型的实例
     */
    public static <T> T fromJson(String json, Class<T> classOfT) throws JsonSyntaxException {
        return gson.fromJson(json, classOfT);
    }

    /**
     * 从公私钥文件路径中读取文件内容
     *
     * @param keyPath 文件路径
     * @return 文件内容
     */
    private static String readKeyStringFromPath(String keyPath) {
        try {
            return new String(Files.readAllBytes(Paths.get(keyPath)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * 读取 PKCS#8 格式的私钥字符串并加载为私钥对象
     *
     * @param keyString 私钥文件内容，以 -----BEGIN PRIVATE KEY----- 开头
     * @return PrivateKey 对象
     */
    public static PrivateKey loadPrivateKeyFromString(String keyString) {
        try {
            keyString = keyString.replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");
            return KeyFactory.getInstance("RSA").generatePrivate(
                    new PKCS8EncodedKeySpec(Base64.getDecoder().decode(keyString)));
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException(e);
        } catch (InvalidKeySpecException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 从 PKCS#8 格式的私钥文件中加载私钥
     *
     * @param keyPath 私钥文件路径
     * @return PrivateKey 对象
     */
    public static PrivateKey loadPrivateKeyFromPath(String keyPath) {
        return loadPrivateKeyFromString(readKeyStringFromPath(keyPath));
    }

    /**
     * 读取 PKCS#8 格式的公钥字符串并加载为公钥对象
     *
     * @param keyString 公钥文件内容，以 -----BEGIN PUBLIC KEY----- 开头
     * @return PublicKey 对象
     */
    public static PublicKey loadPublicKeyFromString(String keyString) {
        try {
            keyString = keyString.replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");
            return KeyFactory.getInstance("RSA").generatePublic(
                    new X509EncodedKeySpec(Base64.getDecoder().decode(keyString)));
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException(e);
        } catch (InvalidKeySpecException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 从 PKCS#8 格式的公钥文件中加载公钥
     *
     * @param keyPath 公钥文件路径
     * @return PublicKey 对象
     */
    public static PublicKey loadPublicKeyFromPath(String keyPath) {
        return loadPublicKeyFromString(readKeyStringFromPath(keyPath));
    }

    /**
     * 创建指定长度的随机字符串，字符集为[0-9a-zA-Z]，可用于安全相关用途
     */
    public static String createNonce(int length) {
        char[] buf = new char[length];
        for (int i = 0; i < length; ++i) {
            buf[i] = SYMBOLS[random.nextInt(SYMBOLS.length)];
        }
        return new String(buf);
    }

    /**
     * 使用公钥按照 RSA_PKCS1_OAEP_PADDING 算法进行加密
     *
     * @param publicKey 加密用公钥对象
     * @param plaintext 待加密明文
     * @return 加密后密文
     */
    public static String encrypt(PublicKey publicKey, String plaintext) {
        final String transformation = "RSA/ECB/OAEPWithSHA-1AndMGF1Padding";

        try {
            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new IllegalArgumentException("The current Java environment does not support " + transformation, e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("RSA encryption using an illegal publicKey", e);
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            throw new IllegalArgumentException("Plaintext is too long", e);
        }
    }

    public static String aesAeadDecrypt(byte[] key, byte[] associatedData, byte[] nonce,
                                        byte[] ciphertext) {
        final String transformation = "AES/GCM/NoPadding";
        final String algorithm = "AES";
        final int tagLengthBit = 128;

        try {
            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    new SecretKeySpec(key, algorithm),
                    new GCMParameterSpec(tagLengthBit, nonce));
            if (associatedData != null) {
                cipher.updateAAD(associatedData);
            }
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (InvalidKeyException
                 | InvalidAlgorithmParameterException
                 | BadPaddingException
                 | IllegalBlockSizeException
                 | NoSuchAlgorithmException
                 | NoSuchPaddingException e) {
            throw new IllegalArgumentException(String.format("AesAeadDecrypt with %s Failed",
                    transformation), e);
        }
    }

    /**
     * 使用私钥按照指定算法进行签名
     *
     * @param message    待签名串
     * @param algorithm  签名算法，如 SHA256withRSA
     * @param privateKey 签名用私钥对象
     * @return 签名结果
     */
    public static String sign(String message, String algorithm, PrivateKey privateKey) {
        byte[] sign;
        try {
            Signature signature = Signature.getInstance(algorithm);
            signature.initSign(privateKey);
            signature.update(message.getBytes(StandardCharsets.UTF_8));
            sign = signature.sign();
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException("The current Java environment does not support " + algorithm, e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException(algorithm + " signature uses an illegal privateKey.", e);
        } catch (SignatureException e) {
            throw new RuntimeException("An error occurred during the sign process.", e);
        }
        return Base64.getEncoder().encodeToString(sign);
    }

    /**
     * 使用公钥按照特定算法验证签名
     *
     * @param message   待签名串
     * @param signature 待验证的签名内容
     * @param algorithm 签名算法，如：SHA256withRSA
     * @param publicKey 验签用公钥对象
     * @return 签名验证是否通过
     */
    public static boolean verify(String message, String signature, String algorithm,
                                 PublicKey publicKey) {
        try {
            Signature sign = Signature.getInstance(algorithm);
            sign.initVerify(publicKey);
            sign.update(message.getBytes(StandardCharsets.UTF_8));
            return sign.verify(Base64.getDecoder().decode(signature));
        } catch (SignatureException e) {
            return false;
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("verify uses an illegal publickey.", e);
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException("The current Java environment does not support" + algorithm, e);
        }
    }

    /**
     * 根据微信支付APIv3请求签名规则构造 Authorization 签名
     *
     * @param mchid               商户号
     * @param certificateSerialNo 商户API证书序列号
     * @param privateKey          商户API证书私钥
     * @param method              请求接口的HTTP方法，请使用全大写表述，如 GET、POST、PUT、DELETE
     * @param uri                 请求接口的URL
     * @param body                请求接口的Body
     * @return 构造好的微信支付APIv3 Authorization 头
     */
    public static String buildAuthorization(String mchid, String certificateSerialNo,
                                            PrivateKey privateKey,
                                            String method, String uri, String body) {
        String nonce = createNonce(32);
        long timestamp = Instant.now().getEpochSecond();

        String message = String.format("%s\n%s\n%d\n%s\n%s\n", method, uri, timestamp, nonce,
                body == null ? "" : body);

        String signature = sign(message, "SHA256withRSA", privateKey);

        return String.format(
                "WECHATPAY2-SHA256-RSA2048 mchid=\"%s\",nonce_str=\"%s\",signature=\"%s\"," +
                        "timestamp=\"%d\",serial_no=\"%s\"",
                mchid, nonce, signature, timestamp, certificateSerialNo);
    }

    /**
     * 计算输入流的哈希值
     *
     * @param inputStream 输入流
     * @param algorithm   哈希算法名称，如 "SHA-256", "SHA-1"
     * @return 哈希值的十六进制字符串
     */
    private static String calculateHash(InputStream inputStream, String algorithm) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            byte[] hashBytes = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException(algorithm + " algorithm not available", e);
        } catch (IOException e) {
            throw new RuntimeException("Error reading from input stream", e);
        }
    }

    /**
     * 计算输入流的 SHA256 哈希值
     *
     * @param inputStream 输入流
     * @return SHA256 哈希值的十六进制字符串
     */
    public static String sha256(InputStream inputStream) {
        return calculateHash(inputStream, "SHA-256");
    }

    /**
     * 计算输入流的 SHA1 哈希值
     *
     * @param inputStream 输入流
     * @return SHA1 哈希值的十六进制字符串
     */
    public static String sha1(InputStream inputStream) {
        return calculateHash(inputStream, "SHA-1");
    }

    /**
     * 计算输入流的 SM3 哈希值
     *
     * @param inputStream 输入流
     * @return SM3 哈希值的十六进制字符串
     */
    public static String sm3(InputStream inputStream) {
        // 确保Bouncy Castle Provider已注册
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        
        try {
            SM3Digest digest = new SM3Digest();
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            byte[] hashBytes = new byte[digest.getDigestSize()];
            digest.doFinal(hashBytes, 0);
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (IOException e) {
            throw new RuntimeException("Error reading from input stream", e);
        }
    }

    /**
     * 对参数进行 URL 编码
     *
     * @param content 参数内容
     * @return 编码后的内容
     */
    public static String urlEncode(String content) {
        try {
            return URLEncoder.encode(content, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 对参数Map进行 URL 编码，生成 QueryString
     *
     * @param params Query参数Map
     * @return QueryString
     */
    public static String urlEncode(Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        boolean isFirstParam = true;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            if (!isFirstParam) {
                result.append("&");
            }
            
            String valueString;
            Object value = entry.getValue();
            // 如果是基本类型、字符串或枚举，直接转换；如果是对象，序列化为JSON
            if (value instanceof String || value instanceof Number || value instanceof Boolean || value instanceof Enum) {
                valueString = value.toString();
            } else {
                valueString = toJson(value);
            }
            
            result.append(entry.getKey())
                    .append("=")
                    .append(urlEncode(valueString));
            isFirstParam = false;
        }
        return result.toString();
    }

    /**
     * 从应答中提取 Body
     *
     * @param response HTTP 请求应答对象
     * @return 应答中的Body内容，Body为空时返回空字符串
     */
    public static String extractBody(Response response) {
        if (response.body() == null) {
            return "";
        }

        try {
            BufferedSource source = response.body().source();
            return source.readUtf8();
        } catch (IOException e) {
            throw new RuntimeException(String.format("An error occurred during reading response body. " +
                    "Status: %d", response.code()), e);
        }
    }

    /**
     * 根据微信支付APIv3应答验签规则对应答签名进行验证，验证不通过时抛出异常
     *
     * @param wechatpayPublicKeyId 微信支付公钥ID
     * @param wechatpayPublicKey   微信支付公钥对象
     * @param headers              微信支付应答 Header 列表
     * @param body                 微信支付应答 Body
     */
    public static void validateResponse(String wechatpayPublicKeyId, PublicKey wechatpayPublicKey,
                                        Headers headers,
                                        String body) {
        String timestamp = headers.get("Wechatpay-Timestamp");
        String requestId = headers.get("Request-ID");
        try {
            Instant responseTime = Instant.ofEpochSecond(Long.parseLong(timestamp));
            // 拒绝过期请求
            if (Duration.between(responseTime, Instant.now()).abs().toMinutes() >= 5) {
                throw new IllegalArgumentException(
                        String.format("Validate response failed, timestamp[%s] is expired, request-id[%s]",
                                timestamp, requestId));
            }
        } catch (DateTimeException | NumberFormatException e) {
            throw new IllegalArgumentException(
                    String.format("Validate response failed, timestamp[%s] is invalid, request-id[%s]",
                            timestamp, requestId));
        }
        String serialNumber = headers.get("Wechatpay-Serial");
        if (!Objects.equals(serialNumber, wechatpayPublicKeyId)) {
            throw new IllegalArgumentException(
                    String.format("Validate response failed, Invalid Wechatpay-Serial, Local: %s, Remote: " +
                            "%s", wechatpayPublicKeyId, serialNumber));
        }

        String signature = headers.get("Wechatpay-Signature");
        String message = String.format("%s\n%s\n%s\n", timestamp, headers.get("Wechatpay-Nonce"),
                body == null ? "" : body);

        boolean success = verify(message, signature, "SHA256withRSA", wechatpayPublicKey);
        if (!success) {
            throw new IllegalArgumentException(
                    String.format("Validate response failed,the WechatPay signature is incorrect.%n"
                                    + "Request-ID[%s]\tresponseHeader[%s]\tresponseBody[%.1024s]",
                            headers.get("Request-ID"), headers, body));
        }
    }

    /**
     * 根据微信支付APIv3通知验签规则对通知签名进行验证，验证不通过时抛出异常
     * @param wechatpayPublicKeyId 微信支付公钥ID
     * @param wechatpayPublicKey 微信支付公钥对象
     * @param headers 微信支付通知 Header 列表
     * @param body 微信支付通知 Body
     */
    public static void validateNotification(String wechatpayPublicKeyId,
                                            PublicKey wechatpayPublicKey, Headers headers,
                                            String body) {
        String timestamp = headers.get("Wechatpay-Timestamp");
        try {
            Instant responseTime = Instant.ofEpochSecond(Long.parseLong(timestamp));
            // 拒绝过期请求
            if (Duration.between(responseTime, Instant.now()).abs().toMinutes() >= 5) {
                throw new IllegalArgumentException(
                        String.format("Validate notification failed, timestamp[%s] is expired", timestamp));
            }
        } catch (DateTimeException | NumberFormatException e) {
            throw new IllegalArgumentException(
                    String.format("Validate notification failed, timestamp[%s] is invalid", timestamp));
        }
        String serialNumber = headers.get("Wechatpay-Serial");
        if (!Objects.equals(serialNumber, wechatpayPublicKeyId)) {
            throw new IllegalArgumentException(
                    String.format("Validate notification failed, Invalid Wechatpay-Serial, Local: %s, " +
                                    "Remote: %s",
                            wechatpayPublicKeyId,
                            serialNumber));
        }

        String signature = headers.get("Wechatpay-Signature");
        String message = String.format("%s\n%s\n%s\n", timestamp, headers.get("Wechatpay-Nonce"),
                body == null ? "" : body);

        boolean success = verify(message, signature, "SHA256withRSA", wechatpayPublicKey);
        if (!success) {
            throw new IllegalArgumentException(
                    String.format("Validate notification failed, WechatPay signature is incorrect.\n"
                                    + "responseHeader[%s]\tresponseBody[%.1024s]",
                            headers, body));
        }
    }


    /**
     * 根据微信支付APIv3通知验签规则对通知签名进行验证，验证不通过时抛出异常
     * @param wechatpayPublicKeyId 微信支付公钥ID
     * @param wechatpayPublicKey 微信支付公钥对象
     * @param request HttpServletRequest
     * @param body 微信支付通知 Body
     */
    public static void validateNotification(String wechatpayPublicKeyId,
                                            PublicKey wechatpayPublicKey, HttpServletRequest request,
                                            String body) {
        String timestamp = request.getHeader("Wechatpay-Timestamp");
        try {
            Instant responseTime = Instant.ofEpochSecond(Long.parseLong(timestamp));
            // 拒绝过期请求
            if (Duration.between(responseTime, Instant.now()).abs().toMinutes() >= 5) {
                throw new IllegalArgumentException(
                        String.format("Validate notification failed, timestamp[%s] is expired", timestamp));
            }
        } catch (DateTimeException | NumberFormatException e) {
            throw new IllegalArgumentException(
                    String.format("Validate notification failed, timestamp[%s] is invalid", timestamp));
        }
        String serialNumber = request.getHeader("Wechatpay-Serial");
        if (!Objects.equals(serialNumber, wechatpayPublicKeyId)) {
            throw new IllegalArgumentException(
                    String.format("Validate notification failed, Invalid Wechatpay-Serial, Local: %s, " +
                                    "Remote: %s",
                            wechatpayPublicKeyId,
                            serialNumber));
        }

        String signature = request.getHeader("Wechatpay-Signature");
        String nonce = request.getHeader("Wechatpay-Nonce");
        String message = String.format("%s\n%s\n%s\n", timestamp, nonce, body == null ? "" : body);

        boolean success = verify(message, signature, "SHA256withRSA", wechatpayPublicKey);
        if (!success) {
            throw new IllegalArgumentException(
                    String.format("Validate notification failed, WechatPay signature is incorrect.\n"
                                    + "responseHeader[%s]\tresponseBody[%.1024s]",
                            message, body));
        }
    }

    /**
     * 对微信支付通知进行签名验证、解析，同时将业务数据解密。验签名失败、解析失败、解密失败时抛出异常
     * @param apiv3Key 商户的 APIv3 Key
     * @param wechatpayPublicKeyId 微信支付公钥ID
     * @param wechatpayPublicKey   微信支付公钥对象
     * @param headers              微信支付请求 Header 列表
     * @param body                 微信支付请求 Body
     * @return 解析后的通知内容，解密后的业务数据可以使用 Notification.getPlaintext() 访问
     */
    public static Notification parseNotification(String apiv3Key, String wechatpayPublicKeyId,
                                                 PublicKey wechatpayPublicKey, Headers headers,
                                                 String body) {
        validateNotification(wechatpayPublicKeyId, wechatpayPublicKey, headers, body);
        Notification notification = gson.fromJson(body, Notification.class);
        notification.decrypt(apiv3Key);
        return notification;
    }

    public static Notification parseNotification(String apiv3Key, String wechatpayPublicKeyId,
                                                 PublicKey wechatpayPublicKey, HttpServletRequest request,
                                                 String body) {
        validateNotification(wechatpayPublicKeyId, wechatpayPublicKey, request, body);
        Notification notification = gson.fromJson(body, Notification.class);
        notification.decrypt(apiv3Key);
        return notification;
    }

    public static Notification decrypt(String apiv3Key, String body) {
        Notification notification = gson.fromJson(body, Notification.class);
        notification.decrypt(apiv3Key);
        return notification;
    }

    /**
     * 微信支付API错误异常，发送HTTP请求成功，但返回状态码不是 2XX 时抛出本异常
     */
    public static class ApiException extends RuntimeException {
        private static final long serialVersionUID = 2261086748874802175L;

        private final int statusCode;
        private final String body;
        private final Headers headers;
        private final String errorCode;
        private final String errorMessage;

        public ApiException(int statusCode, String body, Headers headers) {
            super(String.format("微信支付API访问失败，StatusCode: [%s], Body: [%s], Headers: [%s]", statusCode,
                    body, headers));
            this.statusCode = statusCode;
            this.body = body;
            this.headers = headers;

            if (body != null && !body.isEmpty()) {
                JsonElement code;
                JsonElement message;

                try {
                    JsonObject jsonObject = gson.fromJson(body, JsonObject.class);
                    code = jsonObject.get("code");
                    message = jsonObject.get("message");
                } catch (JsonSyntaxException ignored) {
                    code = null;
                    message = null;
                }
                this.errorCode = code == null ? null : code.getAsString();
                this.errorMessage = message == null ? null : message.getAsString();
            } else {
                this.errorCode = null;
                this.errorMessage = null;
            }
        }

        /**
         * 获取 HTTP 应答状态码
         */
        public int getStatusCode() {
            return statusCode;
        }

        /**
         * 获取 HTTP 应答包体内容
         */
        public String getBody() {
            return body;
        }

        /**
         * 获取 HTTP 应答 Header
         */
        public Headers getHeaders() {
            return headers;
        }

        /**
         * 获取 错误码 （错误应答中的 code 字段）
         */
        public String getErrorCode() {
            return errorCode;
        }

        /**
         * 获取 错误消息 （错误应答中的 message 字段）
         */
        public String getErrorMessage() {
            return errorMessage;
        }
    }

    @Data
    public static class Notification {
        @SerializedName("id")
        private String id;
        @SerializedName("create_time")
        private String createTime;
        @SerializedName("event_type")
        private String eventType;
        @SerializedName("resource_type")
        private String resourceType;
        @SerializedName("summary")
        private String summary;
        @SerializedName("resource")
        private Resource resource;
        private String plaintext;

        private void validate() {
            if (resource == null) {
                throw new IllegalArgumentException("Missing required field `resource` in notification");
            }
            resource.validate();
        }

        /**
         * 使用 APIv3Key 对通知中的业务数据解密，解密结果可以通过 getPlainText 访问。
         * 外部拿到的 Notification 一定是解密过的，因此本方法没有设置为 public
         * @param apiv3Key 商户APIv3 Key
         */
        private void decrypt(String apiv3Key) {
            validate();

            plaintext = aesAeadDecrypt(
                    apiv3Key.getBytes(StandardCharsets.UTF_8),
                    resource.associatedData.getBytes(StandardCharsets.UTF_8),
                    resource.nonce.getBytes(StandardCharsets.UTF_8),
                    Base64.getDecoder().decode(resource.ciphertext)
            );
        }

        public static class Resource {
            @SerializedName("algorithm")
            private String algorithm;

            @SerializedName("ciphertext")
            private String ciphertext;

            @SerializedName("associated_data")
            private String associatedData;

            @SerializedName("nonce")
            private String nonce;

            @SerializedName("original_type")
            private String originalType;

            public String getAlgorithm() {
                return algorithm;
            }

            public String getCiphertext() {
                return ciphertext;
            }

            public String getAssociatedData() {
                return associatedData;
            }

            public String getNonce() {
                return nonce;
            }

            public String getOriginalType() {
                return originalType;
            }

            private void validate() {
                if (algorithm == null || algorithm.isEmpty()) {
                    throw new IllegalArgumentException("Missing required field `algorithm` in Notification" +
                            ".Resource");
                }
                if (!Objects.equals(algorithm, "AEAD_AES_256_GCM")) {
                    throw new IllegalArgumentException(String.format("Unsupported `algorithm`[%s] in " +
                            "Notification.Resource", algorithm));
                }

                if (ciphertext == null || ciphertext.isEmpty()) {
                    throw new IllegalArgumentException("Missing required field `ciphertext` in Notification" +
                            ".Resource");
                }

                if (associatedData == null || associatedData.isEmpty()) {
                    throw new IllegalArgumentException("Missing required field `associatedData` in " +
                            "Notification.Resource");
                }

                if (nonce == null || nonce.isEmpty()) {
                    throw new IllegalArgumentException("Missing required field `nonce` in Notification" +
                            ".Resource");
                }

                if (originalType == null || originalType.isEmpty()) {
                    throw new IllegalArgumentException("Missing required field `originalType` in " +
                            "Notification.Resource");
                }
            }
        }
    }
    /**
     * 根据文件名获取对应的Content-Type
     * @param fileName 文件名
     * @return Content-Type字符串
     */
    public static String getContentTypeByFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "application/octet-stream";
        }

        // 获取文件扩展名
        String extension = "";
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            extension = fileName.substring(lastDotIndex + 1).toLowerCase();
        }

        // 常见文件类型映射
        Map<String, String> contentTypeMap = new HashMap<>();
        // 图片类型
        contentTypeMap.put("png", "image/png");
        contentTypeMap.put("jpg", "image/jpeg");
        contentTypeMap.put("jpeg", "image/jpeg");
        contentTypeMap.put("gif", "image/gif");
        contentTypeMap.put("bmp", "image/bmp");
        contentTypeMap.put("webp", "image/webp");
        contentTypeMap.put("svg", "image/svg+xml");
        contentTypeMap.put("ico", "image/x-icon");

        // 文档类型
        contentTypeMap.put("pdf", "application/pdf");
        contentTypeMap.put("doc", "application/msword");
        contentTypeMap.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        contentTypeMap.put("xls", "application/vnd.ms-excel");
        contentTypeMap.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        contentTypeMap.put("ppt", "application/vnd.ms-powerpoint");
        contentTypeMap.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");

        // 文本类型
        contentTypeMap.put("txt", "text/plain");
        contentTypeMap.put("html", "text/html");
        contentTypeMap.put("css", "text/css");
        contentTypeMap.put("js", "application/javascript");
        contentTypeMap.put("json", "application/json");
        contentTypeMap.put("xml", "application/xml");

        // 音视频类型
        contentTypeMap.put("mp3", "audio/mpeg");
        contentTypeMap.put("wav", "audio/wav");
        contentTypeMap.put("mp4", "video/mp4");
        contentTypeMap.put("avi", "video/x-msvideo");
        contentTypeMap.put("mov", "video/quicktime");

        // 压缩文件类型
        contentTypeMap.put("zip", "application/zip");
        contentTypeMap.put("rar", "application/x-rar-compressed");
        contentTypeMap.put("7z", "application/x-7z-compressed");

        return contentTypeMap.getOrDefault(extension, "application/octet-stream");
    }
}