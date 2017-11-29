package com.polo.marco.marcopoloapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.polo.marco.marcopoloapp.R;

/**
 * Created by kmets on 11/29/2017.
 */

public class CustomDialogActivity extends AppCompatActivity {
    private String id;
    private DatabaseReference databaseUsers = FirebaseDatabase.getInstance().getReference("users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_custom_dialog);
        //Make this view a pop-up over the previous view with dimensions
        //relative to the parent, goes away when the user click outside
        //the popup window
        DisplayMetrics dispMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dispMetrics);
        int width = dispMetrics.widthPixels;
        int height = dispMetrics.heightPixels;

        getWindow().setLayout((int) (width * 0.8), (int) (height * 0.3));

        Intent extras = getIntent();
        id = (extras.getStringExtra("userId"));
    }

    public void onClickAddFound (View view){
        LoginActivity.currentUser.friendsListIds.add(id);
        databaseUsers.child(LoginActivity.currentUser.getUserId()).child("friendsListIds").setValue(LoginActivity.currentUser.friendsListIds);
        finish();
    }

    public void onClickBlockFound (View view){
        finish();
    }

}
