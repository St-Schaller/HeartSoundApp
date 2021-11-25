package com.example.heartapp.visualizer;

import androidx.core.content.ContextCompat;
import android.view.View;
import android.widget.ImageButton;

import com.example.heartapp.BaseActivity;
import com.example.heartapp.R;

public class LineVisualizerActivity extends BaseActivity {

    @Override
    protected void init() {
        LineVisualizer lineVisualizer = findViewById(R.id.visualizer);
        // set custom color to the line.
        lineVisualizer.setColor(ContextCompat.getColor(this, R.color.LightGreen));

        // set the line with for the visualizer between 1-10 default 1.
        lineVisualizer.setStrokeWidth(1);

        // Set you media player to the visualizer.
        lineVisualizer.setPlayer(mediaPlayer.getAudioSessionId());
    }

    public void replay(View view) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(0);
        }
    }

    public void playPause(View view) {
        playPauseBtnClicked((ImageButton) view);
    }

    //@Override
    //protected int getLayout() {
        //return R.layout.activity_line_visualizer;
    //}
}
