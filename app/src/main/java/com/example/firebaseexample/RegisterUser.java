package com.example.firebaseexample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterUser extends AppCompatActivity {
    private EditText editTextFullName;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private TextView title;
    private Button registerButton;


    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);
        mAuth = FirebaseAuth.getInstance();
        findViews();
        setListeners();
    }

    private void findViews() {
        editTextFullName = findViewById(R.id.user_name);
        editTextPassword = findViewById(R.id.password_input);
        editTextEmail = findViewById(R.id.email_input);
        registerButton = findViewById(R.id.register_button);
        title = findViewById(R.id.back_to_main);

    }

    private void setListeners() {

        registerButton.setOnClickListener(v -> {
            registerUser();
        });

        title.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private void registerUser() {

        String fullName = editTextFullName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        //perform validations
        if(fullName.isEmpty()){
            editTextFullName.setError("Full name is required!");
            editTextFullName.requestFocus();
        }
        else if(email.isEmpty()){
            editTextEmail.setError("Email is required!");
            editTextEmail.requestFocus();
        }
        else if(!(Patterns.EMAIL_ADDRESS.matcher(email).matches())){
            editTextEmail.setError("Please enter a valid email");
            editTextEmail.requestFocus();
        }
        else if(password.isEmpty()){
            editTextPassword.setError("Password is required!");
            editTextPassword.requestFocus();
        }
        else if(password.length() < 6){
            editTextPassword.setError("Password must be at least 6 characters");
            editTextPassword.requestFocus();
        }

        else{

            //use the createUserWithEmailAndPassword method with the authenticator variable
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                //check of user exists already, if not task succesful
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        User user = new User(fullName, email);

                        //call firebase database object, and get oncomplete listener
                        FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance()
                                .getCurrentUser().getUid()).setValue(user)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        //if user is registered and the data is aded to the database succesfullu
                                        if(task.isSuccessful()){
                                            Toast.makeText(RegisterUser.this, "User registered!", Toast.LENGTH_SHORT).show();

                                            //redirect to user profile
                                        }
                                        //if task is not succesful, user was  not registered to database properly
                                        else{
                                            Toast.makeText(RegisterUser.this, "User not registered T_T", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                        }
                    else{
                        Toast.makeText(RegisterUser.this, "User not registered T_T", Toast.LENGTH_SHORT).show();

                    }

                }
            });
        }
    }

}