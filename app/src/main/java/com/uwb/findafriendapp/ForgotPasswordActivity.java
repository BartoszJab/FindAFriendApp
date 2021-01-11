package com.uwb.findafriendapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private FirebaseAuth myFirebaseAuth;
    private EditText etEmail;
    private Button btnSendResetEmail;
    private ProgressBar resetProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        myFirebaseAuth = FirebaseAuth.getInstance();
        etEmail = findViewById(R.id.etResetEmail);
        btnSendResetEmail = findViewById(R.id.btnSendResetEmail);
        resetProgressBar = findViewById(R.id.resetPasswordProgress);

        btnSendResetEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetProgressBar.setVisibility(View.VISIBLE);

                if (myFirebaseAuth.getCurrentUser().isEmailVerified()) {
                    sendVerificationEmail();
                } else {
                    resetProgressBar.setVisibility(View.GONE);
                    Toast.makeText(ForgotPasswordActivity.this, "You have not verified your email yet", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendVerificationEmail() {
        myFirebaseAuth.sendPasswordResetEmail(etEmail.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                resetProgressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    Toast.makeText(ForgotPasswordActivity.this, "Reset email was sent", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });

    }
}