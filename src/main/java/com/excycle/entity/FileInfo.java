package com.excycle.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FileInfo {

    public FileInfo(String downloadUrl, String fileName, Long fileSize, String fileType) {
        this.downloadUrl = downloadUrl;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileType = fileType;
    }

    public FileInfo(String fileId, String downloadUrl, String fileName, Long fileSize, String fileType) {
        this.fileId = fileId;
        this.downloadUrl = downloadUrl;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileType = fileType;
    }

    private String fileId;

    private String downloadUrl;

    private String fileName;

    private Long fileSize;

    private String fileType;
}
