package com.example.firebaseexample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    GoogleSignInOptions gso;
    GoogleSignInClient mGoogleSignInClient;
    private final static int RC_SIGN_IN = 0;
    private int clickID;

    private EditText editTextEmail;
    private EditText editTextPassword;
    private TextView forgotPassword;
    private TextView register;
    private Button loginButton;
    SignInButton googleButton;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createAuthentications();
        findViews();
        setListeners();
    }

    private void createAuthentications() {
        mAuth = FirebaseAuth.getInstance();

        gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void findViews() {
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password_input);
        forgotPassword = findViewById(R.id.forgot_password);
        register = findViewById(R.id.register);
        loginButton = findViewById(R.id.register_button);
        googleButton = findViewById(R.id.google_button);
        googleButton.setSize(SignInButton.SIZE_STANDARD);
    }

    private void setListeners() {

        forgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(this, ForgotPassword.class));
        });

        register.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterUser.class));
            finish();
        });

        loginButton.setOnClickListener(v -> {
            clickID = 1;
            userLogin();
        });

        googleButton.setOnClickListener(v -> {
            clickID = 2;
            userLogin();
        });
    }

    private void userLogin() {
        //if email button clicked do email sign in
        if (clickID == 1){
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            //perform validations
            if (email.isEmpty()) {
                editTextEmail.setError("Email is required!");
                editTextEmail.requestFocus();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                editTextEmail.setError("Please enter a valid email!");
                editTextEmail.requestFocus();
            } else if (password.isEmpty()) {
                editTextPassword.setError("Password is required!");
                editTextPassword.requestFocus();

            } else if (password.length() < 6) {
                editTextPassword.setError("Password must be atleast 6 characters");
                editTextPassword.requestFocus();
            }

            //comlete sign in with authentication using the mAuth obect and an oncomplelistener to see if the provided credentials exist in the userbase
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                        finish();
                    } else {
                        Toast.makeText(MainActivity.this, "Could not log on T_T", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        //else do google sign in
        else{
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount gAccount = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
                firebaseAuthWithGoogle(gAccount);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                // ...
                Toast.makeText(this, "Failed Signin", Toast.LENGTH_SHORT).show();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try{
            GoogleSignInAccount gAccount = completedTask.getResult(ApiException.class);
            Toast.makeText(getApplicationContext(),"Signing Success",Toast.LENGTH_SHORT).show();
            firebaseAuthWithGoogle(gAccount);
        }catch(ApiException e)
        {
            Toast.makeText(getApplicationContext(),"Signing FAiled",Toast.LENGTH_SHORT).show();
            //FirebaseGoogleAuth(null); This is useless to call with null
        }
    }

    public void updateUI(FirebaseUser account) {
        if (account != null) {
            Toast.makeText(this, "Sign in successful", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        }
    }

    private void googleUpdateUI(GoogleSignInAccount gAccount) {
        if(gAccount != null){
            Toast.makeText(this, "Sign in successful", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                            startActivity(intent);
                        }
                        else {
                            Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }
}