package com.excycle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;


/**
 * 文件上传请求DTO
 */
@Schema(description = "文件上传请求")
public class FileUploadRequest {

    @NotBlank(message = "文件名不能为空")
    @Schema(description = "文件名", example = "business_license.jpg", required = true)
    private String fileName;

    @Schema(description = "文件分类", example = "business_license")
    private String category;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}