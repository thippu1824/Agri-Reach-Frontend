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
import android.content.Context;
import com.agrireach.app.data.remote.dto.VideoDownloadResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
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



    public Call downloadVideo(
            Context context,
            String videoId,
            String downloadUrl,
            long expectedFileSize,
            TransferCallback callback
    ) throws IOException {

        if (context == null || videoId == null || downloadUrl == null
                || !downloadUrl.startsWith("https://")) {
            throw new IllegalArgumentException("Invalid download parameters");
        }

        File directory = new File(context.getFilesDir(), "offline-videos");

        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Unable to create offline directory");
        }

        File finalFile = new File(directory, videoId + ".mp4");
        File tempFile = new File(directory, videoId + ".mp4.part");

        if (finalFile.exists()
                && finalFile.length() == expectedFileSize) {
            mainHandler.post(callback::onSuccess);
            return null;
        }

        Request request = new Request.Builder()
                .url(downloadUrl)
                .get()
                .build();

        Call call = httpClient.newCall(request);

        call.enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                tempFile.delete();

                mainHandler.post(() ->
                        callback.onFailure(
                                call.isCanceled()
                                        ? "Download cancelled"
                                        : "Download failed: " + e.getMessage()
                        )
                );
            }

            @Override
            public void onResponse(Call call, Response response) {

                try (Response ignored = response) {

                    if (!response.isSuccessful() || response.body() == null) {
                        tempFile.delete();
                        int status = response.code();

                        mainHandler.post(() ->
                                callback.onFailure(
                                        "Download failed. HTTP " + status
                                )
                        );
                        return;
                    }

                    long totalBytes = response.body().contentLength();

                    try (
                            java.io.InputStream input =
                                    response.body().byteStream();
                            OutputStream output =
                                    new FileOutputStream(tempFile)
                    ) {

                        byte[] buffer = new byte[BUFFER_SIZE];
                        long downloaded = 0;
                        int bytesRead;
                        int lastProgress = -1;

                        while ((bytesRead = input.read(buffer)) != -1) {

                            if (call.isCanceled()) {
                                throw new IOException("Download cancelled");
                            }

                            output.write(buffer, 0, bytesRead);
                            downloaded += bytesRead;

                            long total = expectedFileSize > 0
                                    ? expectedFileSize : totalBytes;

                            if (total > 0) {
                                int progress = (int) (downloaded * 100 / total);

                                if (progress != lastProgress) {
                                    lastProgress = progress;
                                    mainHandler.post(() ->
                                            callback.onProgress(progress)
                                    );
                                }
                            }
                        }

                        output.flush();

                        if (expectedFileSize > 0
                                && downloaded != expectedFileSize) {
                            throw new IOException(
                                    "Downloaded file size mismatch"
                            );
                        }

                        if (!tempFile.renameTo(finalFile)) {
                            throw new IOException(
                                    "Unable to save downloaded video"
                            );
                        }

                        mainHandler.post(callback::onSuccess);

                    } catch (IOException e) {
                        tempFile.delete();

                        mainHandler.post(() ->
                                callback.onFailure(e.getMessage())
                        );
                    }

                }
            }
        });

        return call;
    }


}