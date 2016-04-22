package com.example.arun.logintask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener{

    private LoginButton fb_login;
    private TextView user_info_g;
    private CallbackManager callbackmanager;
    private ProfileTracker profileTracker;
    private AccessTokenTracker accessTokenTracker;
    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;
    private TextView user_info_f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        callbackmanager = CallbackManager.Factory.create();
        fb_login = (LoginButton) findViewById(R.id.login_button);
        user_info_f = (TextView) findViewById(R.id.textView2);

        fb_login.registerCallback(callbackmanager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast.makeText(getApplicationContext(), "Log In Successful!", Toast.LENGTH_SHORT).show();
                if(Profile.getCurrentProfile() == null) {
                    profileTracker = new ProfileTracker() {
                        @Override
                        protected void onCurrentProfileChanged(Profile profile, Profile profile2) {
                            // profile2 is the new profile
                            profileTracker.stopTracking();
                        }
                    };
                    profileTracker.startTracking();
                }
                else {
                    AccessToken accessToken = loginResult.getAccessToken();
                    Profile profile = Profile.getCurrentProfile();
                    displayMessage(profile);
                    //user_info_f.setText("Welcome "+profile.getName());
                }

            }

            @Override
            public void onCancel() {
                Toast.makeText(getApplicationContext(), "Log In Cancelled", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(getApplicationContext(), "Unable to Log In", Toast.LENGTH_SHORT).show();

            }
        });

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken,
                                                       AccessToken currentAccessToken) {
                Profile.fetchProfileForCurrentAccessToken();
                if(currentAccessToken == null){
                    user_info_f.setText("");

                }
            }
        };
        Profile.fetchProfileForCurrentAccessToken();

        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);

        user_info_g = (TextView) findViewById(R.id.textView3);
        Button sign_out_g = (Button) findViewById(R.id.button);
        sign_out_g.setVisibility(View.INVISIBLE);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        sign_out_g.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();}
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackmanager.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        updateUI(false);
                        Toast.makeText(getApplicationContext(), "Sign Out Successful", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUI(boolean signedIn) {
        if (signedIn) {
            findViewById(R.id.button).setVisibility(View.VISIBLE);

        } else {
            findViewById(R.id.button).setVisibility(View.GONE);
            user_info_g.setText("");

        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getApplicationContext(), "Failed to connect to google services", Toast.LENGTH_SHORT).show();
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            Toast.makeText(getApplicationContext(), "Sign In Successful!", Toast.LENGTH_SHORT).show();
            user_info_g.setText("Welcome " +acct.getDisplayName());
            updateUI(true);

        } else {
            Toast.makeText(getApplicationContext(), "Sign In Unsuccessful!", Toast.LENGTH_SHORT).show();
            updateUI(false);

        }
    }

    private void displayMessage(Profile profile){
        if(profile != null){
            user_info_f.setText("Welcome "+profile.getName());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        accessTokenTracker.stopTracking();
    }

    @Override
    public void onResume() {
        super.onResume();
        Profile profile = Profile.getCurrentProfile();
        displayMessage(profile);
    }
}

