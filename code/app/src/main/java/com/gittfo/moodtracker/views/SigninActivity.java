package com.gittfo.moodtracker.views;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.gittfo.moodtracker.database.Database;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

/**
 * This is the first activity of the app. When the user launches the app
 * it will go here, and they will sign in. If signed in, it will send them
 * to the main activity.
 */
public class SigninActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 1;
    GoogleSignInClient mGoogleSignInClient;

    /**
     * In the oncreate method, create the signin client
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        findViewById(R.id.sign_in_button).setOnClickListener(v -> signIn());
    }

    /**
     * In onStart, check if the user is already signed in, and if so
     * skip the whole sign-in process
     */
    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            finalizeSignIn(account);
        }
    }

    /**
     * Calls Google's sign in screen
     */
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * This function gets back the information from the google sign-in.
     * It uses that to pass the info to the finalizeSignin, where it is
     * processed for use in the app
     *
     * @param requestCode Which activity is returning the data. For this activity always RC_SIGN_IN
     * @param resultCode The result code of the activity that ended
     * @param data the signin data returned from googles intent
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                finalizeSignIn(account);
            } catch (ApiException e) {
                // Signin troubleshooting process.
                // 1. Make sure your emulator has updated its google play services
                //    to the latest version
                // 2. Make sure you sent me your SHA1 fingerprint for your debug key
                e.printStackTrace();
                // TODO: Show an error message
                this.recreate();
            }
        }
    }

    /**
     * If sign-in is successful, pass the user through to the MainActivity.
     *
     * @param account The google account used for sign-in
     */
    private void finalizeSignIn(GoogleSignInAccount account) {
        // Ensure a valid Google account has been provided
        if (account != null) {
            // Store user id
            this.getSharedPreferences(Database.PREFS, MODE_PRIVATE)
                    .edit()
                    .putString("user", account.getId())
                    .apply();

            // Start main activity
            Intent startApp = new Intent(this, MainActivity.class);
            this.startActivity(startApp);
        } else {
            // TODO: Notify User
            this.recreate();
        }
    }
}
