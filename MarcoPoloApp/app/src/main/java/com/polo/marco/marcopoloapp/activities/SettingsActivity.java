package com.polo.marco.marcopoloapp.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.iid.FirebaseInstanceId;
import com.polo.marco.marcopoloapp.R;
import com.polo.marco.marcopoloapp.firebase.MyFirebaseInstanceIdService;

import java.io.IOException;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SettingsActivity";

    private GoogleApiClient mGoogleApiClient;

    private String currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Have sign in initialization again to be able to disconnect and properly sign out
        // There might be a better way to do this
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();
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
}
