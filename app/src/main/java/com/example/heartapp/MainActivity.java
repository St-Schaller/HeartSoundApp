package com.example.heartapp;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.View;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;


import com.example.heartapp.visualizer.LineVisualizer;

public class MainActivity extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener {

    public static final int AUDIO_PERMISSION_REQUEST_CODE = 102;

    public static final String[] WRITE_EXTERNAL_STORAGE_PERMS = {
            Manifest.permission.RECORD_AUDIO
    };

    protected MediaPlayer mediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();
        Spinner modelspinner = (Spinner) findViewById(R.id.modelSpinner);
        Spinner locationspinner = (Spinner) findViewById(R.id.locationSpinner);
        Spinner speciesspinner = (Spinner) findViewById(R.id.speciesSpinner);

        ArrayAdapter<CharSequence> modeladapter = ArrayAdapter.createFromResource(this,
                R.array.model, R.layout.dropdown_item);
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modelspinner.setAdapter(modeladapter);
        modelspinner.setOnItemSelectedListener(this);

        ArrayAdapter<CharSequence> locationadapter = ArrayAdapter.createFromResource(this,
                R.array.location, R.layout.dropdown_item);
        locationspinner.setAdapter(locationadapter);
        locationspinner.setOnItemSelectedListener(this);

        ArrayAdapter<CharSequence> speciesadapter = ArrayAdapter.createFromResource(this,
                R.array.species, R.layout.dropdown_item);
        speciesspinner.setAdapter(speciesadapter);
        speciesspinner.setOnItemSelectedListener(this);


    }

    //Performing action onItemSelected and onNothing selected
    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
        switch (position) {
            case 1:
                Toast.makeText(MainActivity.this,position + "Spinner 1", Toast.LENGTH_LONG).show();
                break;
            case 2:
                Toast.makeText(MainActivity.this, position + "Spinner 2", Toast.LENGTH_LONG).show();
                break;
            case 3:
                Toast.makeText(MainActivity.this, position + "Spinner 3", Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub

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

    public void replay(View view) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(0);
        }
    }

    public void playPause(View view) {

        LineVisualizer lineVisualizer = findViewById(R.id.visualizer);
        // set custom color to the line.
        lineVisualizer.setColor(ContextCompat.getColor(this, R.color.colorAccent));

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
}