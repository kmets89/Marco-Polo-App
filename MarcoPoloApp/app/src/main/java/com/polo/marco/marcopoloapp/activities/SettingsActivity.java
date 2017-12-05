package com.polo.marco.marcopoloapp.activities;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.polo.marco.marcopoloapp.R;
import com.polo.marco.marcopoloapp.firebase.MyFirebaseInstanceIdService;
import com.polo.marco.marcopoloapp.firebase.models.Marco;
import com.polo.marco.marcopoloapp.firebase.models.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.squareup.picasso.Picasso;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SettingsActivity";

    private GoogleApiClient mGoogleApiClient;

    private String currentUser;
    private User user;
    private View view;

    private DatabaseReference databaseUsers = FirebaseDatabase.getInstance().getReference("users");
    private DatabaseReference databaseEmails = FirebaseDatabase.getInstance().getReference("emails");
    private DatabaseReference databaseMarcos = FirebaseDatabase.getInstance().getReference("marcos");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        user = LoginActivity.currentUser;
        int service;

        //get user profile pic and name to display
        TextView nameTextView = (TextView) findViewById(R.id.settings_user_name);
        ImageView profilePicView = (ImageView) findViewById(R.id.settings_profile_pic);
        Picasso.with(SettingsActivity.this).load(user.getImgUrl()).into(profilePicView);
        Log.d(" Checking image", user.getImgUrl().toString());
        nameTextView.setText(user.getName());
        ImageView serviceLoggedInAs = (ImageView) findViewById(R.id.settings_logged_in_as);

        //find the service the user is logged in with to display icon
        if(user.usingFacebook())
            service = R.drawable.com_facebook_button_icon_blue;
        else
            service = R.drawable.googleg_standard_color_18;
        Picasso.with(SettingsActivity.this).load(service).into(serviceLoggedInAs);

        TextView emailTextView = (TextView) findViewById(R.id.settings_user_email);
        emailTextView.setText(user.getEmail());

        //Have sign in initialization again to be able to disconnect and properly sign out
        // There might be a better way to do this
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();

        checkCurrentMarco();


    }



    public void onClickSignOut(View view) {
        if (LoginActivity.currentUser.usingFacebook()) {
            LoginManager.getInstance().logOut();
            updateUI();
        } else {
            // log out of Google
            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                mGoogleApiClient.clearDefaultAccountAndReconnect().setResultCallback(new ResultCallback<Status>() {

                    @Override
                    public void onResult(Status status) {
                        mGoogleApiClient.disconnect();
                        updateUI();
                    }
                });
            }
        }
        new AsyncTask<Void,Void,Void>()
        {
            @Override
            protected Void doInBackground(Void... params)
            {
                {
                    try
                    {
                        FirebaseInstanceId.getInstance().deleteInstanceId();
                        MyFirebaseInstanceIdService thing = new MyFirebaseInstanceIdService();
                        thing.onTokenRefresh();
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
                return null;
            }
            @Override
            protected void onPostExecute(Void result)
            {
                //call your activity where you want to land after log out
            }
        }.execute();
    }

    private void updateUI() {
        // Place user info into shared preferences
        SharedPreferences sharedPref = this.getSharedPreferences("com.polo.marco.app", Context.MODE_PRIVATE);
        currentUser = sharedPref.getString("Current_User_Name", "");

        Log.d(TAG, "Logging Out: " + currentUser);
        Intent intent = new Intent(this, SplashActivity.class);
        //  finishAffinity() finishes all previous activities so that when the user is directed to the login screen
        //      after logging out, there are no previous intents that are still active
        finishAffinity();
        startActivity(intent);

        Toast.makeText(getApplicationContext(), "Goodbye " + currentUser + "!", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    public void onClickSyncContacts(View view){
        showGoAheadDialog(getResources().getString(R.string.sync_prompt));
    }

    public void onClickDeleteMarco(View view){
        showGoAheadDialog(getResources().getString(R.string.delete_marco_prompt));
    }

    public void emailInDB (String email){
        String emailKey = email.replace('.', ',');
        databaseEmails.child(emailKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Log.d("TESTING", "it doesnt exist!");
                }
                else {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        if(!LoginActivity.currentUser.friendsListIds.contains(child.getKey().toString())){
                            LoginActivity.currentUser.friendsListIds.add(child.getKey().toString());
                            databaseUsers.child(LoginActivity.currentUser.getUserId()).child("friendsListIds").setValue(LoginActivity.currentUser.friendsListIds);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void showGoAheadDialog(String message){
        final String msg = message;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setMessage(message);

        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                if (msg.equals(getResources().getString(R.string.sync_prompt)))
                    new syncContacts().execute();
                else if (msg.equals(getResources().getString(R.string.delete_marco_prompt))) {
                    databaseMarcos.child(LoginActivity.currentUser.getUserId()).removeValue();
                }
                arg0.cancel();
            }
        });

        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //do nothing here
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private class syncContacts extends AsyncTask<Void, Void, List<String>> {
        //prompt the user to search for contacts from their phone

        private ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<String> doInBackground(Void... params){
            List<String> foundEmails = new ArrayList<String>();
            //pull emails from each contact and see if that email exists in our DB
            //Toast.makeText(SettingsActivity.this, "Syncing...", Toast.LENGTH_LONG).show();
            ContentResolver contentResolver = getContentResolver();
            Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI,
                    null, null, null, null);
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String id = cursor.getString(
                            cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cursor.getString(
                            cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                    Cursor emailCursor = contentResolver.query(
                            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (emailCursor.moveToNext()) {
                        String email = emailCursor.getString(
                                emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                        String emailType = emailCursor.getString(
                                emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
                        Log.d("PARSING CONTACTS", id + " " + name + " " + email);
                        foundEmails.add(email);
                    }
                    emailCursor.close();
                }
            }
            cursor.close();
            return foundEmails;
        }

        @Override
        protected void onPostExecute(List<String> result) {
            super.onPostExecute(result);

            for (int i = 0; i < result.size(); i++){
                Log.d("Results", result.get(i));
                emailInDB(result.get(i));
            }
            progressBar.setVisibility(View.GONE);
        }
    }

    public void checkCurrentMarco(){
        databaseMarcos.child(LoginActivity.currentUser.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                TextView textView = (TextView) findViewById(R.id.my_marco_text_view);

                if (!snapshot.exists()) {
                    textView.setText("You don't have a current Marco!");
                }
                else {
                    Marco myMarco = snapshot.getValue(Marco.class);
                    String sentDate = myMarco.getTimestamp().split(" ")[0];

                    textView = (TextView) findViewById(R.id.my_marco_text_view);
                    textView.setText(myMarco.getMessage() + "\nSent on: " + sentDate);

                    Button button = (Button) findViewById(R.id.delete_marco);
                    button.setEnabled(true);
                    button.setTextColor(getResources().getColor(R.color.white));
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
