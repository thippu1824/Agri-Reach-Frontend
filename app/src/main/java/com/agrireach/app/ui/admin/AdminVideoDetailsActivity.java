package com.agrireach.app.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.agrireach.app.R;
import com.agrireach.app.core.network.RetrofitClient;
import com.agrireach.app.data.remote.api.VideoApi;
import com.agrireach.app.data.repository.VideoRepository;

import java.io.File;
import java.util.Locale;

public class AdminVideoDetailsActivity extends AppCompatActivity {
    private TextView videoTitleTextView;
    private TextView videoDescriptionTextView;
    private TextView videoStatusTextView;
    private TextView videoDurationTextView;
    private TextView videoFileSizeTextView;
    private TextView downloadStatusTextView;
    private String videoId;
    private long videoFileSize;
    private Long videoDuration;
    private Button playVideoButton;
    private VideoRepository videoRepository;
    private Button downloadVideoButton;
    private ProgressBar downloadProgressBar;
    private TextView downloadProgressTextView;

    private VideoRepository.DownloadTask activeDownloadTask;
    private boolean isDownloading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_video_details);
        initializeViews();
        initializeRepository();
        if (!readVideoDetails()) {
            finish();
            return;
        }
        displayVideoDetails();
        checkDownloadStatus();
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> finish());
        playVideoButton.setOnClickListener(view -> playVideo());
        downloadVideoButton.setOnClickListener(view -> startVideoDownload());
    }

    private void initializeViews() {
        videoTitleTextView = findViewById(R.id.videoTitleTextView);
        videoDescriptionTextView = findViewById(R.id.videoDescriptionTextView);
        videoStatusTextView = findViewById(R.id.videoStatusTextView);
        videoDurationTextView = findViewById(R.id.videoDurationTextView);
        videoFileSizeTextView = findViewById(R.id.videoFileSizeTextView);
        downloadStatusTextView = findViewById(R.id.downloadStatusTextView);
        playVideoButton = findViewById(R.id.playVideoButton);
        downloadVideoButton = findViewById(R.id.downloadVideoButton);
        downloadProgressBar = findViewById(R.id.downloadProgressBar);
        downloadProgressTextView = findViewById(R.id.downloadProgressTextView);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (videoId != null) {
            checkDownloadStatus();
        }
    }

    private void initializeRepository() {
        VideoApi videoApi = RetrofitClient
                .getInstance()
                .create(VideoApi.class);
        videoRepository = new VideoRepository(
                videoApi,
                getContentResolver()
        );
    }

    private boolean readVideoDetails() {
        videoId = getIntent().getStringExtra("video_id");
        videoFileSize = getIntent().getLongExtra(
                "video_file_size",
                0
        );
        if (getIntent().hasExtra("video_duration")) {
            videoDuration = getIntent().getLongExtra(
                    "video_duration",
                    0
            );
        }
        if (videoId == null || videoId.trim().isEmpty()) {
            return false;
        }
        return true;
    }

    private void displayVideoDetails() {
        String title = getIntent().getStringExtra("video_title");
        String description = getIntent().getStringExtra("video_description");
        String status = getIntent().getStringExtra("video_status");
        videoTitleTextView.setText(
                title == null || title.trim().isEmpty()
                        ? "Untitled Video"
                        : title
        );
        videoDescriptionTextView.setText(
                description == null || description.trim().isEmpty()
                        ? "No description available"
                        : description
        );
        videoStatusTextView.setText(
                "● " + (status == null ? "UNKNOWN" : status.toUpperCase(Locale.ROOT))
        );
        videoDurationTextView.setText(
                formatDuration(videoDuration)
        );
        videoFileSizeTextView.setText(
                formatFileSize(videoFileSize)
        );
    }

    private void checkDownloadStatus() {
        if (videoId == null || isDownloading) {
            return;
        }
        File offlineFile = new File(
                new File(getFilesDir(), "offline-videos"),
                videoId + ".mp4"
        );
        boolean isDownloaded =
                offlineFile.exists()
                        && offlineFile.isFile()
                        && videoFileSize > 0
                        && offlineFile.length() == videoFileSize;
        if (isDownloaded) {
            updateDownloadCompleted();
        } else {
            downloadStatusTextView.setText(
                    "Not downloaded yet"
            );
            downloadStatusTextView.setTextColor(
                    android.graphics.Color.parseColor("#718074")
            );
            downloadVideoButton.setEnabled(true);
            downloadVideoButton.setText("Download Video");
            downloadProgressBar.setVisibility(View.GONE);
            downloadProgressTextView.setVisibility(View.GONE);
        }
    }

    private String formatDuration(Long durationSeconds) {
        if (durationSeconds == null || durationSeconds <= 0) {
            return "Unavailable";
        }
        long hours = durationSeconds / 3600;
        long minutes = (durationSeconds % 3600) / 60;
        long seconds = durationSeconds % 60;
        if (hours > 0) {
            return String.format(
                    Locale.getDefault(),
                    "%d hr %d min",
                    hours,
                    minutes
            );
        }
        return String.format(
                Locale.getDefault(),
                "%d min",
                minutes
        );
    }

    private String formatFileSize(long bytes) {
        if (bytes <= 0) {
            return "Unavailable";
        }
        double sizeInMB = bytes / (1024.0 * 1024.0);
        if (sizeInMB >= 1024) {
            return String.format(
                    Locale.getDefault(),
                    "%.2f GB",
                    sizeInMB / 1024
            );
        }
        return String.format(
                Locale.getDefault(),
                "%.1f MB",
                sizeInMB
        );
    }

    private void playVideo() {
        String status = getIntent().getStringExtra("video_status");
        if (!"ACTIVE".equalsIgnoreCase(status)) {
            Toast.makeText(
                    this,
                    "This video is not available for playback",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }
        File offlineFile = new File(
                new File(getFilesDir(), "offline-videos"),
                videoId + ".mp4"
        );
        // Check whether a complete offline copy exists.
        boolean isDownloaded =
                offlineFile.exists()
                        && offlineFile.isFile()
                        && videoFileSize > 0
                        && offlineFile.length() == videoFileSize;
        if (isDownloaded) {
            Intent intent = new Intent(
                    AdminVideoDetailsActivity.this,
                    OfflineVideoPlayerActivity.class
            );
            intent.putExtra(
                    OfflineVideoPlayerActivity.EXTRA_VIDEO_PATH,
                    offlineFile.getAbsolutePath()
            );
            intent.putExtra(
                    OfflineVideoPlayerActivity.EXTRA_VIDEO_TITLE,
                    getIntent().getStringExtra("video_title")
            );
            startActivity(intent);
            return;
        }
        // No complete local copy: request a fresh R2 playback URL.
        playVideoButton.setEnabled(false);
        playVideoButton.setText("Preparing video...");
        videoRepository.getStreamingUrl(
                videoId,
                new VideoRepository.PlaybackUrlCallback() {
                    @Override
                    public void onSuccess(String playbackUrl) {
                        playVideoButton.setEnabled(true);
                        playVideoButton.setText("Play video");
                        Intent intent = new Intent(
                                AdminVideoDetailsActivity.this,
                                OnlineVideoPlayerActivity.class
                        );
                        intent.putExtra(
                                OnlineVideoPlayerActivity.EXTRA_PLAYBACK_URL,
                                playbackUrl
                        );
                        intent.putExtra(
                                OnlineVideoPlayerActivity.EXTRA_VIDEO_TITLE,
                                getIntent().getStringExtra("video_title")
                        );
                        startActivity(intent);
                    }
                    @Override
                    public void onFailure(String message) {
                        playVideoButton.setEnabled(true);
                        playVideoButton.setText("Play video");
                        Toast.makeText(
                                AdminVideoDetailsActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void startVideoDownload() {
        if (isDownloading) {
            Toast.makeText(
                    this,
                    "Video is already downloading",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }
        String status = getIntent().getStringExtra("video_status");
        if (!"ACTIVE".equalsIgnoreCase(status)) {
            Toast.makeText(
                    this,
                    "Only active videos can be downloaded",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }
        File offlineDirectory = new File(
                getFilesDir(),
                "offline-videos"
        );
        File offlineFile = new File(
                offlineDirectory,
                videoId + ".mp4"
        );
        if (offlineFile.exists()
                && offlineFile.isFile()
                && videoFileSize > 0
                && offlineFile.length() == videoFileSize) {
            updateDownloadCompleted();
            Toast.makeText(
                    this,
                    "Video already downloaded",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }
        isDownloading = true;
        downloadVideoButton.setEnabled(false);
        downloadVideoButton.setText("Downloading...");
        downloadProgressBar.setVisibility(View.VISIBLE);
        downloadProgressBar.setProgress(0);
        downloadProgressTextView.setVisibility(View.VISIBLE);
        downloadProgressTextView.setText("0%");
        activeDownloadTask = videoRepository.downloadVideo(
                getApplicationContext(),
                videoId,
                new VideoRepository.DownloadCallback() {
                    @Override
                    public void onProgress(int progress) {
                        runOnUiThread(() -> {
                            if (isFinishing() || isDestroyed()) {
                                return;
                            }
                            downloadProgressBar.setProgress(progress);
                            downloadProgressTextView.setText(
                                    progress + "%"
                            );
                        });
                    }
                    @Override
                    public void onSuccess(File downloadedFile) {
                        runOnUiThread(() -> {
                            if (isFinishing() || isDestroyed()) {
                                return;
                            }
                            isDownloading = false;
                            activeDownloadTask = null;
                            boolean isValidDownload =
                                    downloadedFile != null
                                            && downloadedFile.exists()
                                            && downloadedFile.isFile()
                                            && videoFileSize > 0
                                            && downloadedFile.length() == videoFileSize;
                            if (isValidDownload) {
                                updateDownloadCompleted();
                                Toast.makeText(
                                        AdminVideoDetailsActivity.this,
                                        "Video downloaded successfully",
                                        Toast.LENGTH_LONG
                                ).show();
                            } else {
                                downloadVideoButton.setEnabled(true);
                                downloadVideoButton.setText("Retry Download");
                                downloadStatusTextView.setText(
                                        "Download incomplete. Please retry."
                                );
                                downloadProgressTextView.setText(
                                        "Download verification failed"
                                );
                                Toast.makeText(
                                        AdminVideoDetailsActivity.this,
                                        "Downloaded file is incomplete",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        });
                    }
                    @Override
                    public void onFailure(String message) {
                        runOnUiThread(() -> {
                            if (isFinishing() || isDestroyed()) {
                                return;
                            }
                            isDownloading = false;
                            activeDownloadTask = null;
                            downloadVideoButton.setEnabled(true);
                            downloadVideoButton.setText("Retry Download");
                            downloadProgressBar.setVisibility(View.GONE);
                            downloadProgressTextView.setVisibility(View.GONE);
                            Toast.makeText(
                                    AdminVideoDetailsActivity.this,
                                    message,
                                    Toast.LENGTH_LONG
                            ).show();
                        });
                    }
                }
        );
        if (activeDownloadTask == null) {
            isDownloading = false;
            downloadVideoButton.setEnabled(true);
            downloadVideoButton.setText("Download Video");
            downloadProgressBar.setVisibility(View.GONE);
            downloadProgressTextView.setVisibility(View.GONE);
            Toast.makeText(
                    this,
                    "Unable to start download",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void updateDownloadCompleted() {
        isDownloading = false;
        activeDownloadTask = null;
        downloadVideoButton.setEnabled(false);
        downloadVideoButton.setText("Downloaded");
        downloadProgressBar.setVisibility(View.VISIBLE);
        downloadProgressBar.setProgress(100);
        downloadProgressTextView.setVisibility(View.VISIBLE);
        downloadProgressTextView.setText("100%");
        downloadStatusTextView.setText(
                "✓ Available offline on this device"
        );
        downloadStatusTextView.setTextColor(
                android.graphics.Color.parseColor("#287D43")
        );
    }
}