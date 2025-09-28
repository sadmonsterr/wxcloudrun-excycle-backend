package com.excycle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;


import java.util.List;

/**
 * 批量文件下载请求DTO
 */
@Schema(description = "批量文件下载请求")
public class BatchFileDownloadRequest {

    @NotEmpty(message = "文件ID列表不能为空")
    @NotNull(message = "文件ID列表不能为null")
    @Schema(description = "云存储文件ID列表", example = "[\"cloudpath/file1.jpg\", \"cloudpath/file2.pdf\"]", required = true)
    private List<String> fileIds;

    public List<String> getFileIds() {
        return fileIds;
    }

    public void setFileIds(List<String> fileIds) {
        this.fileIds = fileIds;
    }
}