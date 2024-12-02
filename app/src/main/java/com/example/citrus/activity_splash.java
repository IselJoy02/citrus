package com.example.citrus;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class activity_splash extends AppCompatActivity {

    private static final int SPLASH_TIMEOUT = 3000; // Duration in milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Navigate to MainActivity after the timeout
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(activity_splash.this, MainActivity.class);
                startActivity(intent);
                finish(); // Finish the splash activity
            }
        }, SPLASH_TIMEOUT);
    }
}