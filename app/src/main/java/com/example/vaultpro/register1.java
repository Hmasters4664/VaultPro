package com.example.vaultpro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import com.example.vaultpro.objects.SharedPrefManager;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

import net.sqlcipher.database.SQLiteDatabase;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

public class register1 extends base {

    private EditText password, retype;
    private TextView textViewPasswordStrengthIndiactor;
    private Button login;
    private String passwords;
    private SQLiteDatabase database;
    private SQLiteDatabase category;
    private String sh, sh2;
    private int counter;
    private ImageView icn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register1);
        icn =findViewById(R.id.imageView2);
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(this);
        String themeName = pref.getString("theme", "AppTheme");
        if (themeName.equals("Night")) {
            icn.setImageResource(R.drawable.icon_dark);
        } else if (themeName.equals("Orange")) {
            icn.setImageResource(R.drawable.icon_orange);
        }else if (themeName.equals("Pink")) {
            icn.setImageResource(R.drawable.icon_pink);
        }else if (themeName.equals("AppTheme")) {
            icn.setImageResource(R.drawable.icon_true);
        }
        password = findViewById(R.id.input_password);
        retype = findViewById(R.id.input_password_retype);
        textViewPasswordStrengthIndiactor = findViewById(R.id.strength);
        login = findViewById(R.id.button);


        password.addTextChangedListener(new TextWatcher() {

            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
                // When No Password Entered
                //textViewPasswordStrengthIndiactor.setText("");
            }

            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            public void afterTextChanged(Editable s)
            {
                if(s.length()>=16) {
                    textViewPasswordStrengthIndiactor.setText("STRONG");
                    textViewPasswordStrengthIndiactor.setTextColor(getResources().getColor(R.color.green));
                }
                else if(s.length()>10 && s.length()<16 ) {
                    textViewPasswordStrengthIndiactor.setText("MEDIUM");
                    textViewPasswordStrengthIndiactor.setTextColor(getResources().getColor(R.color.yellow));
                }
                else if(s.length()<=10) {
                    textViewPasswordStrengthIndiactor.setText("WEAK");
                    textViewPasswordStrengthIndiactor.setTextColor(getResources().getColor(R.color.red));
                }

            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int a=0;

                if (check()) {
                    if (!SharedPrefManager.getInstance(getApplicationContext()).isInitialized()) {
                        InitializeCategories();
                        InitializeSQLCipher();
                        SharedPrefManager.getInstance(getApplicationContext()).Initialize();
                        try {
                            sh = hash(password.getText().toString().trim());
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        }
                        SharedPrefManager.getInstance(getApplicationContext()).accessed(sh);
                        login();
                    }
                } else {
                    textViewPasswordStrengthIndiactor.setText("Passwords do not match!!");
                    textViewPasswordStrengthIndiactor.setTextColor(getResources().getColor(R.color.red));
                }



            }
        });
    }

    public boolean check(){
        if(retype.getText().toString().isEmpty() || !(retype.getText().toString().equals(password.getText().toString())))
        {
            return false;
        } else {
            return true;
        }

    }

    public String hash(String input) throws NoSuchAlgorithmException {
        Hasher hasher = Hashing.sha256().newHasher();
        return hasher.putString(input, StandardCharsets.UTF_8).hash()
                .toString();
    }

    private void InitializeSQLCipher() {
        File databaseFile = getDatabasePath("passwordfile.db");
        databaseFile.mkdirs();
        databaseFile.delete();
        database = SQLiteDatabase.openOrCreateDatabase(databaseFile,password.getText().toString().trim(), null);
        database.execSQL("create table passwords(title, username, website, password, notes, category)");
    }
    private void InitializeCategories() {
        File databaseFile = getDatabasePath("categories.db");
        databaseFile.mkdirs();
        databaseFile.delete();
        category = SQLiteDatabase.openOrCreateDatabase(databaseFile, "#!@#$%^&()0987654321", null);
        category.execSQL("create table categories(Name, Colour)");
    }
    private void login()
    {
        Hasher hasher = Hashing.sha256().newHasher();
        try {
            sh2= hash(password.getText().toString().trim());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        sh=SharedPrefManager.getInstance(getApplicationContext()).getHash();

        if(sh.equals(sh2))
        {
            SharedPrefManager.getInstance(getApplicationContext()).LogIn();
            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            intent.putExtra("password", password.getText().toString().trim());
            startActivity(intent);
            finish();
        } else {
            if(counter<3) {
                Toast.makeText(register1.this, "Incorrect Password", Toast.LENGTH_SHORT).show();
                counter++;
            }else{
                Toast.makeText(register1.this, "Deleting database and closing", Toast.LENGTH_LONG).show();
                InitializeSQLCipher();
                InitializeCategories();
                SharedPrefManager.getInstance(getApplicationContext()).deInitialize();
                finish();
            }
        }

    }

}
