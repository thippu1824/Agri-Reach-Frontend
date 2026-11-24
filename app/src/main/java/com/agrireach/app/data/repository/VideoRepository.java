package com.agrireach.app.data.repository;

import android.content.ContentResolver;
import android.net.Uri;

import com.agrireach.app.data.remote.dto.VideoResponse;
import com.agrireach.app.data.storage.StorageTransferClient;
import com.agrireach.app.data.remote.api.VideoApi;
import com.agrireach.app.data.remote.dto.VideoUploadRequest;
import com.agrireach.app.data.remote.dto.VideoUploadResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VideoRepository {
    private final VideoApi videoApi;
    private final StorageTransferClient storageTransferClient;

    public interface UploadCallback {
        void onProgress(int progress);

        void onSuccess(String videoId);

        void onFailure(String message);
    }

    public interface VideoListCallback {
        void onSuccess(List<VideoResponse> videos);
        void onFailure(String message);
    }

    public static class UploadTask {
        private volatile retrofit2.Call<?> apiCall;
        private volatile okhttp3.Call storageCall;
        private volatile boolean cancelled;

        private void setActiveCall(retrofit2.Call<?> call) {
            apiCall = call;
            if (cancelled && call != null) {
                call.cancel();
            }
        }

        private void setActiveCall(okhttp3.Call call) {
            storageCall = call;
            if (cancelled && call != null) {
                call.cancel();
            }
        }

        public void cancel() {
            cancelled = true;
            if (apiCall != null) {
                apiCall.cancel();
            }
            if (storageCall != null) {
                storageCall.cancel();
            }
        }

        public boolean isCancelled() {
            return cancelled;
        }
    }

    public VideoRepository(VideoApi videoApi, ContentResolver contentResolver) {
        this.videoApi = videoApi;
        this.storageTransferClient =
                new StorageTransferClient(contentResolver);
    }

    public UploadTask uploadVideo(Uri videoUri, String title, String description, long fileSize, UploadCallback callback) {
        UploadTask uploadTask = new UploadTask();
        VideoUploadRequest request = new VideoUploadRequest(
                title,
                description,
                "video/mp4",
                fileSize
        );
        // Step 1: Request a presigned upload URL
        Call<VideoUploadResponse> apiCall =
                videoApi.createUploadRequest(request);
        uploadTask.setActiveCall(apiCall);
        apiCall.enqueue(new Callback<VideoUploadResponse>() {
            @Override
            public void onResponse(
                    Call<VideoUploadResponse> call,
                    Response<VideoUploadResponse> response
            ) {
                if (uploadTask.isCancelled()) {
                    return;
                }
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onFailure(
                            "Unable to create upload request. HTTP "
                                    + response.code()
                    );
                    return;
                }
                VideoUploadResponse uploadResponse = response.body();
                String uploadUrl = uploadResponse.getUploadUrl();
                String contentType = uploadResponse.getContentType();
                String videoId = uploadResponse.getVideoId();
                if (uploadUrl == null || contentType == null
                        || videoId == null) {
                    callback.onFailure(
                            "Invalid upload response from server"
                    );
                    return;
                }
                // Step 2: Upload video directly to Cloudflare R2
                try {
                    okhttp3.Call storageCall =
                            storageTransferClient.uploadVideo(
                                    videoUri,
                                    fileSize,
                                    contentType,
                                    uploadUrl,
                                    new StorageTransferClient.TransferCallback() {
                                        @Override
                                        public void onProgress(int progress) {
                                            if (!uploadTask.isCancelled()) {
                                                callback.onProgress(progress);
                                            }
                                        }

                                        @Override
                                        public void onSuccess() {
                                            if (!uploadTask.isCancelled()) {
                                                // Step 3: Confirm upload with backend
                                                confirmVideoUpload(
                                                        videoId,
                                                        uploadTask,
                                                        callback
                                                );
                                            }
                                        }

                                        @Override
                                        public void onFailure(String message) {
                                            if (!uploadTask.isCancelled()) {
                                                callback.onFailure(message);
                                            }
                                        }
                                    }
                            );
                    uploadTask.setActiveCall(storageCall);
                } catch (Exception e) {
                    if (!uploadTask.isCancelled()) {
                        callback.onFailure(
                                "Unable to start video upload: "
                                        + e.getMessage()
                        );
                    }
                }
            }

            @Override
            public void onFailure(
                    Call<VideoUploadResponse> call,
                    Throwable t
            ) {
                if (!uploadTask.isCancelled()) {
                    callback.onFailure(
                            "Backend request failed: " + t.getMessage()
                    );
                }
            }
        });
        return uploadTask;
    }

    // Step 3: Ask backend to verify the uploaded video
    private void confirmVideoUpload(String videoId, UploadTask uploadTask, UploadCallback callback) {
        Call<Void> confirmCall =
                videoApi.confirmUpload(videoId);
        uploadTask.setActiveCall(confirmCall);
        confirmCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(
                    Call<Void> call,
                    Response<Void> response
            ) {
                if (uploadTask.isCancelled()) {
                    return;
                }
                if (response.isSuccessful()) {
                    // Step 4: Entire upload workflow completed
                    callback.onSuccess(videoId);
                } else {
                    callback.onFailure(
                            "Video uploaded to storage, but backend "
                                    + "verification failed. HTTP "
                                    + response.code()
                    );
                }
            }

            @Override
            public void onFailure(
                    Call<Void> call,
                    Throwable t
            ) {
                if (!uploadTask.isCancelled()) {
                    callback.onFailure(
                            "Video uploaded to storage, but backend "
                                    + "confirmation failed: "
                                    + t.getMessage()
                    );
                }
            }
        });
    }

    public void getAdminVideos(VideoListCallback callback) {
        Call<List<VideoResponse>> apiCall =
                videoApi.getAdminVideos();
        apiCall.enqueue(new Callback<List<VideoResponse>>() {
            @Override
            public void onResponse(
                    Call<List<VideoResponse>> call,
                    Response<List<VideoResponse>> response
            ) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure(
                            "Unable to fetch videos. HTTP "
                                    + response.code()
                    );
                }
            }
            @Override
            public void onFailure(
                    Call<List<VideoResponse>> call,
                    Throwable t
            ) {
                callback.onFailure(
                        "Video listing request failed: "
                                + t.getMessage()
                );
            }
        });
    }
}