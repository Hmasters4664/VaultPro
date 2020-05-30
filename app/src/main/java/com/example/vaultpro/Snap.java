package com.example.vaultpro;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.security.spec.KeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class Snap extends AppCompatActivity {
    private String password;
    private String user;
    private Button btn;
    private TextView txt;
    private SurfaceView sfv;
    private BarcodeDetector  detector;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String TAG="Error";
    String part1 = "";
    String part2 = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snap);
        btn = findViewById(R.id.bt_barcode);
        txt = findViewById(R.id.tv_barcode);
        sfv = findViewById(R.id.sv_barcode);
        Bundle extras = getIntent().getExtras();
        btn.setEnabled(false);


        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user == null) {
            mAuth.signInAnonymously()
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInAnonymously:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInAnonymously:failure", task.getException());
                                Toast.makeText(Snap.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();

                            }

                            // ...
                        }
                    });
        }
        db = FirebaseFirestore.getInstance();

        setUP();
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transmit();
            }
        });

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
                .setRequestedPreviewSize(1600 , 1024)
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
                    //txt.setText(codes.valueAt(0).displayValue);
                    String code = codes.valueAt(0).displayValue;
                    String[] parts = code.split("-");
                    if(parts.length>0) {
                         part1 = parts[0];
                         part2 = parts[1];
                        txt.setText("Scan complete");
                        btn.setEnabled(true);

                    }
                }

            }
        });
    }

   void transmit()
    {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("location",part1);
        returnIntent.putExtra("key",part2);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

}
