
package com.agrireach.app.ui.admin;

import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import java.io.File;

import com.agrireach.app.R;

public class OfflineVideoPlayerActivity extends AppCompatActivity {

    public static final String EXTRA_VIDEO_PATH = "video_path";
    public static final String EXTRA_VIDEO_TITLE = "video_title";

    private PlayerView playerView;
    private ExoPlayer player;
    private File videoFile;
    private String videoTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_offline_video_player);

        playerView = findViewById(R.id.offlinePlayerView);

        TextView titleView = findViewById(R.id.offlineVideoTitle);

        String videoPath = getIntent().getStringExtra(EXTRA_VIDEO_PATH);
        videoTitle = getIntent().getStringExtra(EXTRA_VIDEO_TITLE);

        if (videoPath == null || videoPath.trim().isEmpty()) {
            showErrorAndFinish("Video path not provided");
            return;
        }

        videoFile = new File(videoPath);

        if (!videoFile.exists() || !videoFile.isFile()
                || videoFile.length() == 0) {
            showErrorAndFinish("Downloaded video not found");
            return;
        }

        titleView.setText(
                videoTitle == null || videoTitle.trim().isEmpty()
                        ? "Offline Video"
                        : videoTitle
        );
    }

    private void initializePlayer() {

        if (videoFile == null || !videoFile.exists()) {
            showErrorAndFinish("Video file is unavailable");
            return;
        }

        player = new ExoPlayer.Builder(this).build();

        playerView.setPlayer(player);

        MediaItem mediaItem = MediaItem.fromUri(
                Uri.fromFile(videoFile)
        );

        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();
    }

    private void releasePlayer() {

        if (player != null) {
            playerView.setPlayer(null);
            player.release();
            player = null;
        }
    }

    private void showErrorAndFinish(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (videoFile != null && videoFile.exists()) {
            initializePlayer();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        releasePlayer();
    }
}
