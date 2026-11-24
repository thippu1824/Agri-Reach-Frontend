package com.agrireach.app.data.remote.dto;

public class VideoResponse {
    private String id;
    private String title;
    private String description;
    private long fileSize;
    private Long durationSeconds;
    private String status;
    private String createdAt;

    public VideoResponse() {
        // Required for JSON deserialization
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public long getFileSize() {
        return fileSize;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}