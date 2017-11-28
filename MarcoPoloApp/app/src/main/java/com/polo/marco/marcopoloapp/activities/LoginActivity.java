package com.polo.marco.marcopoloapp.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
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
import com.polo.marco.marcopoloapp.R;
import com.polo.marco.marcopoloapp.api.database.Database;
import com.polo.marco.marcopoloapp.api.database.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Button listeners
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.facebook_login_button).setOnClickListener(this);

        LoginButton facebookLoginButton = (LoginButton) findViewById(R.id.facebook_login_button);
        facebookLoginButton.setReadPermissions(Arrays.asList("public_profile", "user_friends"));

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
                                    String firstName = response.getJSONObject().getString("first_name");
                                    String lastName = response.getJSONObject().getString("last_name");

                                    Log.d(TAG, "Successfully logged into Facebook: " + id + ":" + firstName + " " + lastName);

                                    handleFacebookSignInResult(id, firstName + " " + lastName);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,first_name,last_name");
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
            currentUser = Database.getUser(currentAccessToken.getUserId());

            if (currentUser != null) {
                new GraphRequest(AccessToken.getCurrentAccessToken(), "me/friends/", null, HttpMethod.GET,
                        new GraphRequest.Callback() {
                            public void onCompleted(GraphResponse response) {
                                if (response != null && response.getJSONObject() != null) {
                                    try {
                                        JSONArray data = (JSONArray) response.getJSONObject().get("data");
                                        currentUser.friendsUserList = new ArrayList<User>();
                                        currentUser.setFriendsList(new ArrayList<String>());


                                        for (int i = 0; i < data.length(); i++) {
                                            User friend = Database.getUser(((JSONObject) data.get(i)).getString("id"));
                                            if (friend == null)
                                                continue;

                                            currentUser.friendsUserList.add(friend);
                                            currentUser.getFriendsList().add(friend.getUserId());

                                            Log.d(TAG, "Friend" + i + ": " + currentUser.friendsUserList.get(i).getName());
                                        }

                                        Database.updateUser(currentUser);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                ).executeAsync();

                updateUI(true, "");
            } else {
                LoginManager.getInstance().logOut();
            }
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

    private void handleFacebookSignInResult(String id, String name) {
        boolean isInDatabase = isInDatabase(id);
        Log.d(TAG, "User with ID Token:" + name + " logged in.");
        Log.d(TAG, "User is in database: " + isInDatabase);
        if (!isInDatabase) {
            Log.d(TAG, "???: " + name);
            User new_user = new User(id, name, "Facebook", new ArrayList<String>(), 0, 0, "https://graph.facebook.com/" + id + "/picture?type=square", "");
            currentUser = new_user;
            Database.updateUser(new_user);
        } else {
            //Set the current user from the Databases information
            User user = Database.getUser(id);
            currentUser = user;
            //Load all of the current users' friends information.
            currentUser.friendsUserList = Database.getListOfFriends(user.getFriendsList());
        }

        updateUI(true, name);
    }

    private void handleGoogleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleGoogleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
//            Verify ID Token
//            Code still in progress to verify ID Token via HTTPS on the Google servers

//            GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
//            Oauth2 oauth2 = new Oauth2.Builder(new NetHttpTransport(), new JacksonFactory(), credential).setApplicationName(
//                    "Oauth2").build();
//            Userinfoplus userinfo = oauth2.userinfo().get().execute();
//            userinfo.toPrettyString();

//            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
//                    .setAudience(Collections.singletonList(CLIENT_ID))
//                    // Or, if multiple clients access the backend:
//                    //.setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
//                    .build();
//
//// (Receive idTokenString by HTTPS POST)
//
//            GoogleIdToken idToken = verifier.verify(idTokenString);
//            if (idToken != null) {
//                Payload payload = idToken.getPayload();
//
//                // Print user identifier
//                String userId = payload.getSubject();
//                System.out.println("User ID: " + userId);
//
//                // Get profile information from payload
//                String email = payload.getEmail();
//                boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
//                String name = (String) payload.get("name");
//                String pictureUrl = (String) payload.get("picture");
//                String locale = (String) payload.get("locale");
//                String familyName = (String) payload.get("family_name");
//                String givenName = (String) payload.get("given_name");
//
//                // Use or store profile information
//                // ...
//
//            } else {
//                System.out.println("Invalid ID token.");
//            }


            GoogleSignInAccount acct = result.getSignInAccount();
            String id = acct.getId();
            String name = acct.getDisplayName();
            String email = acct.getEmail();
            String imgUrl = acct.getPhotoUrl().toString();

            boolean isInDatabase = isInDatabase(id);
            Log.d(TAG, "User with ID Token:" + name + " logged in.");
            Log.d(TAG, "User is in database: " + isInDatabase);
            if (!isInDatabase) {
                List<String> temp = new ArrayList<String>();
                User new_user = new User(id, name, email, "Google", temp, 0, 0, imgUrl);
                currentUser = new_user;
                Database.updateUser(new_user);
                Email new_email = new Email(email, id);
                Database.updateEmail(new_email);
            }else{
                //Set the current user from the Databases information
                User user = Database.getUser(id);
                currentUser = user;
                Email new_email = new Email(currentUser.getEmail(), currentUser.getUserId());
                Database.updateEmail(new_email);
                //Load all of the current users' friends information.
                //currentUser.friendsUserList = Database.getListOfFriends(user.getFriendsList());
            }

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

    private boolean isInDatabase(String id) {
        User user = Database.getUser(id);

        if (user == null)
            return false;
        else
            return true;
    }

}
