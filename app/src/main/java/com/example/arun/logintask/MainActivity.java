package com.example.arun.logintask;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "SignInActivity";

    private LoginButton mFBLoginButton;
    private TextView mGoogleUserInfo;
    private CallbackManager mCallBackManager;
    private ProfileTracker mProfileTracker;
    private AccessTokenTracker mAccessTokenTracker;
    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;
    private TextView mFacebookUserInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        mCallBackManager = CallbackManager.Factory.create();
        mFBLoginButton = (LoginButton) findViewById(R.id.login_button);
        mFacebookUserInfo = (TextView) findViewById(R.id.textView2);

        SignInButton mGoogleSignInButton = (SignInButton) findViewById(R.id.sign_in_button);
        mGoogleUserInfo = (TextView) findViewById(R.id.textView3);
        Button mGoogleSignOutButton = (Button) findViewById(R.id.button);
        mGoogleSignOutButton.setVisibility(View.INVISIBLE);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mProfileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile newProfile) {
                displayMessage(newProfile);
            }
        };

        mAccessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken,
                                                       AccessToken currentAccessToken) {
                Profile.fetchProfileForCurrentAccessToken();
                if(currentAccessToken == null){
                    mFacebookUserInfo.setText("");
                }
            }
        };

        mAccessTokenTracker.startTracking();
        mProfileTracker.startTracking();


        mFBLoginButton.registerCallback(mCallBackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast.makeText(getApplicationContext(), "Facebook Log In Successful!", Toast.LENGTH_SHORT).show();
                AccessToken accessToken = loginResult.getAccessToken();
                Profile.fetchProfileForCurrentAccessToken();
                Profile profile = Profile.getCurrentProfile();
                displayMessage(profile);
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


        mGoogleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        mGoogleSignOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();}
        });
    }

    private void displayMessage(Profile profile){
        if(profile != null){
            mFacebookUserInfo.setText("Welcome "+profile.getName());
        }else{
            //mFacebookUserInfo.setText("");
            Log.d(TAG, "Null value returned for profile");
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallBackManager.onActivityResult(requestCode, resultCode, data);
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
                        Toast.makeText(getApplicationContext(), "Google Sign Out Successful", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void updateUI(boolean signedIn) {
        if (signedIn) {
            findViewById(R.id.button).setVisibility(View.VISIBLE);

        } else {
            findViewById(R.id.button).setVisibility(View.GONE);
            mGoogleUserInfo.setText("");

        }
    }



    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            Toast.makeText(getApplicationContext(), "Google Sign In Successful!", Toast.LENGTH_SHORT).show();
            mGoogleUserInfo.setText("Welcome " +acct.getDisplayName());
            updateUI(true);

        } else {
            updateUI(false);

        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getApplicationContext(), "Failed to connect to google services", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onStart() {
        super.onStart();
        Profile profile = Profile.getCurrentProfile();
        if(profile!=null){
        displayMessage(profile);}
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }
}

