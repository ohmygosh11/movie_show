package com.example.myapplication.activities;

import android.media.browse.MediaBrowser;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.media3.common.ErrorMessageProvider;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;

import com.example.myapplication.R;
import com.example.myapplication.databinding.ActivityVideoPlayerBinding;

public class VideoPlayerActivity extends AppCompatActivity {
    private ActivityVideoPlayerBinding activityVideoPlayerBinding;
    private ExoPlayer exoPlayer;
    private String videoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        activityVideoPlayerBinding = DataBindingUtil.setContentView(this, R.layout.activity_video_player);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        doInitialization();
    }

    private void doInitialization() {
        exoPlayer = new ExoPlayer.Builder(this).build();
        activityVideoPlayerBinding.playerView.setPlayer(exoPlayer);
        videoUrl = getIntent().getStringExtra("episodeUrl");
        MediaItem mediaItem1 = MediaItem.fromUri(videoUrl);
        MediaItem mediaItem2 = MediaItem.fromUri(videoUrl);
        MediaItem mediaItem3 = MediaItem.fromUri(videoUrl);
        MediaItem mediaItem4 = MediaItem.fromUri(videoUrl);
        exoPlayer.addMediaItem(mediaItem1);
        exoPlayer.addMediaItem(mediaItem2);
        exoPlayer.addMediaItem(mediaItem3);
        exoPlayer.addMediaItem(mediaItem4);
        exoPlayer.prepare();
        exoPlayer.play();
    }

    @Override
    protected void onStart() {
        super.onStart();
        exoPlayer.setPlayWhenReady(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        exoPlayer.setPlayWhenReady(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        exoPlayer.release();
    }
}