package com.uwb.findafriendapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.uwb.findafriendapp.classes.CircleTransform;
import com.uwb.findafriendapp.dialogs.ConfirmPasswordDialog;
import com.uwb.findafriendapp.dialogs.EditAgeDialog;
import com.uwb.findafriendapp.dialogs.EditEmailDialog;
import com.uwb.findafriendapp.dialogs.EditNameDialog;
import com.uwb.findafriendapp.dialogs.EditPhoneDialog;

import java.util.HashMap;

import jp.wasabeef.picasso.transformations.BlurTransformation;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity1111";
    public TextView tvName, tvLastName, tvAge, tvPhone, tvEmail, tvUsername;
    private ImageView ivNameEditPhoto, ivAgeEditPhoto, ivPhoneEditPhoto, ivEmailEditPhoto;
    private Button btnDeleteAccount, btnLogOut;
    private EditText etDescription;
    private DatabaseReference dbRef;
    private FirebaseAuth.AuthStateListener myAuthStateListener;
    private FirebaseUser firebaseUser;
    private ImageView ivAvatar, ivBigAvatar, ivChangeAvatar;
    private StorageReference storageRef, profileRef;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Intent intent = getIntent();

        ivChangeAvatar = findViewById((R.id.ivChangeAvatar));
        tvUsername = findViewById(R.id.tvUsername);
        tvName = findViewById(R.id.tvName);
        tvAge = findViewById(R.id.tvAge);
        tvPhone = findViewById(R.id.tvPhone);
        tvEmail = findViewById(R.id.tvEmail);
        ivNameEditPhoto = findViewById(R.id.ivNameEditPhoto);
        ivAgeEditPhoto = findViewById(R.id.ivAgeEditPhoto);
        ivPhoneEditPhoto = findViewById(R.id.ivPhoneEditPhoto);
        ivEmailEditPhoto = findViewById(R.id.ivEmailEditPhoto);
        btnLogOut = findViewById(R.id.bLogout);
        btnDeleteAccount = findViewById(R.id.bDelete);
        ivAvatar = findViewById(R.id.ivAvatar);
        ivBigAvatar = findViewById(R.id.ivBigAvatar);

        // Firebase
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbRef = FirebaseDatabase.getInstance().getReference().child("users").child(userID);
        tvEmail.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // Firbase get user photoUrl and change avatar method
        storageRef = FirebaseStorage.getInstance().getReference();
        profileRef = storageRef.child("users/" + userID + "/profile.jpg");
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get()
                        .load(uri)
                        .transform(new CircleTransform())
                        .fit()
                        .centerInside()
                        .into(ivAvatar);
                Picasso.get()
                        .load(uri)
                        .fit()
                        .centerInside()
                        .transform(new BlurTransformation(ProfileActivity.this, 10, 1))
                        .into(ivBigAvatar);
            }
        });

        ivAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(openGalleryIntent, 1000);
            }
        });
        ivChangeAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(openGalleryIntent, 1000);
            }
        });

        // Button to edit first name and last name
        final FragmentManager fragmentManager = this.getSupportFragmentManager();
        ivNameEditPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditNameDialog dialog = new EditNameDialog();
                dialog.show(fragmentManager, "EditNameDialog");
            }
        });

        // Button to edit age
        ivAgeEditPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditAgeDialog dialog = new EditAgeDialog();
                dialog.show(fragmentManager, "EditAgeDialog");
            }
        });

        // Button to edit phone number
        ivPhoneEditPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditPhoneDialog dialog = new EditPhoneDialog();
                dialog.show(fragmentManager, "EditPhoneDialog");
            }
        });

        // Button to edit email address
        ivEmailEditPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditEmailDialog dialog = new EditEmailDialog();
                dialog.show(fragmentManager, "EditEmailDialog");
            }
        });

        etDescription = findViewById(R.id.etDescription);
        etDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String newDescription = etDescription.getText().toString();
                HashMap map = new HashMap();
                map.put("description", newDescription);
                dbRef.updateChildren(map);
            }
        });

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("username").exists()){
                    String username = snapshot.child("username").getValue().toString();
                    tvUsername.setText(username);
                }
                if(snapshot.child("name").exists()){
                    String name = snapshot.child("name").getValue().toString();
                    tvName.setText(name);
                }
                if(!snapshot.child("age").getValue().toString().equals("0")){
                    String age = snapshot.child("age").getValue().toString();
                    tvAge.setText(age);
                }
                else {
                    String age = "";
                    tvAge.setText(age);
                }
                if(snapshot.child("phoneNumber").exists()){
                    String phone = snapshot.child("phoneNumber").getValue().toString();
                    tvPhone.setText(phone);
                }

                if(snapshot.child("description").exists()){
                    String description = snapshot.child("description").getValue().toString();
                    etDescription.setText(description);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        btnDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show password confirmation dialog with delete account functionallity
                ConfirmPasswordDialog dialog = new ConfirmPasswordDialog();
                dialog.show(fragmentManager, "ConfirmPasswordDialog");
            }
        });

        setupFirebaseListener();
        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(myAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (myAuthStateListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(myAuthStateListener);
        }
    }

    private void setupFirebaseListener() {
        myAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged: signed_in " + user.getUid());
                } else {
                    Log.d(TAG, "onAuthStateChanged: signed_out");
                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        };
    }

    public void goToInterestsActivity(View view) {
        Intent intent = new Intent(ProfileActivity.this, InterestsActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 1000 - set profile picture
        if(requestCode == 1000){
            if(resultCode == Activity.RESULT_OK){
                Uri imageUri = data.getData();
                uploadImageToFireBase(imageUri);
            }
        }
    }

    private void uploadImageToFireBase(Uri imageUri){
        final StorageReference fileRef = storageRef.child("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/profile.jpg");
        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get()
                                .load(uri)
                                .transform(new CircleTransform())
//                                .resize(90, 90)
                                .centerInside()
                                .fit()
                                .into(ivAvatar);
                        Picasso.get()
                                .load(uri)
                                .transform(new BlurTransformation(ProfileActivity.this, 10, 1))
                                .into(ivBigAvatar);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileActivity.this, "Image Upload Failed.", Toast.LENGTH_LONG).show();
            }
        });
    }
}
