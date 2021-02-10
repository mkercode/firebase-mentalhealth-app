package com.example.firebaseexample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private EditText editTextEmail;
    private Button resetButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        setContentView(R.layout.activity_forgot_password);
        findViews();
        setListeners();
    }

    private void findViews() {
        editTextEmail = findViewById(R.id.forgotEmail);
        resetButton = findViewById(R.id.reset_button);
    }

    private void setListeners() {
        resetButton.setOnClickListener(v -> {
            resetPassword();
        });
    }

    private void resetPassword() {
        String email  = editTextEmail.getText().toString().trim();

        //perform validation
        if(email.isEmpty()){
            editTextEmail.setError("Email is required!");
            editTextEmail.requestFocus();
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editTextEmail.setError("Please enter a valid email address.");
            editTextEmail.requestFocus();
        }

        else{
            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(ForgotPassword.this, "Check your email!", Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(ForgotPassword.this, MainActivity.class));
                        finish();
                    }
                    else{
                        Toast.makeText(ForgotPassword.this, "Account not found, check if email is correct or internet is connected", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

    }


}