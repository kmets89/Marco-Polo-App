package com.polo.marco.marcopoloapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.polo.marco.marcopoloapp.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        boolean loggingIn = false;
        if (getIntent() != null) {
            loggingIn = getIntent().getBooleanExtra("loggingIn", false);
        }

        Intent intent = new Intent(this, loggingIn ? MapsActivity.class : LoginActivity.class);
        startActivity(intent);
        finish();
    }

}
