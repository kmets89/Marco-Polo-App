package com.polo.marco.marcopoloapp.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.polo.marco.marcopoloapp.R;
import com.polo.marco.marcopoloapp.firebase.models.User;
import com.polo.marco.marcopoloapp.firebase.tasks.LoadUserFromDbEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

/*
    Implemented by Joseph (Google) & Chase (Facebook)
 */

public class LoginActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;

    private GoogleApiClient mGoogleApiClient;
    private ProgressDialog mProgressDialog;

    // FACEBOOK LOGIN VARS //
    private CallbackManager facebookCallbackManager;
    private AccessTokenTracker accessTokenTracker;

    public static User currentUser = null;
    public static String firebaseToken = null;

    private DatabaseReference databaseUsers;
    private DatabaseReference databaseEmails;
    private DatabaseReference databaseNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Button listeners
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.facebook_login_button).setOnClickListener(this);

        LoginButton facebookLoginButton = (LoginButton) findViewById(R.id.facebook_login_button);
        facebookLoginButton.setReadPermissions(Arrays.asList("public_profile", "user_friends", "email"));

        databaseUsers = FirebaseDatabase.getInstance().getReference("users");
        databaseEmails = FirebaseDatabase.getInstance().getReference("emails");
        databaseNames = FirebaseDatabase.getInstance().getReference("names");

        // [START configure_signin]
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();
        // [END configure_signin]

        // [START build_client]
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();
        // [END build_client]

        // [START customize_button]
        // Set the dimensions of the sign-in buttons.
        SignInButton GoogleSignInButton = (SignInButton) findViewById(R.id.sign_in_button);
        GoogleSignInButton.setSize(SignInButton.SIZE_WIDE);
        GoogleSignInButton.setScaleX(1.2f);
        GoogleSignInButton.setScaleY(1.2f);
        facebookLoginButton.setScaleX(1.2f);
        facebookLoginButton.setScaleY(1.3f);
        // [END customize_button]

        // BEGIN FACEBOOK LOGIN
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken newAccessToken) {
                updateWithToken(newAccessToken);
            }
        };

        updateWithToken(AccessToken.getCurrentAccessToken());

        facebookCallbackManager = CallbackManager.Factory.create();
        facebookLoginButton.registerCallback(facebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Log.v("LoginActivity", response.toString());

                                // Application code
                                try {
                                    String id = response.getJSONObject().getString("id");
                                    String email = response.getJSONObject().getString("email");
                                    Log.d("IN GETTING INFO", email);
                                    String firstName = response.getJSONObject().getString("first_name");
                                    String lastName = response.getJSONObject().getString("last_name");

                                    Log.d(TAG, "Successfully logged into Facebook: " + id + ":" + firstName + " " + lastName);

                                    handleFacebookSignInResult(id, firstName + " " + lastName, email);

                                    String emailKey = email.replace('.', ',');
                                    emailKey = emailKey.toLowerCase();
                                    String nameKey = (firstName + " " + lastName).toLowerCase();
                                    databaseEmails.child(emailKey).child(id).setValue(email);
                                    databaseNames.child(nameKey).child(id).setValue(firstName + " " + lastName);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,first_name,last_name,email");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                updateUI(false, null);
            }

            @Override
            public void onError(FacebookException error) {
                updateUI(false, null);
            }
        });
    }

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    @Override
    public void onStart() {
        super.onStart();

        //  Try to silently sign into Google. If the user is already logged into the app, then the Maps Activity
        //      is loaded and the login screen is bypassed.
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleGoogleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    handleGoogleSignInResult(googleSignInResult);
                }
            });
        }
    }

    //  Login with Facebook Token
    private void updateWithToken(AccessToken currentAccessToken) {
        if (currentAccessToken != null) {
            databaseUsers.child(currentAccessToken.getUserId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User retrievedUser = dataSnapshot.getValue(User.class);
                            currentUser = retrievedUser;

                            if (currentUser != null) {
                                new GraphRequest(AccessToken.getCurrentAccessToken(), "me/friends/", null, HttpMethod.GET,
                                        new GraphRequest.Callback() {
                                            public void onCompleted(GraphResponse response) {
                                                if (response != null && response.getJSONObject() != null) {
                                                    try {

                                                        JSONArray data = (JSONArray) response.getJSONObject().get("data");
                                                        //currentUser.setFriendsList(new ArrayList<User>());
                                                        currentUser.setFriendsListIds(new ArrayList<String>());

                                                        for (int i = 0; i < data.length(); i++) {
                                                            String id = ((JSONObject) data.get(i)).getString("id");
                                                            databaseUsers.child(id)
                                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                                            User retrievedUser = dataSnapshot.getValue(User.class);
                                                                            if (retrievedUser != null) {
                                                                                //currentUser.friendsList.add(retrievedUser);
                                                                                currentUser.friendsListIds.add(retrievedUser.getUserId());
                                                                                Log.d(TAG, retrievedUser.getName());
                                                                            }
                                                                        }

                                                                        @Override
                                                                        public void onCancelled(DatabaseError databaseError) {

                                                                        }
                                                                    });
                                                        }
                                                        databaseUsers.child(currentUser.getUserId()).setValue(currentUser);
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }
                                        }
                                ).executeAsync();

                                updateUI(true, "");
                            }/* else {
                                handleFacebookSignInResult();
                            }*/
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                GoogleSignIn();
                break;
        }
    }

    private void GoogleSignIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result != null) {
                handleGoogleSignInResult(result);
            } else {
                updateUI(false, "");
            }
        } else {
            facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleFacebookSignInResult(String id, String name, String email) {
        databaseUsers.child(id).addListenerForSingleValueEvent(
                new LoadUserFromDbEvent(databaseUsers, id, name, "facebook", "https://graph.facebook.com/" + id + "/picture?type=square", email)
        );
        updateUI(true, name);
    }

    private void handleGoogleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleGoogleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            final String id = acct.getId();
            final String name = acct.getDisplayName();
            final String imgUrl = acct.getPhotoUrl().toString();
            final String email = acct.getEmail();

            databaseUsers.child(id).addListenerForSingleValueEvent(
                    new LoadUserFromDbEvent(databaseUsers, id, name, "Google", imgUrl, email)
            );

            String emailKey = email.replace('.', ',');
            emailKey = emailKey.toLowerCase();
            String nameKey = name.toLowerCase();
            databaseEmails.child(emailKey).child(id).setValue(email);
            databaseNames.child(nameKey).child(id).setValue(name);

            updateUI(true, name);
        } else {
            updateUI(false, "");
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }


    private void updateUI(boolean signedIn, String name) {
        if (signedIn) {
            // Place user info into shared preferences

            if (name.length() > 0) {
                SharedPreferences sharedPref = this.getSharedPreferences("com.polo.marco.app", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("Current_User_Name", name).apply();
            }

            Intent intent = new Intent(this, SplashActivity.class);
            intent.putExtra("loggingIn", true);
            startActivity(intent);
            this.finish();

            if (name.length() > 0) {
                Toast welcomeToast = Toast.makeText(getApplicationContext(), "Welcome " + name + "!", Toast.LENGTH_LONG);
                welcomeToast.show();
            }
        }
        // Else, keep the UI the same
    }
}
