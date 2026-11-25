package com.agrireach.app.ui.admin;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.agrireach.app.R;

public class OnlineVideoPlayerActivity extends AppCompatActivity {
    public static final String EXTRA_PLAYBACK_URL = "playback_url";
    public static final String EXTRA_VIDEO_TITLE = "video_title";
    private PlayerView playerView;
    private TextView titleView;
    private ExoPlayer player;
    private String playbackUrl;
    private String videoTitle;
    private boolean playbackErrorShown = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_video_player);
        playerView = findViewById(R.id.onlinePlayerView);
        titleView = findViewById(R.id.onlineVideoTitle);
        playbackUrl = getIntent().getStringExtra(EXTRA_PLAYBACK_URL);
        videoTitle = getIntent().getStringExtra(EXTRA_VIDEO_TITLE);
        if (playbackUrl == null || playbackUrl.trim().isEmpty()) {
            showErrorAndFinish("Playback URL not provided");
            return;
        }
        if (!playbackUrl.startsWith("https://")) {
            showErrorAndFinish("Invalid secure playback URL");
            return;
        }
        titleView.setText(
                videoTitle == null || videoTitle.trim().isEmpty()
                        ? "Video Player"
                        : videoTitle
        );
    }

    private void initializePlayer() {
        if (player != null || isFinishing() || isDestroyed()) {
            return;
        }
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        player.addListener(new Player.Listener() {
            @Override
            public void onPlayerError(PlaybackException error) {
                if (!playbackErrorShown) {
                    playbackErrorShown = true;
                    Toast.makeText(
                            OnlineVideoPlayerActivity.this,
                            "Unable to play video. Please check your connection.",
                            Toast.LENGTH_LONG
                    ).show();
                }
            }
        });
        MediaItem mediaItem = MediaItem.fromUri(playbackUrl);
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
        if (playbackUrl != null && !playbackUrl.trim().isEmpty()) {
            initializePlayer();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        releasePlayer();
    }
}