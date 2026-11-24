package com.agrireach.app.data.remote.dto;

public class VideoUploadRequest {
    private String title;
    private String description;
    private String contentType;
    private Long fileSize;

    public VideoUploadRequest() {
    }

    public VideoUploadRequest(
            String title,
            String description,
            String contentType,
            Long fileSize
    ) {
        this.title = title;
        this.description = description;
        this.contentType = contentType;
        this.fileSize = fileSize;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
}