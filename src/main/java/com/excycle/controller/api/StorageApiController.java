package com.excycle.controller.api;

import com.excycle.common.Result;
import com.excycle.entity.FileInfo;
import com.excycle.service.CloudBaseService;
import com.excycle.dto.FileDownloadRequest;
import com.excycle.dto.BatchFileDownloadRequest;
import com.excycle.dto.FileUploadRequest;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * 云存储管理RESTful API
 */
@RestController
@RequestMapping("/api/v1/storage")
public class StorageApiController {

    @Autowired
    private CloudBaseService cloudBaseService;

    /**
     * 获取文件下载链接
     * POST /api/v1/storage/download-url
     */
    @PostMapping("/download-url")
    public Result<FileInfo> getDownloadUrl(@Valid @RequestBody FileDownloadRequest request) {
        FileInfo fileInfo = cloudBaseService.getTempFileURL(request.getFileId());
        return Result.success(fileInfo);
    }

    /**
     * 批量获取文件下载链接
     * POST /api/v1/storage/batch-download-urls
     */
    @PostMapping("/batch-download-urls")
    public Result<List<FileInfo>> getBatchDownloadUrls(@Valid @RequestBody BatchFileDownloadRequest request) {
        List<String> fileIds = request.getFileIds();
        List<FileInfo> resultList = new ArrayList<>();

        for (String fileId : fileIds) {
            FileInfo fileInfo = cloudBaseService.getTempFileURL(fileId);
            fileInfo.setFileId(fileId);
            resultList.add(fileInfo);
        }

        return Result.success(resultList);
    }

    /**
     * 上传文件
     * POST /api/v1/storage/upload
     */
    @PostMapping("/upload")
    public Result<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            byte[] fileContent = file.getBytes();
            String fileName = file.getOriginalFilename();

            Map<String, Object> uploadResult = cloudBaseService.uploadFile(fileContent, fileName);
            return Result.success("文件上传成功", uploadResult);
        } catch (Exception e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     * DELETE /api/v1/storage/{fileId}
     */
    @DeleteMapping("/{fileId}")
    public Result<String> deleteFile(@PathVariable String fileId) {
        try {
            boolean success = cloudBaseService.deleteFile(fileId);
            if (success) {
                return Result.success("文件删除成功");
            } else {
                return Result.error("文件删除失败");
            }
        } catch (Exception e) {
            return Result.error("文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取模拟文件信息（用于测试）
     * GET /api/v1/storage/mock-info/{fileId}
     */
    @GetMapping("/mock-info/{fileId}")
    public Result<Map<String, Object>> getMockFileInfo(@PathVariable String fileId) {
        try {
            // 返回模拟的文件信息用于测试
            Map<String, Object> mockFileInfo = new HashMap<>();
            mockFileInfo.put("downloadUrl", "https://mock-download-url.com/" + fileId);
            mockFileInfo.put("fileName", "business_license_" + fileId + ".jpg");
            mockFileInfo.put("fileSize", 1024000); // 1MB
            mockFileInfo.put("fileType", "image");

            return Result.success(mockFileInfo);
        } catch (Exception e) {
            return Result.error("获取文件信息失败: " + e.getMessage());
        }
    }

    /**
     * 检查cloud:// URL文件是否存在
     * GET /api/v1/storage/check-cloud-url
     */
    @GetMapping("/check-cloud-url")
    public Result<FileInfo> checkCloudUrlFile(@RequestParam String cloudUrl) {
        FileInfo fileInfo = cloudBaseService.checkCloudUrlFile(cloudUrl);
        return Result.success(fileInfo);
    }
}