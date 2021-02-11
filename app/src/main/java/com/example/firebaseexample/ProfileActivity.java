package com.example.firebaseexample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseUser user;
    private DatabaseReference reference;
    private String userID;

    private Button logoutButton;
    private TextView name;
    private TextView email;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        findViews();
        setListeners();
        //check if the user is signed on with an email account, and get the database data
        getUser();
        getData();
        //check if the user is signed on with a google account and get the data
        getGoogleAccount();
    }

    private void findViews() {
        logoutButton = findViewById(R.id.logout_button);
        name = findViewById(R.id.name_display);
        email = findViewById(R.id.email_display);
    }
    private void setListeners() {
        logoutButton.setOnClickListener(v -> {
            //get signout instance of firebase auth
            signOut();
        });
    }

    private void getUser() {
        //get current user id by geting an instance of FirebaseAuth, getting a reference to the Users path, and setting the ID string to the users unique ID in the DB
        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        userID = user.getUid();
    }

    private void getData() {
        reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //get current value from realtime DB
                User userProfile = snapshot.getValue(User.class);

                //if the user object created from the DB is not null set the textviews to the users data
                if(userProfile != null){
                    String nameData = userProfile.fullName;
                    String emailData = userProfile.email;

                    name.setText(nameData);
                    email.setText(emailData);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "ERROR... Please retry the sign in process!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //if gAcc was used
    private void getGoogleAccount() {
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(ProfileActivity.this);
        if(signInAccount != null){
            name.setText(signInAccount.getDisplayName());
            email.setText(signInAccount.getEmail());
        }
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(ProfileActivity.this, MainActivity.class));
        finish();
    }

}