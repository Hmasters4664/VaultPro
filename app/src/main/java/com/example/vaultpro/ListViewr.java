package com.example.vaultpro;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.vaultpro.Adapters.PasswordAdapter;
import com.example.vaultpro.objects.Password;
import com.example.vaultpro.objects.SharedPrefManager;
import com.example.vaultpro.touchlisteners.RecyclerTouchListener;
import android.widget.PopupMenu;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import java.io.File;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class ListViewr extends base {

    private SQLiteDatabase database;
    private String passwords;
    private String category;
    private PasswordAdapter PassA;
    private List<Password> PList;
    private RecyclerView mrecyler;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private LinearLayoutManager linear;
    private String location;
    private String key;
    private Boolean done;
    private static final String TAG="ERROR";
    int LAUNCH_SECOND_ACTIVITY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_viewr);
        if (!SharedPrefManager.getInstance(this).isLoggedIn()) {
            Intent intent = new Intent(ListViewr.this, register.class);
            startActivity(intent);
            finish();

        }
        done=false;

        Bundle extras = getIntent().getExtras();
        firebasesetup();

        if(extras !=null)
        {
            passwords = extras.getString("password");
            category = extras.getString("category");
        }
        initializePass();
        mrecyler= findViewById(R.id.Pass_List);
        PList = new ArrayList<>();
        PList.clear();
        PassA = new PasswordAdapter(PList);
        linear = new LinearLayoutManager(getBaseContext());
        mrecyler.setLayoutManager(linear);
        mrecyler.setAdapter(PassA);
        showpassword();
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation2);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.new_cat:
                        Intent intent = new Intent(ListViewr.this, PasswordCreation.class);
                        intent.putExtra("category",category);
                        intent.putExtra("password", passwords);
                        intent.putExtra("edit",false );
                        startActivity(intent);

                        break;
                    case R.id.settings:
                        Intent g = new Intent(getBaseContext(), Settings.class);
                        startActivity(g);
                        break;
                    case R.id.exit_nav:
                       out();
                        Intent i = new Intent(getBaseContext(), register.class);
                        startActivity(i);
                        finish();
                        break;
                }
                return true;
            }
        });

        mrecyler.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mrecyler, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Intent intent = new Intent(ListViewr.this, PasswordCreation.class);
                intent.putExtra("category",category);
                intent.putExtra("password", passwords);
                intent.putExtra("id", PList.get(position).getRowid());
                intent.putExtra("edit",true );
                startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position) {
                final String pass  = PList.get(position).getPassword();
                final String usr  = PList.get(position).getUsername();
                PopupMenu popup = new PopupMenu(ListViewr.this, view);
                popup.getMenuInflater().inflate(R.menu.pop_up, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        String choice = item.getTitle().toString();
                        switch (choice) {
                            case "Copy to Clipboard":
                                clipboard(pass);
                                return true;
                            case "Push to Browser":
                                transmit(pass,usr);
                                return true;

                            case "Scan browser barcode":
                                newapp();
                                return true;

                            default:
                                return false;
                        }
                    }
                });

                popup.show();//showing popup menu




            }
        }));


    }

    private void firebasesetup()
    {
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
                                Toast.makeText(ListViewr.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
        }
        db = FirebaseFirestore.getInstance();

    }
    private void clipboard(String pass)
    {
        Toast.makeText(ListViewr.this, "Copied to clipboard", Toast.LENGTH_SHORT).show();

        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("password", pass);
        assert clipboard != null;
        clipboard.setPrimaryClip(clip);
    }

    private void newapp()
    {

        Intent i = new Intent(getBaseContext(), Snap.class);
        startActivityForResult(i, LAUNCH_SECOND_ACTIVITY);
    }
    private void initializePass()
    {
        File databaseFile = getDatabasePath("passwordfile.db");
        database = SQLiteDatabase.openOrCreateDatabase(databaseFile,passwords, null);
    }
    private void showpassword()
    {
        String query = "SELECT title, username, website, password, notes, category, ROWID FROM passwords WHERE category=?";
        Cursor cursor = database.rawQuery(query, new String[] {category});
        PList.clear();

        try {
            if (cursor.moveToFirst()) {
                do {
                    Password ct = new Password();
                    ct.setRowid(cursor.getLong(cursor.getColumnIndex("rowid")));
                    ct.setTitle(cursor.getString(cursor.getColumnIndex("title")));
                    ct.setUsername(cursor.getString(cursor.getColumnIndex("username")));
                    ct.setWebsite(cursor.getString(cursor.getColumnIndex("website")));
                    ct.setPassword(cursor.getString(cursor.getColumnIndex("password")));

                    PList.add(ct);
                    PassA.notifyDataSetChanged();
                    mrecyler.smoothScrollToPosition(PassA.getItemCount()-1);

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get posts from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

    }
    @Override
    public void onResume(){
        super.onResume();
        Bundle extras = getIntent().getExtras();

        if(extras !=null)
        {
            passwords = extras.getString("password");
            category = extras.getString("category");
        }


    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Bundle extras = getIntent().getExtras();

        if(extras !=null)
        {
            passwords = extras.getString("password");
            category = extras.getString("category");
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LAUNCH_SECOND_ACTIVITY) {
            if(resultCode == Activity.RESULT_OK){
                location=data.getStringExtra("location");
                key=data.getStringExtra("key");
                done=true;
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
                done=false;
            }
        }
    }

    void transmit(String Password, String user)
    {
        if(done) {
            String t;
            t = Encrypt(key, Password);
            long time = System.currentTimeMillis();
            Map<String, Object> docData = new HashMap<>();
            docData.put("Password", t);
            docData.put("URL", "CA");
            docData.put("username", user);
            docData.put("seconds", time);
            db.collection("browsers").document(location).set(docData);
        }else{
            newapp();
        }

    }

    String Encrypt(String key,String text)
    {
        try {
            byte[] salt = new String("12345678").getBytes("Utf8");
            int iterationCount = 2048;
            int keyStrength = 256;
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(key.toCharArray(), salt, iterationCount, keyStrength);
            SecretKey tmp = factory.generateSecret(spec);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            // encrypt the text
            cipher.init(Cipher.ENCRYPT_MODE, tmp);
            byte[] encrypted = cipher.doFinal(text.getBytes());
            byte[] iv = cipher.getIV();

            return Base64.encodeToString(encrypted,Base64.DEFAULT)+"_"+Base64.encodeToString(iv,Base64.DEFAULT);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return e.getMessage();
        }
    }

}
