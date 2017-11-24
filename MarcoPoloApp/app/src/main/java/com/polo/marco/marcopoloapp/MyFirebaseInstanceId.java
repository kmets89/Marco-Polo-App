package com.polo.marco.marcopoloapp;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.polo.marco.marcopoloapp.activities.LoginActivity;

/**
 * Created by Krazy on 11/23/2017.
 */

public class MyFirebaseInstanceId extends FirebaseInstanceIdService{

    private static final String TAG = "MyFirebaseIIDService";

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String token){
        LoginActivity.firebaseToken = token;
    }
}
