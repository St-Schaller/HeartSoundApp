package com.example.heartapp;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.view.View;
import android.os.Bundle;
import android.widget.ImageButton;



import com.example.heartapp.visualizer.LineVisualizer;
import com.example.heartapp.visualizer.LineVisualizerActivity;

public class MainActivity extends AppCompatActivity {
    public static final int AUDIO_PERMISSION_REQUEST_CODE = 102;

    public static final String[] WRITE_EXTERNAL_STORAGE_PERMS = {
            Manifest.permission.RECORD_AUDIO
    };

    protected MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void replay(View view) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(0);
        }
    }

    public void playPause(View view) {
        initialize();
        LineVisualizer lineVisualizer = findViewById(R.id.visualizer);
        // set custom color to the line.
        lineVisualizer.setColor(ContextCompat.getColor(this, R.color.custom));

        // set the line with for the visualizer between 1-10 default 1.
        lineVisualizer.setStrokeWidth(5);

        // Set you media player to the visualizer.
        lineVisualizer.setPlayer(mediaPlayer.getAudioSessionId());
        playPauseBtnClicked((ImageButton) view);
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

    private void initialize() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(WRITE_EXTERNAL_STORAGE_PERMS, AUDIO_PERMISSION_REQUEST_CODE);
        } else {
            setPlayer();
        }
    }


    private void setPlayer() {
        mediaPlayer = MediaPlayer.create(this, R.raw.red_e);
        mediaPlayer.setLooping(false);
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


    //protected int getLayout() {return R.layout.activity_line_visualizer;
    // }

    //public void line(View view) {startActivity(LineVisualizerActivity.class);}




    /*public void startActivity(Class clazz) {
        startActivity(new Intent(this, clazz));
    }*/
}