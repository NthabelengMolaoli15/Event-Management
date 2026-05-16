package com.example.nthabelengmolaoli2333784;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MediaActivity extends AppCompatActivity {

    VideoView videoView;
    Button btnVideo1, btnVideo2, btnVideo3;
    Button btnMusic1, btnMusic2, btnStopMusic;
    Button btnShareVideo, btnShareMusic;
    MediaPlayer mediaPlayer;
    int currentVideoRes = -1;
    int currentMusicRes = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Expo Media");
        }

        videoView = findViewById(R.id.videoView);
        btnVideo1 = findViewById(R.id.btnVideo1);
        btnVideo2 = findViewById(R.id.btnVideo2);
        btnVideo3 = findViewById(R.id.btnVideo3);
        btnMusic1 = findViewById(R.id.btnMusic1);
        btnMusic2 = findViewById(R.id.btnMusic2);
        btnStopMusic = findViewById(R.id.btnStopMusic);
        btnShareVideo = findViewById(R.id.btnShareVideo);
        btnShareMusic = findViewById(R.id.btnShareMusic);

        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);

        btnVideo1.setOnClickListener(v -> playVideo(R.raw.nice));
        btnVideo2.setOnClickListener(v -> playVideo(R.raw.choir));
        btnVideo3.setOnClickListener(v -> playVideo(R.raw.concert));

        btnMusic1.setOnClickListener(v -> playMusic(R.raw.jehova));
        btnMusic2.setOnClickListener(v -> playMusic(R.raw.leribe));
        btnStopMusic.setOnClickListener(v -> stopMusic());

        btnShareVideo.setOnClickListener(v -> {
            if (currentVideoRes != -1) {
                shareMedia("Check out this video from Botho Expo!", currentVideoRes);
            } else {
                Toast.makeText(this, "Play a video first to share", Toast.LENGTH_SHORT).show();
            }
        });

        btnShareMusic.setOnClickListener(v -> {
            if (currentMusicRes != -1) {
                shareMedia("Listen to this music from Botho Expo!", currentMusicRes);
            } else {
                Toast.makeText(this, "Play music first to share", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void playVideo(int resId) {
        stopMusic();
        currentVideoRes = resId;
        String path = "android.resource://" + getPackageName() + "/" + resId;
        videoView.setVideoURI(Uri.parse(path));
        videoView.start();
    }

    private void playMusic(int resId) {
        stopMusic();
        currentMusicRes = resId;
        mediaPlayer = MediaPlayer.create(this, resId);
        if (mediaPlayer != null) {
            mediaPlayer.start();
            Toast.makeText(this, "Playing music...", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopMusic() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void shareMedia(String message, int resId) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Botho Expo Media");
        shareIntent.putExtra(Intent.EXTRA_TEXT, message);
        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.student_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home || id == R.id.menu_go_back) {
            onBackPressed();
            return true;
        } else if (id == R.id.menu_logout) {
            stopMusic();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopMusic();
    }
}
