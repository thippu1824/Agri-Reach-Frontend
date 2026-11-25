package com.agrireach.app.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agrireach.app.R;
import com.agrireach.app.core.network.RetrofitClient;
import com.agrireach.app.data.remote.api.VideoApi;
import com.agrireach.app.data.remote.dto.VideoResponse;
import com.agrireach.app.data.repository.VideoRepository;
import android.content.Intent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminVideosActivity extends AppCompatActivity {
    private Button backButton;
    private Button allVideosButton;
    private Button activeVideosButton;
    private Button pendingVideosButton;
    private EditText searchVideosEditText;
    private TextView videoCountTextView;
    private TextView emptyVideosTextView;
    private RecyclerView videosRecyclerView;
    private VideoRepository videoRepository;
    private VideoAdapter videoAdapter;
    private final List<VideoResponse> allVideos = new ArrayList<>();
    private String selectedFilter = "ALL";
    // Track active downloads using video ID.
    private final Map<String, VideoRepository.DownloadTask>
            activeDownloads = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_videos);
        initializeViews();
        setupRecyclerView();
        initializeRepository();
        setupClickListeners();
        loadVideos();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        allVideosButton = findViewById(R.id.allVideosButton);
        activeVideosButton = findViewById(R.id.activeVideosButton);
        pendingVideosButton = findViewById(R.id.pendingVideosButton);
        searchVideosEditText =
                findViewById(R.id.searchVideosEditText);
        videoCountTextView =
                findViewById(R.id.videoCountTextView);
        emptyVideosTextView =
                findViewById(R.id.emptyVideosTextView);
        videosRecyclerView =
                findViewById(R.id.videosRecyclerView);
    }


    private void setupRecyclerView() {

        videoAdapter = new VideoAdapter();

        videosRecyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        videosRecyclerView.setAdapter(videoAdapter);
        videosRecyclerView.setHasFixedSize(false);

        // Connect download button with Activity
        videoAdapter.setDownloadListener(video -> {

            if (video == null || video.getId() == null) {
                Toast.makeText(
                        this,
                        "Invalid video information",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            startVideoDownload(video);
        });
        // Connect Play Offline button with Activity
        videoAdapter.setPlayListener(video -> {
            playOfflineVideo(video);
        });

        // Connect video card click with the Activity
        videoAdapter.setDetailsListener(video -> {
            openVideoDetails(video);
        });
    }

    private void openVideoDetails(VideoResponse video) {
        if (video == null || video.getId() == null) {
            Toast.makeText(
                    this,
                    "Invalid video information",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }
        Intent intent = new Intent(
                AdminVideosActivity.this,
                AdminVideoDetailsActivity.class
        );
        intent.putExtra("video_id", video.getId());
        intent.putExtra("video_title", video.getTitle());
        intent.putExtra("video_description", video.getDescription());
        intent.putExtra("video_status", video.getStatus());
        intent.putExtra("video_file_size", video.getFileSize());
        if (video.getDurationSeconds() != null) {
            intent.putExtra(
                    "video_duration",
                    video.getDurationSeconds()
            );
        }
        startActivity(intent);
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

    private void setupClickListeners() {
        backButton.setOnClickListener(view -> finish());
        allVideosButton.setOnClickListener(view -> {
            selectedFilter = "ALL";
            applyFilters();
        });
        activeVideosButton.setOnClickListener(view -> {
            selectedFilter = "ACTIVE";
            applyFilters();
        });
        pendingVideosButton.setOnClickListener(view -> {
            selectedFilter = "PENDING";
            applyFilters();
        });
        searchVideosEditText.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {
                        applyFilters();
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                }
        );
    }

    private void loadVideos() {
        emptyVideosTextView.setText("Loading videos...");
        emptyVideosTextView.setVisibility(View.VISIBLE);
        videosRecyclerView.setVisibility(View.GONE);
        videoRepository.getAdminVideos(
                new VideoRepository.VideoListCallback() {
                    @Override
                    public void onSuccess(List<VideoResponse> videos) {
                        allVideos.clear();
                        if (videos != null) {
                            allVideos.addAll(videos);
                        }
                        applyFilters();
                    }

                    @Override
                    public void onFailure(String message) {
                        allVideos.clear();
                        videoAdapter.setVideos(allVideos);
                        videoCountTextView.setText("0 videos");
                        emptyVideosTextView.setText(
                                "Unable to load videos.\n" +
                                        "Please try again later."
                        );
                        emptyVideosTextView.setVisibility(
                                View.VISIBLE
                        );
                        videosRecyclerView.setVisibility(
                                View.GONE
                        );
                        Toast.makeText(
                                AdminVideosActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void applyFilters() {
        String searchText = searchVideosEditText
                .getText()
                .toString()
                .trim()
                .toLowerCase(Locale.ROOT);
        List<VideoResponse> filteredVideos =
                new ArrayList<>();
        for (VideoResponse video : allVideos) {
            if (video == null) {
                continue;
            }
            String status = video.getStatus() == null
                    ? ""
                    : video.getStatus().toUpperCase(Locale.ROOT);
            boolean matchesFilter =
                    selectedFilter.equals("ALL")
                            || selectedFilter.equals(status);
            String title = video.getTitle() == null
                    ? ""
                    : video.getTitle().toLowerCase(Locale.ROOT);
            String description = video.getDescription() == null
                    ? ""
                    : video.getDescription().toLowerCase(Locale.ROOT);
            boolean matchesSearch =
                    title.contains(searchText)
                            || description.contains(searchText);
            if (matchesFilter && matchesSearch) {
                filteredVideos.add(video);
            }
        }
        videoAdapter.setVideos(filteredVideos);
        restoreDownloadedStates(filteredVideos);
        videoCountTextView.setText(
                "Showing " + filteredVideos.size()
                        + " of " + allVideos.size() + " videos"
        );
        if (filteredVideos.isEmpty()) {
            if (allVideos.isEmpty()) {
                emptyVideosTextView.setText(
                        "No videos uploaded yet."
                );
            } else {
                emptyVideosTextView.setText(
                        "No videos match your search or filter."
                );
            }
            emptyVideosTextView.setVisibility(View.VISIBLE);
            videosRecyclerView.setVisibility(View.GONE);
        } else {
            emptyVideosTextView.setVisibility(View.GONE);
            videosRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void restoreDownloadedStates(List<VideoResponse> videos) {

        File offlineDirectory = new File(
                getFilesDir(),
                "offline-videos"
        );

        if (!offlineDirectory.exists()) {
            return;
        }

        for (VideoResponse video : videos) {

            if (video == null || video.getId() == null) {
                continue;
            }

            File offlineFile = new File(
                    offlineDirectory,
                    video.getId() + ".mp4"
            );

            if (offlineFile.exists()
                    && video.getFileSize() > 0
                    && offlineFile.length() == video.getFileSize()) {

                videoAdapter.setDownloaded(video.getId());
            }
        }
    }

    private void startVideoDownload(VideoResponse video) {

        String videoId = video.getId();

        // Prevent duplicate downloads
        if (activeDownloads.containsKey(videoId)) {
            Toast.makeText(
                    this,
                    "This video is already downloading",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // Check whether the video is already downloaded
        File offlineDirectory = new File(
                getFilesDir(),
                "offline-videos"
        );

        File offlineFile = new File(
                offlineDirectory,
                videoId + ".mp4"
        );

        if (offlineFile.exists()
                && offlineFile.length() == video.getFileSize()
                && video.getFileSize() > 0) {

            videoAdapter.setDownloaded(videoId);

            Toast.makeText(
                    this,
                    "Video already downloaded",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Toast.makeText(
                this,
                "Starting download: " + video.getTitle(),
                Toast.LENGTH_SHORT
        ).show();

        VideoRepository.DownloadTask task =
                videoRepository.downloadVideo(
                        getApplicationContext(),
                        videoId,
                        new VideoRepository.DownloadCallback() {

                            @Override
                            public void onProgress(int progress) {

                                videoAdapter.setDownloadProgress(
                                        videoId,
                                        progress
                                );
                            }

                            @Override
                            public void onSuccess(File downloadedFile) {

                                activeDownloads.remove(videoId);

                                videoAdapter.setDownloaded(videoId);

                                Toast.makeText(
                                        AdminVideosActivity.this,
                                        "Downloaded successfully: "
                                                + video.getTitle(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }

                            @Override
                            public void onFailure(String message) {

                                activeDownloads.remove(videoId);

                                videoAdapter.resetDownload(videoId);

                                Toast.makeText(
                                        AdminVideosActivity.this,
                                        message,
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );

        if (task != null) {
            activeDownloads.put(videoId, task);
        }
    }

    private void playOfflineVideo(VideoResponse video) {

        if (video == null || video.getId() == null) {
            Toast.makeText(
                    this,
                    "Invalid video information",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        File offlineDirectory = new File(
                getFilesDir(),
                "offline-videos"
        );

        File offlineFile = new File(
                offlineDirectory,
                video.getId() + ".mp4"
        );

        // Verify that the downloaded video exists and is complete
        if (!offlineFile.exists()
                || !offlineFile.isFile()
                || offlineFile.length() == 0
                || video.getFileSize() <= 0
                || offlineFile.length() != video.getFileSize()) {

            Toast.makeText(
                    this,
                    "Offline video not found or incomplete. Please download again.",
                    Toast.LENGTH_LONG
            ).show();

            videoAdapter.resetDownload(video.getId());
            return;
        }

        Intent intent = new Intent(
                AdminVideosActivity.this,
                OfflineVideoPlayerActivity.class
        );

        intent.putExtra(
                OfflineVideoPlayerActivity.EXTRA_VIDEO_PATH,
                offlineFile.getAbsolutePath()
        );

        intent.putExtra(
                OfflineVideoPlayerActivity.EXTRA_VIDEO_TITLE,
                video.getTitle()
        );

        startActivity(intent);
    }
}