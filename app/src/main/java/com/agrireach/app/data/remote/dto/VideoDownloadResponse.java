
package com.agrireach.app.data.remote.dto;

public class VideoDownloadResponse {

    private String videoId;
    private String downloadUrl;
    private int expiresInMinutes;
    private long fileSize;
    private String contentType;
    private String title;

    public String getVideoId() {
        return videoId;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public int getExpiresInMinutes() {
        return expiresInMinutes;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getContentType() {
        return contentType;
    }

    public String getTitle() {
        return title;
    }
}
