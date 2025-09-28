package com.excycle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;

/**
 * 文件下载请求DTO
 */
@Schema(description = "文件下载请求")
public class FileDownloadRequest {

    @NotBlank(message = "文件ID不能为空")
    @Schema(description = "云存储文件ID", example = "cloudpath/file/123.jpg", required = true)
    private String fileId;

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }
}