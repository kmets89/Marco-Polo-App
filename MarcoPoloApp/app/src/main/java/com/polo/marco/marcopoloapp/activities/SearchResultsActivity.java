package com.polo.marco.marcopoloapp.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.polo.marco.marcopoloapp.R;
import com.polo.marco.marcopoloapp.api.database.User;
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
                            Intent intent = new Intent(SearchResultsActivity.this, CustomDialogActivity.class);
                            intent.putExtra("userId", retrieved.getUserId());
                            intent.putExtra("name", retrieved.getName());
                            startActivity(intent);
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
}
