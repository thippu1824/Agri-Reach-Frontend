package com.agrireach.app.ui.admin;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.ProgressBar;

import com.google.android.material.button.MaterialButton;
import com.agrireach.app.R;
import com.agrireach.app.data.remote.dto.VideoResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private final Map<String, Integer> downloadProgress =
            new HashMap<>();

    private final Set<String> downloadedVideos =
            new HashSet<>();
    public interface VideoDownloadListener {
        void onDownloadClick(VideoResponse video);
    }

    public interface VideoPlayListener {
        void onPlayOfflineClick(VideoResponse video);
    }

    private VideoDownloadListener downloadListener;

    public void setDownloadListener(VideoDownloadListener listener) {
        this.downloadListener = listener;
    }

    private VideoPlayListener playListener;

    public void setPlayListener(VideoPlayListener listener) {
        this.playListener = listener;
    }
    private final List<VideoResponse> videoList = new ArrayList<>();

    public void setVideos(List<VideoResponse> videos) {
        videoList.clear();
        if (videos != null) {
            videoList.addAll(videos);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull VideoViewHolder holder,
            int position
    ) {
        VideoResponse video = videoList.get(position);
        holder.videoTitleTextView.setText(video.getTitle());
        String description = video.getDescription();
        holder.videoDescriptionTextView.setText(
                description == null || description.trim().isEmpty()
                        ? "No description available"
                        : description
        );
        bindStatus(holder.videoStatusTextView, video.getStatus());
        holder.videoDurationTextView.setText(
                "◷  " + formatDuration(video.getDurationSeconds())
        );
        holder.videoFileSizeTextView.setText(
                "▣  " + formatFileSize(video.getFileSize())
        );
        holder.videoUploadDateTextView.setText(
                "↑  Uploaded " + formatDate(video.getCreatedAt())
        );

        String videoId = video.getId();

// Reset UI visibility to avoid RecyclerView reuse issues
        holder.videoDownloadProgressBar.setVisibility(View.GONE);
        holder.videoDownloadProgressBar.setProgress(0);
        holder.videoDownloadProgressText.setVisibility(View.GONE);

        holder.downloadVideoButton.setVisibility(View.VISIBLE);
        holder.playOfflineVideoButton.setVisibility(View.GONE);

        boolean isActive =
                "ACTIVE".equalsIgnoreCase(video.getStatus());

// Check whether this video is already downloaded
        if (downloadedVideos.contains(videoId)) {

            holder.downloadVideoButton.setVisibility(View.GONE);

            holder.playOfflineVideoButton.setVisibility(View.VISIBLE);
            holder.playOfflineVideoButton.setEnabled(true);

        }
// Check whether this video is currently downloading
        else if (downloadProgress.containsKey(videoId)) {

            int progress = downloadProgress.get(videoId);

            holder.downloadVideoButton.setText("Downloading...");
            holder.downloadVideoButton.setEnabled(false);

            holder.videoDownloadProgressBar.setProgress(progress);
            holder.videoDownloadProgressBar.setVisibility(View.VISIBLE);

            holder.videoDownloadProgressText.setText(
                    "Downloading " + progress + "%"
            );

            holder.videoDownloadProgressText.setVisibility(View.VISIBLE);

        }
// Default state
        else {

            holder.downloadVideoButton.setText("Download video");
            holder.downloadVideoButton.setEnabled(isActive);
        }

// Handle download button click
        holder.downloadVideoButton.setOnClickListener(view -> {

            if (!isActive || downloadListener == null) {
                return;
            }

            downloadListener.onDownloadClick(video);
        });

// Handle Play Offline button click
        holder.playOfflineVideoButton.setOnClickListener(view -> {

            if (playListener != null) {
                playListener.onPlayOfflineClick(video);
            }
        });



    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    private void bindStatus(TextView statusView, String status) {
        String normalizedStatus = status == null
                ? "UNKNOWN"
                : status.toUpperCase(Locale.ROOT);
        statusView.setText("● " + normalizedStatus);
        int backgroundColor;
        int textColor;
        switch (normalizedStatus) {
            case "ACTIVE":
                backgroundColor = Color.parseColor("#E5F1E7");
                textColor = Color.parseColor("#287D43");
                break;
            case "PENDING":
                backgroundColor = Color.parseColor("#FFF3DB");
                textColor = Color.parseColor("#A66A00");
                break;
            default:
                backgroundColor = Color.parseColor("#EEEEEE");
                textColor = Color.parseColor("#666666");
                break;
        }
        GradientDrawable background = new GradientDrawable();
        background.setColor(backgroundColor);
        background.setCornerRadius(
                statusView.getResources().getDisplayMetrics().density * 20
        );
        statusView.setBackground(background);
        statusView.setTextColor(textColor);
    }

    private String formatDuration(Long durationSeconds) {
        if (durationSeconds == null || durationSeconds <= 0) {
            return "Duration unavailable";
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
        if (minutes > 0) {
            return String.format(
                    Locale.getDefault(),
                    "%d min",
                    minutes
            );
        }
        return String.format(
                Locale.getDefault(),
                "%d sec",
                seconds
        );
    }

    private String formatFileSize(long bytes) {
        if (bytes <= 0) {
            return "Size unavailable";
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

    private String formatDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return "Date unavailable";
        }
        String[] patterns = {
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'"
        };
        for (String pattern : patterns) {
            try {
                SimpleDateFormat inputFormat =
                        new SimpleDateFormat(pattern, Locale.US);
                inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = inputFormat.parse(dateString);
                if (date != null) {
                    SimpleDateFormat outputFormat =
                            new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                    return outputFormat.format(date);
                }
            } catch (Exception ignored) {
                // Try the next supported date format.
            }
        }
        return dateString;
    }

    public void setDownloadProgress(String videoId, int progress) {
        downloadProgress.put(videoId, progress);
        notifyVideoChanged(videoId);
    }

    public void setDownloaded(String videoId) {
        downloadProgress.remove(videoId);
        downloadedVideos.add(videoId);
        notifyVideoChanged(videoId);
    }

    public void resetDownload(String videoId) {
        downloadProgress.remove(videoId);
        notifyVideoChanged(videoId);
    }

    private void notifyVideoChanged(String videoId) {
        for (int i = 0; i < videoList.size(); i++) {
            if (videoId.equals(videoList.get(i).getId())) {
                notifyItemChanged(i);
                return;
            }
        }
    }
    static class VideoViewHolder extends RecyclerView.ViewHolder {

        TextView videoTitleTextView;
        TextView videoDescriptionTextView;
        TextView videoStatusTextView;
        TextView videoDurationTextView;
        TextView videoFileSizeTextView;
        TextView videoUploadDateTextView;

        MaterialButton downloadVideoButton;
        MaterialButton playOfflineVideoButton;
        ProgressBar videoDownloadProgressBar;
        TextView videoDownloadProgressText;

        VideoViewHolder(@NonNull View itemView) {
            super(itemView);

            videoTitleTextView =
                    itemView.findViewById(R.id.videoTitleTextView);

            videoDescriptionTextView =
                    itemView.findViewById(R.id.videoDescriptionTextView);

            videoStatusTextView =
                    itemView.findViewById(R.id.videoStatusTextView);

            videoDurationTextView =
                    itemView.findViewById(R.id.videoDurationTextView);

            videoFileSizeTextView =
                    itemView.findViewById(R.id.videoFileSizeTextView);

            videoUploadDateTextView =
                    itemView.findViewById(R.id.videoUploadDateTextView);

            downloadVideoButton =
                    itemView.findViewById(R.id.downloadVideoButton);
            playOfflineVideoButton =
                    itemView.findViewById(R.id.playOfflineVideoButton);

            videoDownloadProgressBar =
                    itemView.findViewById(R.id.videoDownloadProgressBar);

            videoDownloadProgressText =
                    itemView.findViewById(R.id.videoDownloadProgressText);
        }
    }
}