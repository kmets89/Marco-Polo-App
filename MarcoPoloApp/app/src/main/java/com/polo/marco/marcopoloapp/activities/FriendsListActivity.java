package com.polo.marco.marcopoloapp.activities;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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

import java.util.ArrayList;
import java.util.List;

public class FriendsListActivity extends AppCompatActivity {

    public static boolean changedFriends = false;
    private DatabaseReference databaseUsers = FirebaseDatabase.getInstance().getReference("users");

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
                        LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View newView = layoutInflater.inflate(R.layout.friends_list_layout, null);

                        TextView textView1 = (TextView) newView.findViewById(R.id.friendslist_user_name);
                        textView1.setText(retrieved.getName());

                        ImageView profilePicView = (ImageView) newView.findViewById(R.id.friendslist_profile_image);
                        Picasso.with(FriendsListActivity.this).load(retrieved.getImgUrl()).into(profilePicView);

                        LinearLayout friendsView = (LinearLayout) findViewById(R.id.friends_layout_child);
                        friendsView.addView(newView);

                        newView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(FriendsListActivity.this, CustomDialogActivity.class);
                                intent.putExtra("userId", retrieved.getUserId());
                                intent.putExtra("name", retrieved.getName());
                                startActivity(intent);
                            }
                        });
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
            LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View newView = layoutInflater.inflate(R.layout.friends_list_layout, null);

            TextView textView1 = (TextView) newView.findViewById(R.id.friendslist_user_name);
            textView1.setText(retrieved.getName());

            ImageView profilePicView = (ImageView) newView.findViewById(R.id.friendslist_profile_image);
            Picasso.with(FriendsListActivity.this).load(retrieved.getImgUrl()).into(profilePicView);

            LinearLayout friendsView = (LinearLayout) findViewById(R.id.friends_layout_child);
            friendsView.addView(newView);

            newView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(FriendsListActivity.this, CustomDialogActivity.class);
                    intent.putExtra("userId", retrieved.getUserId());
                    intent.putExtra("name", retrieved.getName());
                    startActivity(intent);
                }
            });
        }
    }
}
