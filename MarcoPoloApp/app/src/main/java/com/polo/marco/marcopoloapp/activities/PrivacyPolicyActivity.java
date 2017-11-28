package com.polo.marco.marcopoloapp.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.polo.marco.marcopoloapp.R;

public class PrivacyPolicyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.privacy_policy);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}
