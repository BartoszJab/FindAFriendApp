package com.uwb.findafriendapp.dialogs;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.uwb.findafriendapp.LoginActivity;
import com.uwb.findafriendapp.R;

public class ConfirmPasswordDialog extends DialogFragment {
    private static final String TAG = "ConfirmPasswordDialog";

    private EditText etConfirmPassword;
    private Button btnCancel, btnConfirm;
    private FirebaseUser firebaseUser;
    private DatabaseReference eventsUsersRef, participantRef, usersFavouritesRef, usersCommonInterestsRef, eventsRef;
    private ProgressBar progressBarConfirmPassword;
    private ImageView ivCancelPassword;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_confirm_password, container, false);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnConfirm = view.findViewById(R.id.btnConfirm);
        progressBarConfirmPassword = view.findViewById(R.id.progressBarConfirmPassword);
        ivCancelPassword = view.findViewById(R.id.ivCancelPassword);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        ivCancelPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                progressBarConfirmPassword.setVisibility(View.VISIBLE);

                // make deletion here on password confirm
                String password = etConfirmPassword.getText().toString();

                if (password.isEmpty()) {
                    etConfirmPassword.setError("You have to confirm password");
                    etConfirmPassword.requestFocus();
                    progressBarConfirmPassword.setVisibility(View.GONE);
                } else {
                    AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), etConfirmPassword.getText().toString());
                    firebaseUser.reauthenticate(credential)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {
                                                // REMOVE USER (users INDEX) FROM DATABASE
                                                FirebaseDatabase.getInstance().getReference().child("users/" + firebaseUser.getUid()).getRef().removeValue();

                                                // REMOVE USER FROM ALL EVENTS HE PARTICIPATES IN
                                                removeUserFromEvents();

                                                // REMOVE USER FROM users-favourites
                                                removeUserFromFavourites();

                                                // REMOVE USER FROM usersCommonInterestsCounter
                                                removeUserFromCommonInterests();

                                                /* REMOVE EVENTS FROM events AND EVENTS FROM events-users THAT WERE CREATED BY DELETED USER
                                                   AND ICON REFERENCE IN FirebaseStorage OF THESE EVENTS
                                                */
                                                removeUsersEvents();


                                                // REMOVE USER'S KEY FROM FirebaseStorage users/ FOLDER IF EXISTS
                                                removeUsersStorage();

                                                // REMOVE USER'S INVITATIONS TO OTHER USERS
                                                removeUserInvitations();

                                                // REMOVE USER'S TOKEN FROM TOKENS
                                                removeUsersToken();


                                                Log.d(TAG, "user account deleted");
                                                Intent intent = new Intent(view.getContext(), LoginActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);


                                            }
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBarConfirmPassword.setVisibility(View.GONE);
                            Log.d(TAG, "onFailure: " + e.getMessage());
                            Toast.makeText(getView().getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        return view;
    }

    private void removeUserFromEvents() {
        eventsUsersRef = FirebaseDatabase.getInstance().getReference().child("events-users");
        eventsUsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (final DataSnapshot eventsDataSnapshot : snapshot.getChildren()) {
                    participantRef = FirebaseDatabase.getInstance().getReference().child("events-users/" + eventsDataSnapshot.getKey() + "/" + firebaseUser.getUid());
                    Log.d(TAG, "datasnapshot value: " + eventsDataSnapshot.getValue());
                    Log.d(TAG, "datasnapshot key: " + eventsDataSnapshot.getKey());

                    participantRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Log.d(TAG, "datasnapshot key: " + eventsDataSnapshot.getKey());
                            Log.d(TAG, "participants ref snapshot.getValue: " + snapshot.getValue());
                            if (snapshot.getValue() != null) {
                                FirebaseDatabase.getInstance().getReference().child("events-users/" + eventsDataSnapshot.getKey() + "/" + firebaseUser.getUid()).getRef().removeValue();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void removeUserFromFavourites() {
        // delete a key if exists
        FirebaseDatabase.getInstance().getReference().child("users-favourites/" + firebaseUser.getUid()).getRef().removeValue();
        // delete value if exists
        usersFavouritesRef = FirebaseDatabase.getInstance().getReference().child("users-favourites");
        usersFavouritesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (final DataSnapshot usersFavSnapshot : snapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: " + usersFavSnapshot.getKey());
                    Log.d(TAG, "onDataChange: " + usersFavSnapshot.getValue());

                    participantRef = FirebaseDatabase.getInstance().getReference().child("users-favourites/" + usersFavSnapshot.getKey() + "/" + firebaseUser.getUid());
                    Log.d(TAG, "participantsRef path: " + "users-favourites/" + usersFavSnapshot.getKey() + firebaseUser.getUid());
                    participantRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Log.d(TAG, "participantRef snapshot value: " + snapshot.getValue());
                            Log.d(TAG, "participantRef snapshot key: " + snapshot.getKey());
                            if (snapshot.getValue() != null) {
                                FirebaseDatabase.getInstance().getReference().child("users-favourites/" + usersFavSnapshot.getKey() + "/" + firebaseUser.getUid()).removeValue();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void removeUserFromCommonInterests() {
        // delete a key if exists
        FirebaseDatabase.getInstance().getReference().child("usersCommonInterestsCounter/" + firebaseUser.getUid()).getRef().removeValue();
        // delete value if exists
        usersCommonInterestsRef = FirebaseDatabase.getInstance().getReference().child("usersCommonInterestsCounter");
        usersCommonInterestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (final DataSnapshot usersCommonInterestsSnapshot : snapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: " + usersCommonInterestsSnapshot.getKey());
                    Log.d(TAG, "onDataChange: " + usersCommonInterestsSnapshot.getValue());

                    participantRef = FirebaseDatabase.getInstance().getReference().child("usersCommonInterestsCounter/" + usersCommonInterestsSnapshot.getKey() + "/" + firebaseUser.getUid());
                    Log.d(TAG, "participantsRef path: " + "users-favourites/" + usersCommonInterestsSnapshot.getKey() + firebaseUser.getUid());
                    participantRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Log.d(TAG, "participantRef snapshot value: " + snapshot.getValue());
                            Log.d(TAG, "participantRef snapshot key: " + snapshot.getKey());
                            if (snapshot.getValue() != null) {
                                FirebaseDatabase.getInstance().getReference().child("usersCommonInterestsCounter/" + usersCommonInterestsSnapshot.getKey() + "/" + firebaseUser.getUid()).removeValue();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void removeUsersEvents() {
        eventsRef = FirebaseDatabase.getInstance().getReference("events");
        eventsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (final DataSnapshot eventsSnapshot : snapshot.getChildren()) {
                    Log.d(TAG, "eventsSnapshotKey: " + eventsSnapshot.getKey());
                    Log.d(TAG, "eventsSnapshotValue: " + eventsSnapshot.getValue());

                    participantRef = FirebaseDatabase.getInstance().getReference().child("events/" + eventsSnapshot.getKey() + "/owner");
                    participantRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Log.d(TAG, "inside participantRef - snapshot value: " + snapshot.getValue());
                            // check if currently logged user owns an event, if so delete it
                            if (snapshot.getValue().equals(firebaseUser.getUid())) {
                                FirebaseDatabase.getInstance().getReference().child("events/" + eventsSnapshot.getKey()).removeValue();
                                FirebaseDatabase.getInstance().getReference().child("events-users/" + eventsSnapshot.getKey()).removeValue();

                                StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("events/" + eventsSnapshot.getKey() + "/icon.png");
                                storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "Events storage deletion successful");
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "Evets storage deletion failed");
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void removeUsersStorage() {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("users/" + firebaseUser.getUid() + "/profile.jpg");
        storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "FILE DELETED SUCCESSFULY");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "SOMETHING WENT WRONG");
            }
        });
    }

    private void removeUserInvitations() {
        FirebaseDatabase.getInstance().getReference("users-invitations").orderByChild(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            FirebaseDatabase.getInstance().getReference("users-invitations/" + dataSnapshot.getKey() + "/" + firebaseUser.getUid()).removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void removeUsersToken() {
        FirebaseDatabase.getInstance().getReference("tokens/" + firebaseUser.getUid()).removeValue();
    }
}
