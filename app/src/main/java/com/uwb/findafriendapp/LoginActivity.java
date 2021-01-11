package com.uwb.findafriendapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private ProgressBar progressBarLogin;

    private EditText etEmail, etPassword;

    private TextView btnResetPassword;
    private Button btnLogIn;
    private Button btnResendEmail;
    private TextView btnRegister;

    private FirebaseAuth myFirebaseAuth;
    private FirebaseUser currentFirebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // FIREBASE
        myFirebaseAuth = FirebaseAuth.getInstance();
        currentFirebaseUser = myFirebaseAuth.getCurrentUser();

        // EDIT TEXT
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        // BUTTONS
        btnLogIn = findViewById(R.id.btnLogIn);
        btnResendEmail = findViewById(R.id.btnResendEmail);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        btnRegister = findViewById(R.id.btnRegister);
        // PROGRESS BAR
        progressBarLogin = findViewById(R.id.progressBarLogin);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        btnLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (email.isEmpty()) {
                    etEmail.setError("Email cannot be empty");
                    etEmail.requestFocus();
                } else if (password.isEmpty()) {
                    etPassword.setError("Password cannot be empty");
                    etPassword.requestFocus();
                } else {
                    progressBarLogin.setVisibility(View.VISIBLE);

                    signInUser(email, password);
                }
            }
        });

        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            }
        });
    }

    private void showResendEmailText() {
        if (currentFirebaseUser != null) {
            if (!currentFirebaseUser.isEmailVerified()) {
                btnResendEmail.setVisibility(View.VISIBLE);

                btnResendEmail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        currentFirebaseUser.sendEmailVerification()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(LoginActivity.this, "Verification mail resent", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                });
            }
        } else {
            currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            btnResendEmail.setVisibility(View.VISIBLE);

            btnResendEmail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    currentFirebaseUser.sendEmailVerification()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(LoginActivity.this, "Verification mail resent", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            });

        }

    }

    private void signInUser(String email, String password) {
        myFirebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBarLogin.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            if (myFirebaseAuth.getCurrentUser().isEmailVerified()) {
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            } else {
                                showResendEmailText();
                                Toast.makeText(LoginActivity.this, "Please, verify your email", Toast.LENGTH_SHORT).show();
                           }

                        } else {
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}