package com.example.heartapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.example.heartapp.visualizer.LineVisualizer;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    public static final int AUDIO_PERMISSION_REQUEST_CODE = 102;
    public static final int REQUEST_ENABLE_BT = 100;

    public static final String[] WRITE_EXTERNAL_STORAGE_PERMS = {
            Manifest.permission.RECORD_AUDIO
    };

    protected MediaPlayer mediaPlayer;



    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

        Spinner modelspinner = (Spinner) findViewById(R.id.modelSpinner);
        Spinner locationspinner = (Spinner) findViewById(R.id.locationSpinner);
        Spinner connectionspinner = (Spinner) findViewById(R.id.connectionSpinner);
        LinearLayout results = findViewById(R.id.resultContainer);
        results.setClipToOutline(true);
        RelativeLayout visualizerContainer = findViewById(R.id.visualizerContainer);
        visualizerContainer.setClipToOutline(true);

        ArrayAdapter<CharSequence> modeladapter = ArrayAdapter.createFromResource(this,
                R.array.model, R.layout.dropdown_item);
        modelspinner.setAdapter(modeladapter);

        ArrayAdapter<CharSequence> locationadapter = ArrayAdapter.createFromResource(this,
                R.array.location, R.layout.dropdown_item);
        locationspinner.setAdapter(locationadapter);

        ArrayAdapter<CharSequence> speciesadapter = ArrayAdapter.createFromResource(this,
                R.array.connection, R.layout.dropdown_item);
        connectionspinner.setAdapter(speciesadapter);


        LineVisualizer lineVisualizer = findViewById(R.id.visualizer);
        // set custom color to the line.
        lineVisualizer.setColor(ContextCompat.getColor(this, R.color.DarkGreen));
        // set the line with for the visualizer between 1-10 default 1.
        lineVisualizer.setStrokeWidth(1);
        // Set you media player to the visualizer.
        lineVisualizer.setPlayer(mediaPlayer.getAudioSessionId());

        int width = Resources.getSystem().getDisplayMetrics().widthPixels;

        //LinearLayout resultGrid = (LinearLayout) findViewById(R.id.resultContainer);


        if(width > 2000){

            ViewGroup.MarginLayoutParams params1 = (ViewGroup.MarginLayoutParams) results.getLayoutParams();
            params1.width = 1500;
            TextView abnormal = findViewById(R.id.abnormal);
            abnormal.setTextSize(TypedValue.COMPLEX_UNIT_SP,25);
            TextView normal = findViewById(R.id.normal);
            normal.setTextSize(TypedValue.COMPLEX_UNIT_SP,25);
            TextView abnormalOutput = findViewById(R.id.abnormalOutput);
            abnormalOutput.setTextSize(TypedValue.COMPLEX_UNIT_SP,25);
            TextView normalOutput = findViewById(R.id.normalOutput);
            normalOutput.setTextSize(TypedValue.COMPLEX_UNIT_SP,25);

        }

    }







    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
            }
        }
    };




    private void initialize() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(WRITE_EXTERNAL_STORAGE_PERMS, AUDIO_PERMISSION_REQUEST_CODE);
        } else {
            setPlayer();
        }
    }

    public void record(View view){
        Toast.makeText(MainActivity.this, "Recording", Toast.LENGTH_LONG).show();
    }

    public void evaluate(View view){
        Spinner modelSpinner = (Spinner) findViewById(R.id.modelSpinner);
        Spinner locationSpinner = (Spinner) findViewById(R.id.locationSpinner);
        Spinner speciesSpinner = (Spinner) findViewById(R.id.connectionSpinner);
        String model = modelSpinner.getSelectedItem().toString();
        String location = locationSpinner.getSelectedItem().toString();
        String connection = speciesSpinner.getSelectedItem().toString();
        TextView abnormalOutput = findViewById(R.id.abnormalOutput);
        abnormalOutput.setText("64 %");
        TextView normalOutput = (TextView) findViewById(R.id.normalOutput);
        normalOutput.setText("36 %");
        Toast.makeText(MainActivity.this, "Evaluating", Toast.LENGTH_LONG).show();
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
        ImageButton btnPlayPause = (ImageButton) view;
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

    private void connectToBluetoothDevice(){

        // Use this check to determine whether Bluetooth classic is supported on the device.
// Then you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            finish();
        }
// Use this check to determine whether BLE is supported on the device. Then
// you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE not supported", Toast.LENGTH_SHORT).show();
            finish();
        }
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
            }
        }
        int requestCode = 1;
        Intent discoverableIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivityForResult(discoverableIntent, requestCode);





    }

    private void connectToWifiDevice(){
        Toast.makeText(this, "BLE not supported", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver);
    }

}