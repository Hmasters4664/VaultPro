package com.example.vaultpro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.example.vaultpro.objects.SharedPrefManager;

import java.util.Date;

public abstract class base extends AppCompatActivity {
private long lastActivity;
protected int icon;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        //icon = 5;
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(this);
        String themeName = pref.getString("theme", "AppTheme");
        if (themeName.equals("Night")) {
            setTheme(R.style.Night);
            icon=0;
        } else if (themeName.equals("Orange")) {
            setTheme(R.style.Orange);
            icon=1;
        }else if (themeName.equals("Pink")) {
            setTheme(R.style.Pink);
            icon=2;
        }else if (themeName.equals("AppTheme")) {
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        lastActivity = new Date().getTime();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        lastActivity = new Date().getTime();
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onResume() {
        long now = new Date().getTime();
        if (now - lastActivity > 300000) {
            SharedPrefManager.getInstance(this).LogOut();
            Intent i = new Intent(getBaseContext(), register.class);
            startActivity(i);
        }
        super.onResume();
    }

    public void out()
    {
        SharedPrefManager.getInstance(this).LogOut();
    }
}
