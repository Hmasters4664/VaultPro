package com.example.vaultpro;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

public class Snap extends AppCompatActivity {
    private String password;
    private Button btn;
    private TextView txt;
    private SurfaceView sfv;
    private BarcodeDetector  detector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snap);
        btn = findViewById(R.id.bt_barcode);
        txt = findViewById(R.id.tv_barcode);
        sfv = findViewById(R.id.sv_barcode);
        Bundle extras = getIntent().getExtras();
        if(extras !=null)
        {
            password = extras.getString("password");

        } else{
            Intent intent = new Intent(Snap.this, register.class);
            startActivity(intent);
            finish();
        }

        setUP();
    }

    private void setUP() {
        final String[] PERMISSION_STORE = new String[] {"android.permission.CAMERA"};
        detector =
                new BarcodeDetector.Builder(getApplicationContext())
                        .setBarcodeFormats(Barcode.QR_CODE)
                        .build();
        if (!detector.isOperational()) {
            txt.setText("Could not set up the detector!");
            return;
        }
        final CameraSource cam = new CameraSource.Builder(this, detector)
                .setAutoFocusEnabled(true)
                .setRequestedPreviewSize(640 , 480)
                .build();

        sfv.getHolder().addCallback(new SurfaceHolder.Callback() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if ((Build.VERSION.SDK_INT >=23)&&(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
                    requestPermissions(PERMISSION_STORE,0);
                    return;
                }
                try {
                    cam.start(sfv.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cam.stop();
            }
        });

        detector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> codes = detections.getDetectedItems();
                if(codes.size() >0)
                {
                    txt.setText(codes.valueAt(0).displayValue.toString());
                }

            }
        });
    }
}
