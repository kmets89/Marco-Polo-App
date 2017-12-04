package com.polo.marco.marcopoloapp.activities;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.polo.marco.marcopoloapp.*;
import com.polo.marco.marcopoloapp.firebase.models.User;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class FriendsListActivity extends AppCompatActivity {

    public static boolean changedFriends = false;
    private DatabaseReference databaseUsers = FirebaseDatabase.getInstance().getReference("users");

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        begin();
    }

    private void begin(){
        if(LoginActivity.currentUser.friendsListIds.size() == 0){
            Toast.makeText(this, "You don't seem to have any friends who use this app!", Toast.LENGTH_LONG).show();
        }else{
            if(LoginActivity.currentUser.friendsList.size() != LoginActivity.currentUser.friendsListIds.size())
                pullFriendsFromDB();
            else
                pullLocalFriends();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        EditText searchBar = (EditText) findViewById(R.id.searchBar);
        searchBar.setText("");

        if (changedFriends) {
            LinearLayout friendsView = (LinearLayout) findViewById(R.id.friends_layout_child);
            friendsView.removeAllViews();
            Log.d("RESUMING", "resetting friendslist");
            pullFriendsFromDB();
            changedFriends = false;
        }
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    public void onClickSearch(View view){
        String searchText = ((EditText)findViewById(R.id.searchBar)).getText().toString();
        if (searchText.equals(""))
            return;

        Intent intent = new Intent(this, SearchResultsActivity.class);
        intent.putExtra("searchText", searchText);
        startActivity(intent);
    }

    public void pullFriendsFromDB(){
        for (int i = 0; i < LoginActivity.currentUser.getFriendsListIds().size(); i++){
            databaseUsers.child(LoginActivity.currentUser.friendsListIds.get(i)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                    }
                    else {
                        final User retrieved = snapshot.getValue(User.class);
                        //LoginActivity.currentUser.friendsList.add(retrieved);
                        Log.d("TESTING", "added friend! " + retrieved.getName());
                        populateView(retrieved);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }

    public void pullLocalFriends(){
        for (int i = 0; i < LoginActivity.currentUser.getFriendsListIds().size(); i++) {
            final User retrieved = LoginActivity.currentUser.friendsList.get(i);
            populateView(retrieved);
        }
    }

    public void populateView(final User retrieved){
        LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View newView = layoutInflater.inflate(R.layout.friends_list_layout, null);

        TextView textView1 = (TextView) newView.findViewById(R.id.friendslist_user_name);
        textView1.setText(retrieved.getName());

        ImageView profilePicView = (ImageView) newView.findViewById(R.id.friendslist_profile_image);
        Picasso.with(FriendsListActivity.this).load(retrieved.getImgUrl()).into(profilePicView);

        TextView emailView = (TextView) newView.findViewById(R.id.friendslist_email_address);
        emailView.setText(retrieved.getEmail());

        LinearLayout friendsView = (LinearLayout) findViewById(R.id.friends_layout_child);
        friendsView.addView(newView);

        newView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(FriendsListActivity.this);
                View view = getLayoutInflater().inflate(R.layout.dialog_select_friends, null);
                TextView txtViewFriend = (TextView) view.findViewById(R.id.txt_friend_name);
                Button btnUnfriend = (Button) view.findViewById(R.id.btn_unfriend);
                Button btnBlock = (Button) view.findViewById(R.id.btn_block);
                Button btnMarco = (Button) view.findViewById(R.id.btn_marco_in_friendslist);

                String id = retrieved.getUserId();

                dialogBuilder.setView(view);
                final AlertDialog dialog = dialogBuilder.create();

                txtViewFriend.setText(retrieved.getName());

                btnUnfriend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LoginActivity.currentUser.friendsListIds.remove(retrieved.getUserId());
                        databaseUsers.child(LoginActivity.currentUser.getUserId()).child("friendsListIds").setValue(LoginActivity.currentUser.friendsListIds);
                        FriendsListActivity.changedFriends = true;
                        int position = findUser(retrieved.getUserId());
                        LoginActivity.currentUser.friendsList.remove(position);
                        dialog.dismiss();
                        onResume();
                    }
                });

                btnBlock.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LoginActivity.currentUser.blockList.add(retrieved.getUserId());
                        databaseUsers.child(LoginActivity.currentUser.getUserId()).child("blockList").setValue(LoginActivity.currentUser.blockList);

                        if (LoginActivity.currentUser.friendsListIds.contains(retrieved.getUserId())) {
                            LoginActivity.currentUser.friendsListIds.remove(retrieved.getUserId());
                            databaseUsers.child(LoginActivity.currentUser.getUserId()).child("friendsListIds").setValue(LoginActivity.currentUser.friendsListIds);
                            FriendsListActivity.changedFriends = true;
                            int position = findUser(retrieved.getUserId());
                            LoginActivity.currentUser.friendsList.remove(position);
                            dialog.dismiss();
                            onResume();
                        }
                    }
                });

                btnMarco.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(FriendsListActivity.this, MarcoActivity.class);
                        intent.putExtra("callingActivity", "CustomDialog");
                        intent.putExtra("userId", retrieved.getUserId());
                        startActivity(intent);
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });
    }
    public int findUser(String id){
        for (int i = 0; i < LoginActivity.currentUser.friendsList.size(); i++)
            if (LoginActivity.currentUser.friendsList.get(i).getUserId().equals(id))
                return i;
        return -1;
    }
}
