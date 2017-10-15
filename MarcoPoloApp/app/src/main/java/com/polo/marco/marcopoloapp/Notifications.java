package com.polo.marco.marcopoloapp;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.support.v7.app.AppCompatActivity;

public class Notifications extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.notifications);

        DisplayMetrics dispMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dispMetrics);
        int width = dispMetrics.widthPixels;
        int height = dispMetrics.heightPixels;

        getWindow().setLayout((int) (width * 0.8), (int) (height * 0.5));
    }
}
