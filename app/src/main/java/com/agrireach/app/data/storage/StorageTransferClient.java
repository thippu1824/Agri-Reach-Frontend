package com.agrireach.app.data.storage;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

public class StorageTransferClient {
    private static final int BUFFER_SIZE = 8192;
    private final ContentResolver contentResolver;
    private final OkHttpClient httpClient;
    private final Handler mainHandler;

    public interface TransferCallback {
        void onProgress(int progress);

        void onSuccess();

        void onFailure(String message);
    }

    public StorageTransferClient(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(0, TimeUnit.MILLISECONDS)
                .callTimeout(0, TimeUnit.MILLISECONDS)
                .build();
    }

    public Call uploadVideo(Uri videoUri, long fileSize, String contentType, String presignedUrl,
                            TransferCallback callback) throws IOException {

        if (videoUri == null || fileSize <= 0) {
            throw new IllegalArgumentException("Invalid video file");
        }
        if (presignedUrl == null || !presignedUrl.startsWith("https://")) {
            throw new IllegalArgumentException("Invalid upload URL");
        }
        MediaType mediaType = MediaType.parse(contentType);
        if (mediaType == null) {
            throw new IllegalArgumentException("Invalid content type");
        }
        RequestBody requestBody = new RequestBody() {
            @Override
            public MediaType contentType() {
                return mediaType;
            }

            @Override
            public long contentLength() {
                return fileSize;
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                try (InputStream inputStream =
                             contentResolver.openInputStream(videoUri)) {
                    if (inputStream == null) {
                        throw new IOException("Unable to open selected video");
                    }
                    byte[] buffer = new byte[BUFFER_SIZE];
                    long uploadedBytes = 0;
                    int bytesRead;
                    int lastReportedProgress = -1;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        if (Thread.currentThread().isInterrupted()) {
                            throw new IOException("Upload cancelled");
                        }
                        sink.write(buffer, 0, bytesRead);
                        uploadedBytes += bytesRead;
                        int progress = (int) (
                                uploadedBytes * 100 / fileSize
                        );
                        if (progress != lastReportedProgress) {
                            lastReportedProgress = progress;
                            mainHandler.post(() ->
                                    callback.onProgress(progress)
                            );
                        }
                    }
                    if (uploadedBytes != fileSize) {
                        throw new IOException(
                                "Video size does not match expected file size"
                        );
                    }
                    sink.flush();
                }
            }
        };
        Request request = new Request.Builder()
                .url(presignedUrl)
                .put(requestBody)
                .header("Content-Type", contentType)
                .build();
        Call call = httpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() ->
                        callback.onFailure(
                                call.isCanceled()
                                        ? "Upload cancelled"
                                        : "Upload failed: " + e.getMessage()
                        )
                );
            }

            @Override
            public void onResponse(
                    Call call,
                    Response response
            ) {
                try (Response ignored = response) {
                    if (response.isSuccessful()) {
                        mainHandler.post(callback::onSuccess);
                    } else {
                        int statusCode = response.code();
                        mainHandler.post(() ->
                                callback.onFailure(
                                        "Storage upload failed. HTTP "
                                                + statusCode
                                )
                        );
                    }
                }
            }
        });
        return call;
    }
}