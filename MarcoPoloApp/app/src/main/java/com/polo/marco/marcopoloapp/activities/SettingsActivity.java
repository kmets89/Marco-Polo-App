package com.polo.marco.marcopoloapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.polo.marco.marcopoloapp.R;

public class SettingsActivity extends AppCompatActivity {
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

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
        Intent intent = new Intent(this, SplashActivity.class);
        startActivity(intent);
        this.finish();

        if (LoginActivity.currentUser.usingFacebook()) {
            LoginManager.getInstance().logOut();
        } else {
            // log out google
            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                mGoogleApiClient.clearDefaultAccountAndReconnect().setResultCallback(new ResultCallback<Status>() {

                    @Override
                    public void onResult(Status status) {
                        mGoogleApiClient.disconnect();
                    }
                });
            }
        }
    }
}
