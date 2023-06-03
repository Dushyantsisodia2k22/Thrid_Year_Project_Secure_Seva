package com.example.gla_final_2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                SharedPreferences sharedPreferences = getSharedPreferences(login_Activity.PREFS_NAME, 0);

                boolean hasloggedIn= sharedPreferences.getBoolean("hasLoggedIn",false);

                if(hasloggedIn) {

                    Intent iHome = new Intent(MainActivity.this, login_Activity.class);
                    startActivity(iHome);
                    finish();
                }
                else{
                    Intent iHome = new Intent(MainActivity.this, login_Activity.class);
                    startActivity(iHome);
                    finish();
                }

            }
        }, 4000);

    }
}