package com.polo.marco.marcopoloapp.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.login.Login;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.polo.marco.marcopoloapp.R;
import com.polo.marco.marcopoloapp.firebase.models.User;
import com.squareup.picasso.Picasso;

/**
 * Created by kmets on 11/29/2017.
 */

public class CustomDialogActivity extends AppCompatActivity {
    private String id;
    private String name;
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
        name = (extras.getStringExtra("name"));

        TextView title = (TextView) findViewById(R.id.custom_display_name);
        title.setText(name);

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
        FriendsListActivity.changedFriends = true;
        addtoFriendsList(id);
        finish();
    }

    public void onClickBlockFound (View view){
        LoginActivity.currentUser.blockList.add(id);
        databaseUsers.child(LoginActivity.currentUser.getUserId()).child("blockList").setValue(LoginActivity.currentUser.blockList);

        if (current.friendsListIds.contains(id)) {
            LoginActivity.currentUser.friendsListIds.remove(id);
            databaseUsers.child(LoginActivity.currentUser.getUserId()).child("friendsListIds").setValue(LoginActivity.currentUser.friendsListIds);
            FriendsListActivity.changedFriends = true;
            int position = findUser(id);
            LoginActivity.currentUser.friendsList.remove(position);
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
        FriendsListActivity.changedFriends = true;
        int position = findUser(id);
        LoginActivity.currentUser.friendsList.remove(position);
        finish();
    }

    public void onClickCancelFound (View view){
        finish();
    }

    public int findUser(String id){
        for (int i = 0; i < LoginActivity.currentUser.friendsList.size(); i++)
            if (LoginActivity.currentUser.friendsList.get(i).getUserId().equals(id))
                return i;
        return -1;
    }

    public void addtoFriendsList(String id){
        databaseUsers.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {

                }
                else {
                    User retrieved = snapshot.getValue(User.class);
                    LoginActivity.currentUser.friendsList.add(retrieved);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
