package com.polo.marco.marcopoloapp.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.polo.marco.marcopoloapp.R;
import com.polo.marco.marcopoloapp.firebase.models.User;

/**
 * Created by kmets on 11/29/2017.
 */

public class CustomDialogActivity extends AppCompatActivity {
    private String id;
    private DatabaseReference databaseUsers = FirebaseDatabase.getInstance().getReference("users");
    private User current = LoginActivity.currentUser;

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

        if (!id.equals(current.getUserId())) {
            if (current.getBlockList().contains(id))
                findViewById(R.id.unblock_found).setVisibility(View.VISIBLE);
            else
                findViewById(R.id.block_found).setVisibility(View.VISIBLE);

            if (current.getFriendsListIds().contains(id))
                findViewById(R.id.unfriend_found).setVisibility(View.VISIBLE);
            else if (!current.getBlockList().contains(id))
                findViewById(R.id.add_found).setVisibility(View.VISIBLE);
        }
    }

    public void onClickAddFound (View view){
        LoginActivity.currentUser.friendsListIds.add(id);
        databaseUsers.child(LoginActivity.currentUser.getUserId()).child("friendsListIds").setValue(LoginActivity.currentUser.friendsListIds);
        finish();
    }

    public void onClickBlockFound (View view){
        LoginActivity.currentUser.blockList.add(id);
        databaseUsers.child(LoginActivity.currentUser.getUserId()).child("blockList").setValue(LoginActivity.currentUser.blockList);

        if (current.friendsListIds.contains(id)) {
            LoginActivity.currentUser.friendsListIds.remove(id);
            databaseUsers.child(LoginActivity.currentUser.getUserId()).child("friendsListIds").setValue(LoginActivity.currentUser.friendsListIds);
        }
        finish();
    }

    public void onClickUnblockFound (View view){
        LoginActivity.currentUser.blockList.remove(id);
        databaseUsers.child(LoginActivity.currentUser.getUserId()).child("blockList").setValue(LoginActivity.currentUser.blockList);
        finish();
    }

    public void onClickUnfriendFound (View view){
        LoginActivity.currentUser.friendsListIds.remove(id);
        databaseUsers.child(LoginActivity.currentUser.getUserId()).child("friendsListIds").setValue(LoginActivity.currentUser.friendsListIds);
        finish();
    }

    public void onClickCancelFound (View view){
        finish();
    }

    public void showAlert(String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setMessage(message);

        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                //just continue here
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        final Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        LinearLayout.LayoutParams positiveButtonLL = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
        positiveButtonLL.width = ViewGroup.LayoutParams.MATCH_PARENT;
        positiveButton.setLayoutParams(positiveButtonLL);
    }

}
