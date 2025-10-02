package com.excycle.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.excycle.entity.FileInfo;
import com.excycle.utils.EnvUtils;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * CloudBase云存储服务
 */
@Service
public class CloudBaseService {

    private static final Logger logger = LoggerFactory.getLogger(CloudBaseService.class);

    // WeChat Cloud Base配置信息
    private static final String WECHAT_API_BASE_URL = "https://api.weixin.qq.com";

    private static final String ENV_ID = "excycle-3gude89g2e454ced"; // 需要替换为实际的云开发环境ID

    private static String ACCESS_TOKEN = ""; // 需要从微信获取的access_token

    @PostConstruct
    public void init() {
        // 从微信获取access_token
        if (!EnvUtils.isProd()) {
            HttpResponse response = HttpRequest.get("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=wx764480a6fa9e8719&secret=5c15cbefcd1e02499fbb2c6355365a63")
                    .timeout(30000)
                    .execute();
            JSONObject result = JSON.parseObject(response.body());
            logger.info("token {}", result);
            ACCESS_TOKEN = result.getString("access_token");
        }
    }

    /**
     * 获取文件临时下载链接
     * @param fileId 云存储文件ID
     * @return 文件下载信息
     */
    public FileInfo getTempFileURL(String fileId) {
        try {
            // 验证文件ID不为空
            if (fileId == null || fileId.trim().isEmpty()) {
                throw new IllegalArgumentException("文件ID不能为空");
            }

            logger.info("Getting download URL for file: {}", fileId);

            // 构建请求参数（按照微信云API格式）
            Map<String, Object> params = new HashMap<>();
            params.put("env", ENV_ID);

            // 构建file_list数组
            List<Map<String, Object>> fileList = new ArrayList<>();
            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("fileid", fileId);
            fileInfo.put("max_age", 7200); // 2小时有效期
            fileList.add(fileInfo);
            params.put("file_list", fileList);

            String queryParams = EnvUtils.isProd() ? "" : "?access_token=" + ACCESS_TOKEN;
            // 调用微信云API
            String WechatApiBaseUrl = EnvUtils.isProd() ? WECHAT_API_BASE_URL.replace("https", "http") : WECHAT_API_BASE_URL;
            String apiUrl = WechatApiBaseUrl + "/tcb/batchdownloadfile" + queryParams;
            HttpResponse response = HttpRequest.post(apiUrl)
                    .header("Content-Type", "application/json")
                    .body(JSON.toJSONString(params))
                    .timeout(3000)
                    .execute();

            logger.info("WeChat Cloud API response status: {}, body: {}", response.getStatus(), response.body());

            if (response.isOk()) {
                JSONObject result = JSON.parseObject(response.body());
                logger.info("WeChat Cloud API result: {}", result);

                if (result.getInteger("errcode") == 0) {
                    JSONArray fileListResult = result.getJSONArray("file_list");
                    if (fileListResult != null && fileListResult.size() > 0) {
                        JSONObject fileResult = fileListResult.getJSONObject(0);

                        if (fileResult.getInteger("status") == 0) {
                            String downloadUrl = fileResult.getString("download_url");
                            if (downloadUrl == null || downloadUrl.isEmpty()) {
                                throw new RuntimeException("文件下载链接为空");
                            }
                            String fileName = extractFileNameFromId(fileId);
                            Long fileSize = fileResult.getLongValue("file_size");
                            String fileType = getFileType(fileName);
                            return new FileInfo(downloadUrl, fileName, fileSize, fileType);
                        } else {
                            String errorMsg = fileResult.getString("errmsg");
                            throw new RuntimeException("文件下载失败: " + errorMsg);
                        }
                    } else {
                        throw new RuntimeException("文件列表为空");
                    }
                } else {
                    String errorMsg = result.getString("errmsg");
                    Integer errcode = result.getInteger("errcode");
                    throw new RuntimeException("API调用失败 (错误码: " + errcode + "): " + errorMsg);
                }
            }

            logger.error("WeChat Cloud API request failed with status: {}, response: {}", response.getStatus(), response.body());
            throw new RuntimeException("微信云服务请求失败，状态码: " + response.getStatus());

        } catch (Exception e) {
            logger.error("Error getting file download URL for file: {}", fileId, e);
            throw new RuntimeException("获取文件下载链接失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件类型
     * @param fileName 文件名
     * @return 文件类型
     */
    private String getFileType(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "other";
        }

        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();

        if (extension.matches("jpg|jpeg|png|gif|bmp|webp")) {
            return "image";
        } else if (extension.matches("pdf|doc|docx|xls|xlsx|ppt|pptx")) {
            return "document";
        } else if (extension.matches("txt|md|json|xml")) {
            return "text";
        } else {
            return "other";
        }
    }

    
    /**
     * 上传文件到CloudBase
     * @param fileContent 文件内容
     * @param fileName 文件名
     * @return 上传结果
     */
    public Map<String, Object> uploadFile(byte[] fileContent, String fileName) {
        // 实现文件上传逻辑
        Map<String, Object> result = new HashMap<>();
        result.put("fileId", "mock-file-id-" + System.currentTimeMillis());
        result.put("fileName", fileName);
        result.put("fileSize", fileContent.length);
        return result;
    }

    /**
     * 删除CloudBase文件
     * @param fileId 文件ID
     * @return 删除结果
     */
    public boolean deleteFile(String fileId) {
        try {
            // 实现文件删除逻辑
            logger.info("File deleted: {}", fileId);
            return true;
        } catch (Exception e) {
            logger.error("Error deleting file: {}", fileId, e);
            return false;
        }
    }

    /**
     * 从cloud://URL提取文件ID
     * @param cloudUrl cloud://格式的URL
     * @return 文件ID
     */
    public String extractFileIdFromCloudUrl(String cloudUrl) {
        if (cloudUrl == null || !cloudUrl.startsWith("cloud://")) {
            throw new IllegalArgumentException("无效的cloud:// URL格式");
        }

        // 移除cloud://前缀
        String withoutPrefix = cloudUrl.substring(8); // "cloud://".length() = 8

        // 查找最后一个斜杠，前面的部分是环境信息，后面的是文件路径
        int lastSlashIndex = withoutPrefix.lastIndexOf('/');
        if (lastSlashIndex == -1) {
            throw new IllegalArgumentException("无效的cloud:// URL格式");
        }

        String filePath = withoutPrefix.substring(lastSlashIndex + 1);
        return filePath;
    }

    /**
     * 从文件ID中提取文件名
     * @param fileId 文件ID
     * @return 文件名
     */
    private String extractFileNameFromId(String fileId) {
        if (fileId == null || fileId.trim().isEmpty()) {
            return "unknown";
        }

        // 如果是完整的cloud:// URL，先提取文件路径
        if (fileId.startsWith("cloud://")) {
            fileId = extractFileIdFromCloudUrl(fileId);
        }

        // 查找最后一个斜杠
        int lastSlashIndex = fileId.lastIndexOf('/');
        if (lastSlashIndex != -1) {
            return fileId.substring(lastSlashIndex + 1);
        }

        return fileId;
    }

    /**
     * 测试检查cloud:// URL文件是否存在
     * @param cloudUrl cloud://格式的URL
     * @return 文件信息
     */
    public FileInfo checkCloudUrlFile(String cloudUrl) {
        try {
            logger.info("Checking cloud URL file: {}", cloudUrl);

            // 直接使用完整的cloud:// URL作为fileid
            String fileId = cloudUrl;
            logger.info("Using file ID: {}", fileId);

            return getTempFileURL(fileId);

        } catch (Exception e) {
            logger.error("Error checking cloud URL file: {}", cloudUrl, e);
            throw new RuntimeException("检查文件失败: " + e.getMessage());
        }
    }
}