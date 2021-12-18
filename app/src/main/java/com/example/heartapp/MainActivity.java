package com.example.heartapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
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
import android.content.res.Resources;
import android.media.MediaPlayer;

import android.net.Uri;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.toolbox.Volley;
import com.example.heartapp.visualizer.LineVisualizer;
import com.obsez.android.lib.filechooser.ChooserDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.channels.Channel;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    public static final int AUDIO_PERMISSION_REQUEST_CODE = 102;
    public static final int REQUEST_ENABLE_BT = 100;

    public static final String[] WRITE_EXTERNAL_STORAGE_PERMS = {
            Manifest.permission.RECORD_AUDIO
    };

    protected MediaPlayer mediaPlayer;
    private BluetoothService bluetoothService = null;
    private WifiP2pManager manager;
    private Channel channel;
    private BroadcastReceiver wifiReceiver;
    private String url = "https://heartsoundsanalysis.herokuapp.com/predict";
    private String filePath = null;
    private LineVisualizer lineVisualizer;


    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();

        IntentFilter bluetoothFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bluetoothReceiver, bluetoothFilter);
        //manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        //channel =  manager.initialize(this, getMainLooper(), null);
        //wifiReceiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        IntentFilter wifiFilter = new IntentFilter();
        wifiFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        wifiFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        wifiFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        wifiFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        Spinner modelspinner = findViewById(R.id.modelSpinner);
        Spinner extractorspinner = findViewById(R.id.extractorSpinner);
        Spinner connectionspinner = findViewById(R.id.connectionSpinner);
        LinearLayout results = findViewById(R.id.resultContainer);
        results.setClipToOutline(true);
        RelativeLayout visualizerContainer = findViewById(R.id.visualizerContainer);
        visualizerContainer.setClipToOutline(true);

        ArrayAdapter<CharSequence> modeladapter = ArrayAdapter.createFromResource(this,
                R.array.model, R.layout.dropdown_item);
        modelspinner.setAdapter(modeladapter);
        ArrayAdapter<CharSequence> extractoradapter = ArrayAdapter.createFromResource(this,
                R.array.extractor, R.layout.dropdown_item);
        extractorspinner.setAdapter(extractoradapter);
        ArrayAdapter<CharSequence> speciesadapter = ArrayAdapter.createFromResource(this,
                R.array.connection, R.layout.dropdown_item);
        connectionspinner.setAdapter(speciesadapter);


        lineVisualizer = findViewById(R.id.visualizer);
        // set custom color to the line.
        lineVisualizer.setColor(ContextCompat.getColor(this, R.color.DarkGreen));
        // set the line with for the visualizer between 1-10 default 1.
        lineVisualizer.setStrokeWidth(5);
        // Set you media player to the visualizer.
        lineVisualizer.setPlayer(mediaPlayer.getAudioSessionId());



        Button btnChooseFile = (Button) findViewById(R.id.btn_choose_file);

        btnChooseFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ChooserDialog(MainActivity.this)
                        .withFilter(false, false, "wav")
                        .withResources(R.string.title_choose_file, R.string.title_choose, R.string.dialog_cancel)
                        .withChosenListener(new ChooserDialog.Result() {
                            @Override
                            public void onChoosePath(String path, File pathFile) {
                                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                                    ImageButton btnPlayPause = findViewById(R.id.ib_play_pause);
                                    mediaPlayer.pause();
                                    btnPlayPause.setImageDrawable(ContextCompat.getDrawable(
                                            MainActivity.this,
                                            R.drawable.ic_play_red_48dp));
                                }
                                Toast.makeText(MainActivity.this, "FILE: " + path +" selected", Toast.LENGTH_SHORT).show();
                                filePath = path;
                                setPlayerWithPath(pathFile);
                            }
                        })
                        .build()
                        .show();
            }
        });

        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        if (height > 1500) {
            ViewGroup.MarginLayoutParams params1 = (ViewGroup.MarginLayoutParams) results.getLayoutParams();
            params1.width = 1500;
            lineVisualizer.setStrokeWidth(3);
            TextView resulttextview = findViewById(R.id.resultstextview);
            resulttextview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
            TextView resultsOutput = findViewById(R.id.resultOutput);
            resultsOutput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
            ImageButton stethoscope = findViewById(R.id.record);
            stethoscope.setScaleType(ImageView.ScaleType.CENTER);
            ImageButton heart = findViewById(R.id.evaluate);
            heart.setScaleType(ImageView.ScaleType.CENTER);

        }

    }

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
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
            setPlayerDefault();
        }
    }

    public void record(View view) {
        Toast.makeText(MainActivity.this, "Recording", Toast.LENGTH_LONG).show();
        Spinner connectionSpinner = findViewById(R.id.connectionSpinner);
        String connection = connectionSpinner.getSelectedItem().toString();
        if (connection == "Bluetooth") {
            connectToBluetoothDevice();
        } else {
            connectToWifiDevice();
        }

    }

    public void evaluate(View view) {
        if(filePath == null){
            Toast.makeText(MainActivity.this, "No File selected!", Toast.LENGTH_SHORT).show();
            return;
        }
        Spinner modelSpinner = findViewById(R.id.modelSpinner);
        Spinner extractorSpinner = findViewById(R.id.extractorSpinner);

        String model = modelSpinner.getSelectedItem().toString();
        String extractor = extractorSpinner.getSelectedItem().toString();
        TextView resultOutput = findViewById(R.id.resultOutput);

        SimpleMultiPartRequest multiPartRequestWithParams = new SimpleMultiPartRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String data = jsonObject.getString("result");
                        if(data.equals("1")){
                            resultOutput.setText("Abnormal");
                        }else if(data.equals("-1")){
                            resultOutput.setText("Normal");
                        }else{
                            resultOutput.setText("Try again");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(MainActivity.this, "ConnectionError", Toast.LENGTH_SHORT).show()){
        };
        // Add the file here
        multiPartRequestWithParams.addFile("audiodata",filePath);

        // Add the params here
        multiPartRequestWithParams.addStringParam("extractor", extractor);
        multiPartRequestWithParams.addStringParam("model", model);

        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        queue.add(multiPartRequestWithParams);
        Toast.makeText(MainActivity.this, "Evaluating", Toast.LENGTH_LONG).show();
    }


    private void setPlayerDefault() {
        mediaPlayer = MediaPlayer.create(this, R.raw.red_e);
        mediaPlayer.setLooping(false);
    }

    private void setPlayerWithPath(File file) {
        mediaPlayer = MediaPlayer.create(this, Uri.fromFile(file));
        mediaPlayer.setLooping(false);
        lineVisualizer.setPlayer(mediaPlayer.getAudioSessionId());
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
                setPlayerDefault();
            } else {
                this.finish();
            }
        }
    }

    private void connectToBluetoothDevice() {

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
        } else if (bluetoothService == null) {
            bluetoothService = new BluetoothService(MainActivity.this, mHandler);
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
        //discoverable for 5 minutes, default 2 minutes
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivityForResult(discoverableIntent, requestCode);
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Toast.makeText(MainActivity.this, "Implement Handler", Toast.LENGTH_SHORT).show();
        }
    };

    private void connectToWifiDevice() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        /*manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                ;
            }

            @Override
            public void onFailure(int reasonCode) {
                ;
            }
        });*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unregister the ACTION_FOUND receiver.
        unregisterReceiver(bluetoothReceiver);
        if(bluetoothService != null){
            bluetoothService.stop();
        }
    }

}