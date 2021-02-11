package com.example.firebaseexample;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.Arrays;


public class RegisterLoginActivity extends AppCompatActivity {

    private static final String TAG = "Login/Register Activity";
    int AUTHUI_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_login);
        authorizationCheck();
        Button button = findViewById(R.id.logreg_button);
        button.setOnClickListener(v -> handleLoginRegister(button));
    }

    //check if user is logged in, if so open the main activity
    private void authorizationCheck() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    //create a sign in UI with a list of our firebase accepted providers
    private void handleLoginRegister(Button view) {
        Intent intent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(Arrays.asList(
                        new AuthUI.IdpConfig.EmailBuilder().build(),
                        new AuthUI.IdpConfig.GoogleBuilder().build()))
                .setLogo(R.drawable.ic_brain).setTheme(R.style.AppThemeFirebaseAuth)
                .build();
        startActivityForResult(intent, AUTHUI_REQUEST_CODE);
    }

    //check code to see if sign in is succesful
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTHUI_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // We have signed in the user or we have a new user
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Log.d(TAG, "onActivityResult: " + user.toString());
                //Checking for User (New/Old)
                if (user.getMetadata().getCreationTimestamp() == user.getMetadata().getLastSignInTimestamp()) {
                    //This is a New User
                } else {
                    //This is a returning user
                }

                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                this.finish();

            } else {
                // Signing in failed
                IdpResponse response = IdpResponse.fromResultIntent(data);
                if (response == null) {
                    Log.d(TAG, "onActivityResult: the user has cancelled the sign in request");
                }
            }
        }
    }
}