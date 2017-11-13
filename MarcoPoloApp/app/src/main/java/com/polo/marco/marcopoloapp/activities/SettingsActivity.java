package com.polo.marco.marcopoloapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.facebook.login.LoginManager;
import com.polo.marco.marcopoloapp.R;

public class SettingsActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
    }

    public void onClickSignOut(View view) {
        Intent intent = new Intent(this, SplashActivity.class);
        startActivity(intent);
        this.finish();

        if (LoginActivity.currentUser.usingFacebook()) {
            LoginManager.getInstance().logOut();
        } else {
            // log out google
        }
    }
}
