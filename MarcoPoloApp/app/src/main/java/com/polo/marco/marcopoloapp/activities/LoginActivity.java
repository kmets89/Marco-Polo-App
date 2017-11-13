package com.polo.marco.marcopoloapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
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
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Task;
import com.polo.marco.marcopoloapp.R;
import com.polo.marco.marcopoloapp.api.database.Database;
import com.polo.marco.marcopoloapp.api.database.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/*
    Implemented by Joseph
 */

public class LoginActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private static final String TAG = "SignInActivity";
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


//        GoogleSignInApi.silentSignIn()
//            .addOnCompleteListener(this, new OnCompleteListener<GoogleSignInAccount>(){
//                @Override
//                public void onComplete(@NonNull Task<GoogleSignInAccount> task){
//                    handleSilentSignInResult(task);
//                }
//            });


        // BEGIN FACEBOOK LOGIN
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken newAccessToken) {
                updateWithToken(newAccessToken);
            }
        };

        updateWithToken(AccessToken.getCurrentAccessToken());

        facebookCallbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) findViewById(R.id.facebook_login_button);
        loginButton.registerCallback(facebookCallbackManager, new FacebookCallback<LoginResult>() {
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

                                    handleSignInResult(id, firstName + " " + lastName, false);
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

    private void updateWithToken(AccessToken currentAccessToken) {
        if (currentAccessToken != null) {
            handleSignInResult(currentAccessToken.getUserId(), "", false);
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
                handleSignInResult(result.getSignInAccount().getId(), result.getSignInAccount().getDisplayName(), true);
            } else {
                updateUI(false, "");
            }
        } else {
            facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleSilentSignInResult(@NonNull Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String idToken = account.getIdToken();

            // TODO(developer): send ID Token to server and validate

            updateUI(true, "");
        } catch (ApiException e) {
            Log.w(TAG, "handleSignInResult:error", e);
            updateUI(false, null);
        }
    }

    private void handleSignInResult(String id, String name, boolean google) {
        boolean isInDatabase = isInDatabase(id);
        Log.d(TAG, "User with ID Token:" + name + " logged in.");
        Log.d(TAG, "User is in database: " + isInDatabase);
        if (!isInDatabase) {
            User new_user = new User(id, name, google ? "Google" : "Facebook", null);
            Database.updateUser(new_user);
        }

        currentUser = new User(id, name, google ? "Google" : "Facebook", null);

        updateUI(true, name);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    private void updateUI(boolean signedIn, String name) {
        Intent intent = new Intent(this, SplashActivity.class);
        intent.putExtra("loggingIn", true);
        startActivity(intent);
        this.finish();

        if (signedIn && name.length() > 0) {
            Toast.makeText(getApplicationContext(), "Welcome " + name + "!", Toast.LENGTH_LONG).show();
        }
    }

    private boolean isInDatabase(String id) {
        User user = Database.getUser(id);

        if (user == null)
            return false;
        else
            return true;
    }

}
