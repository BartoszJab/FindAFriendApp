package com.uwb.findafriendapp.dialogs;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.uwb.findafriendapp.LoginActivity;
import com.uwb.findafriendapp.ProfileActivity;
import com.uwb.findafriendapp.R;
import com.uwb.findafriendapp.RegisterActivity;
import com.uwb.findafriendapp.classes.User;

import java.util.HashMap;

public class EditEmailDialog extends DialogFragment {

    private static final String TAG = "EditEmailDialog";

    // UI
    private EditText etNewEmail, etEmailConfirmPassword;
    private Button bCancel, bOk;
    private ProgressBar progressBar;
    private ImageView ivCancelEmail;

    // Firebase
    private DatabaseReference dbRef;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_edit_email, container, false);
        bCancel = view.findViewById(R.id.bCancelEmail);
        bOk = view.findViewById(R.id.bOkEmail);
        etNewEmail = view.findViewById(R.id.etNewEmail);
        etEmailConfirmPassword = view.findViewById(R.id.etEmailChangeConfirmPassword);
        progressBar = view.findViewById(R.id.progressBarEmailChange);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        ivCancelEmail = view.findViewById(R.id.ivCancelEmail);

        firebaseAuth = FirebaseAuth.getInstance();

        etNewEmail.setText(((ProfileActivity)getActivity()).tvEmail.getText().toString());

        // Firebase
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbRef = FirebaseDatabase.getInstance().getReference().child("users").child(userID);

        // Button that says "Cancel"
        bCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        ivCancelEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        // Button that says "Ok"
        bOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String confirmedPassword = etEmailConfirmPassword.getText().toString();
                final String input = etNewEmail.getText().toString();


                if (confirmedPassword.isEmpty()) {
                    etEmailConfirmPassword.setError("You have to confirm password");
                    etEmailConfirmPassword.requestFocus();
                } else {

                    if (firebaseUser.getEmail().equals(input)) {
                        Toast.makeText(view.getContext(), "You already have this email", Toast.LENGTH_SHORT).show();
                    } else if (!isEmailValid(input)) {
                        etNewEmail.setError("Email badly formatted");
                        etNewEmail.requestFocus();
                    } else {
                        progressBar.setVisibility(View.VISIBLE);

                        Log.d(TAG, "Confirmed password: " + confirmedPassword);
                        Log.d(TAG, "Old email: " + firebaseUser.getEmail());
                        Log.d(TAG, "New email: " + input);

                        Log.d(TAG, "EMAIL BEFORE CHANGE: " + firebaseUser.getEmail());
                        Log.d(TAG, "EMAIL AFTER CHANGE: " + firebaseUser.getEmail());
//                        FirebaseUser userAfterEmailChanged = FirebaseAuth.getInstance().getCurrentUser();
                        changeUserEmail(firebaseUser.getEmail(), input, confirmedPassword);
//                        Log.d(TAG, "EMAIL AFTER CHANGE NEW USER! :D : " + userAfterEmailChanged.getEmail());
//                        firebaseUser.sendEmailVerification()
//                                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<Void> task) {
//                                        if (task.isSuccessful()) {
//                                            FirebaseDatabase.getInstance().getReference("users/" + firebaseUser.getUid() + "/email").setValue(input);
////                                            User user = new User(username, email);
////                                            dbRef.child(currentFirebaseUser.getUid()).setValue(user);
////                                            Intent intent = new Intent(getContext(), LoginActivity.class);
////                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                                            Toast.makeText(getContext(), "Verify your new email", Toast.LENGTH_SHORT).show();
//                                            changeUserEmail(firebaseUser.getEmail(), input, confirmedPassword);
////                                            firebaseAuth.signOut();
////                                            startActivity(intent);
//
////                                            getActivity().finish();
//                                        } else {
//                                            Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                                        }
//                                    }
//                                });


                    }
                }
            }
        });

        return view;
    }

    private void changeUserEmail(String oldEmail, final String newEmail, String password) {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // Get auth credentials from the user for re-authentication
        AuthCredential credential = EmailAuthProvider
                .getCredential(oldEmail, password); // current login credentials
        user.reauthenticate(credential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "User re-authenticated");
                        updateEmailAuthetication(newEmail);
                        ((ProfileActivity)getActivity()).tvEmail.setText(newEmail);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.GONE);

                Toast.makeText(getView().getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEmailAuthetication(final String newEmail) {
        // Change email
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.updateEmail(newEmail)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "User email updated");

                        updateEmailDatabase(newEmail);
                        getDialog().dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.GONE);
                Log.d(TAG, "onFailure: email update" + e.getMessage());
                Toast.makeText(getView().getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Adding email to db method
    private void updateEmailDatabase(String newEmail){
        HashMap map = new HashMap();
        map.put("email", newEmail);
        dbRef.updateChildren(map);
    }

    private boolean isEmailValid(CharSequence email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
