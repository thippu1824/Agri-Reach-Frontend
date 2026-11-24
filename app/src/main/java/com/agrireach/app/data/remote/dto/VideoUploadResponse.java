package com.agrireach.app.data.remote.dto;

public class VideoUploadResponse {
    private String videoId;
    private String uploadUrl;
    private int expiresInMinutes;
    private String contentType;

    public VideoUploadResponse() {
    }

    public String getVideoId() {
        return videoId;
    }

    public String getUploadUrl() {
        return uploadUrl;
    }

    public int getExpiresInMinutes() {
        return expiresInMinutes;
    }

    public String getContentType() {
        return contentType;
    }
}