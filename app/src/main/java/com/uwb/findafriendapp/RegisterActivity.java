package com.uwb.findafriendapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.uwb.findafriendapp.classes.User;

public class RegisterActivity extends AppCompatActivity {


    private static final String TAG = "USER";
    // UI elements
    private EditText etUsername, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private ProgressBar registrationProgressBar;

    // FIREBASE
    private FirebaseAuth myFirebaseAuth;
    private FirebaseDatabase database;
    private FirebaseUser currentFirebaseUser;
    private DatabaseReference dbRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // FIREBASE
        database = FirebaseDatabase.getInstance();
        myFirebaseAuth = FirebaseAuth.getInstance();
        currentFirebaseUser = myFirebaseAuth.getCurrentUser();

        // EDIT TEXT
        etUsername = findViewById(R.id.etUsarname);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        // BUTTON
        btnRegister = findViewById(R.id.btnRegister);

        // PROGRESS BAR
        registrationProgressBar = findViewById(R.id.registrationProgressBar);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String username = etUsername.getText().toString().trim();
                final String email = etEmail.getText().toString().trim();
                final String password = etPassword.getText().toString();
                String confirmPassword = etConfirmPassword.getText().toString();

                // check if fields are empty
                if (username.isEmpty()) {
                    etUsername.setError("Username cannot be empty");
                    etUsername.requestFocus();
                } else if (email.isEmpty()) {
                    etEmail.setError("Email cannot be empty");
                    etEmail.requestFocus();
                } else if (password.isEmpty()) {
                    etPassword.setError("Password cannot be empty");
                    etPassword.requestFocus();

                    // if none of fields are empty
                } else {
                    // check if passwords are the same
                    if (!password.equals(confirmPassword)) {
                        etConfirmPassword.setError("Both passwords must be identical");
                        etConfirmPassword.requestFocus();
                    } else {
                        registrationProgressBar.setVisibility(View.VISIBLE);
                        dbRef = database.getReference("users");

                        // check if a username is already taken or not
                        dbRef.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    registrationProgressBar.setVisibility(View.GONE);
                                    Toast.makeText(RegisterActivity.this, "Login taken", Toast.LENGTH_SHORT).show();
                                // register user if a username is not taken
                                } else {
                                    registerUser(username, email, password);
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                }
            }

        });
    }





    private void registerUser(final String username, final String email, String password) {
        myFirebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        registrationProgressBar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            // in case of current user being null
                            currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                            currentFirebaseUser.sendEmailVerification()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                dbRef = database.getReference("users");
                                                User user = new User(username, email);
                                                Log.d(TAG, "currentFirebaseUser");
                                                dbRef.child(currentFirebaseUser.getUid()).setValue(user);
                                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                                startActivity(intent);
                                                Toast.makeText(RegisterActivity.this, "Verification email sent", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });



                        } else {
                            Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }
}