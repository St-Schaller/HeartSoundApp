package com.example.heartapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageButton;

/**
 * BaseActivity that contains common code for all visualizers
 *
 * Created by gautam chibde on 18/11/17.
 */
abstract public class BaseActivity extends AppCompatActivity {
    public static final int AUDIO_PERMISSION_REQUEST_CODE = 102;

    public static final String[] WRITE_EXTERNAL_STORAGE_PERMS = {
            Manifest.permission.RECORD_AUDIO
    };

    protected MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getLayout() != 0) {
            setContentView(getLayout());
        } else {
            throw new NullPointerException("Provide layout file for the activity");
        }
        setActionBar();
        initialize();
    }

    private void initialize() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(WRITE_EXTERNAL_STORAGE_PERMS, AUDIO_PERMISSION_REQUEST_CODE);
        } else {
            setPlayer();
        }
    }

    private void setActionBar() {
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setPlayer() {
        mediaPlayer = MediaPlayer.create(this, R.raw.red_e);
        mediaPlayer.setLooping(false);
        init();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == AUDIO_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setPlayer();
            } else {
                this.finish();
            }
        }
    }

    public void playPauseBtnClicked(ImageButton btnPlayPause) {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                btnPlayPause.setImageDrawable(ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_play_red_48dp));
            } else {
                mediaPlayer.start();
                btnPlayPause.setImageDrawable(ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_pause_red_48dp));
            }
        }
    }

    protected int getLayout() {
        return 0;
    }

    protected abstract void init();
}