package com.polo.marco.marcopoloapp.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.polo.marco.marcopoloapp.R;
import com.polo.marco.marcopoloapp.firebase.models.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kmets on 11/28/2017.
 */

public class SearchResultsActivity extends AppCompatActivity {
    private DatabaseReference databaseUsers = FirebaseDatabase.getInstance().getReference("users");
    private DatabaseReference databaseEmails = FirebaseDatabase.getInstance().getReference("emails");
    private DatabaseReference databaseNames = FirebaseDatabase.getInstance().getReference("names");

    private List<String> foundAccounts = new ArrayList<String>();
    private List<User> foundUsers = new ArrayList<User>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.search_results_activity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent extras = getIntent();
        String searchText = (extras.getStringExtra("searchText")).toLowerCase();
        searchText = searchText.replace('.', ',');

        //pull account numbers from matching email
        databaseEmails.child(searchText).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                }
                else {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        String foundEmail = child.getKey().toString();
                        foundAccounts.add(foundEmail);
                        Log.d("SEARCHING", "found email " + foundEmail);
                        pullUser(foundEmail);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //pull account numbers from matching names
        databaseNames.child(searchText).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Log.d("SEARCHING", "not found!");
                }
                else {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Log.d("SEARCHING", "found name " + child.toString());
                        String foundName = child.getKey().toString();
                        foundAccounts.add(foundName);
                        Log.d("SEARCHING", "found name " + foundName);
                        pullUser(foundName);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    //pull users from matched accounts
    public void pullUser(String id) {
        databaseUsers.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Log.d("RESULTS", "not found!");
                }
                else {
                    final User retrieved = snapshot.getValue(User.class);
                    foundUsers.add(retrieved);
                    Log.d("RESULTS","found User " + retrieved.getName());

                    LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View newView = layoutInflater.inflate(R.layout.search_result_layout, null);

                    TextView textView1 = (TextView) newView.findViewById(R.id.user_name);
                    textView1.setText(retrieved.getName());

                    TextView textView2 = (TextView) newView.findViewById(R.id.email_address);
                    textView2.setText(retrieved.getEmail());

                    ImageView profilePicView = (ImageView) newView.findViewById(R.id.profile_pic);
                    Picasso.with(SearchResultsActivity.this).load(retrieved.getImgUrl()).into(profilePicView);

                    newView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final String id = retrieved.getUserId();
                            //If they are already in your friends list.
                            if(LoginActivity.currentUser.getFriendsListIds().contains(id)){
                                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SearchResultsActivity.this);
                                View view = getLayoutInflater().inflate(R.layout.dialog_select_friends, null);
                                TextView txtViewFriend = (TextView) view.findViewById(R.id.txt_friend_name);
                                Button btnUnfriend = (Button) view.findViewById(R.id.btn_unfriend);
                                Button btnBlock = (Button) view.findViewById(R.id.btn_block);
                                Button btnMarco = (Button) view.findViewById(R.id.btn_marco_in_friendslist);

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
                                        Intent intent = new Intent(SearchResultsActivity.this, MarcoActivity.class);
                                        intent.putExtra("callingActivity", "CustomDialog");
                                        intent.putExtra("userId", retrieved.getUserId());
                                        startActivity(intent);
                                        dialog.dismiss();
                                    }
                                });

                                dialog.show();
                            }
                            //If they are in your block list
                            else if(LoginActivity.currentUser.getBlockList().contains(id)){
                                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SearchResultsActivity.this);
                                View view = getLayoutInflater().inflate(R.layout.dialog_select_unblock, null);
                                TextView txtViewFriend = (TextView) view.findViewById(R.id.txt_friend_name);
                                Button btnUnblock = (Button) view.findViewById(R.id.btn_unBlock);

                                dialogBuilder.setView(view);
                                final AlertDialog dialog = dialogBuilder.create();

                                txtViewFriend.setText(retrieved.getName());

                                btnUnblock.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        LoginActivity.currentUser.blockList.remove(id);
                                        databaseUsers.child(LoginActivity.currentUser.getUserId()).child("blockList").setValue(LoginActivity.currentUser.blockList);
                                        dialog.dismiss();
                                    }
                                });
                                dialog.show();
                            }
                            //If it is not yourself
                            else if(!id.equals(LoginActivity.currentUser.getUserId())){
                                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SearchResultsActivity.this);
                                View view = getLayoutInflater().inflate(R.layout.dialog_select_search_results, null);
                                TextView txtViewFriend = (TextView) view.findViewById(R.id.txt_friend_name);
                                Button btnAddFriend = (Button) view.findViewById(R.id.btn_unfriend);
                                Button btnBlock = (Button) view.findViewById(R.id.btn_block);

                                dialogBuilder.setView(view);
                                final AlertDialog dialog = dialogBuilder.create();

                                txtViewFriend.setText(retrieved.getName());

                                btnAddFriend.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        LoginActivity.currentUser.friendsListIds.add(id);
                                        databaseUsers.child(LoginActivity.currentUser.getUserId()).child("friendsListIds").setValue(LoginActivity.currentUser.friendsListIds);
                                        FriendsListActivity.changedFriends = true;
                                        addtoFriendsList(id);
                                        dialog.dismiss();
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
                                        }
                                        dialog.dismiss();
                                        onResume();
                                    }
                                });
                                dialog.show();
                            }
                            //If it is yourself
                            else if(id.equals(LoginActivity.currentUser.getUserId())){
                                Toast.makeText(SearchResultsActivity.this, "You found yourself!", Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                    LinearLayout resultsView = (LinearLayout) findViewById(R.id.results_layout_child);
                    resultsView.addView(newView);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public void addtoFriendsList(String id){
        databaseUsers.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {

                }
                else {
                    com.polo.marco.marcopoloapp.firebase.models.User retrieved = snapshot.getValue(com.polo.marco.marcopoloapp.firebase.models.User.class);
                    LoginActivity.currentUser.friendsList.add(retrieved);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
