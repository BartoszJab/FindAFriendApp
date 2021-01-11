package com.uwb.findafriendapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.uwb.findafriendapp.Notifications.Client;
import com.uwb.findafriendapp.Notifications.Data;
import com.uwb.findafriendapp.Notifications.MyResponse;
import com.uwb.findafriendapp.Notifications.Sender;
import com.uwb.findafriendapp.Notifications.Token;
import com.uwb.findafriendapp.classes.CircleTransform;
import com.uwb.findafriendapp.classes.User;
import com.uwb.findafriendapp.fragments.APIService;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExpandedProfileActivity extends AppCompatActivity {

    private static final String TAG = "ExpandedProfileActivity";
    private ImageView ivExpandedProfileAvatar;
    private TextView tvExpandedProfileUsername;
    private TextView tvExpandedProfileDescription;
    private TextView tvExpandedProfileFirstInterest;
    private TextView tvExpandedProfileSecondInterest;
    private TextView tvExpandedProfileSimPercentage;
    private DatabaseReference dbRef;
    private DatabaseReference rulesRef;
    private DatabaseReference currentUserRef;
    private StorageReference storageRef;
    private StorageReference profileRef;
    private int similarityPercentage = 0;
    private int interestsCounter;
    private String userID;
    private String currentUserID;
    private RelativeLayout progress;
    private Button btnChat;
    private Button  btnExpandedProfileRemoveUser;
    private Button btnExpandedProfileAddToFavourites;
    private LinearLayout clickForInterestsExpand, tvInvitationSentMessage;

    APIService apiService;
    boolean notify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expanded_profile);

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        progress = findViewById(R.id.llProgressBar);
        progress.setVisibility(View.VISIBLE);

        ivExpandedProfileAvatar = findViewById(R.id.ivExpandedProfileAvatar);
        tvExpandedProfileUsername = findViewById(R.id.tvExpandedProfileUsername);
        tvExpandedProfileDescription = findViewById(R.id.tvExpandedProfileDescription);
        tvExpandedProfileFirstInterest = findViewById(R.id.tvExpandedProfileFirstInterest);
        tvExpandedProfileSecondInterest = findViewById(R.id.tvExpandedProfileSecondInterest);
        tvExpandedProfileSimPercentage = findViewById(R.id.tvExpandedProfileSimPercentage);
        btnExpandedProfileAddToFavourites = findViewById(R.id.btnExpandedProfileAddToFavourites);
        btnExpandedProfileRemoveUser = findViewById(R.id.btnExpandedProfileRemoveUser);
        btnChat = findViewById(R.id.btnChat);
        tvInvitationSentMessage = findViewById(R.id.tvInvitationSentMessage);
        clickForInterestsExpand = findViewById(R.id.clickForInterestsExpand);

        Intent intent = getIntent();
        userID = getIntent().getStringExtra("userID");
        interestsCounter = intent.getIntExtra("localInterestsCounter", 0);
        Log.d(TAG, "onCreate: userID -> " + userID + " interestsCounter -> " + interestsCounter);
        dbRef = FirebaseDatabase.getInstance().getReference().child("users").child(userID);

        // Geting rules from db
        rulesRef = FirebaseDatabase.getInstance().getReference().child("rules");
        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        currentUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserID);

        // getting and setting profile picture
        storageRef = FirebaseStorage.getInstance().getReference();
        profileRef = storageRef.child("users/" + userID + "/profile.jpg");
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get()
                        .load(uri)
                        .centerInside()
                        .fit()
                        .transform(new CircleTransform())
                        .into(ivExpandedProfileAvatar);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Picasso.get()
                        .load(R.drawable.default_user_avatar)
                        .centerInside()
                        .transform(new CircleTransform())
                        .resize(100, 100)
                        .onlyScaleDown()
                        .into(ivExpandedProfileAvatar);
            }
        });

        // Button to send an invitation
        btnExpandedProfileAddToFavourites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                updateToken(FirebaseInstanceId.getInstance().getToken());
                final String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

                DatabaseReference expandedUserRef = FirebaseDatabase.getInstance().getReference().child("users/" + userID);
                expandedUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot expandedUserSnap) {
                        FirebaseDatabase.getInstance().getReference().child("users-invitations/" + currentUserID + "/" + userID)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot usersInvSnap) {

                                        final Intent intent = new Intent(ExpandedProfileActivity.this, MainActivity.class);
                                        if (expandedUserSnap.getValue() != null) {
                                            Log.d(TAG, "User is not null: ");

                                            // check if user has already gotten an invitation
                                            if (usersInvSnap.exists()) {
                                                Log.d(TAG, "This user has already sent an invitation to currently logged user");
                                                Toast.makeText(ExpandedProfileActivity.this, "This user has already sent you an invitation", Toast.LENGTH_SHORT).show();
                                                Intent invitationIntent = new Intent(ExpandedProfileActivity.this, InvitationsActivity.class);

                                                startActivity(invitationIntent);
                                            } else {
                                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users").child(currentUserID);
                                                reference.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                                        HashMap map = new HashMap();
                                                        map.put(currentUserID, true);
                                                        DatabaseReference usersInvitations = FirebaseDatabase.getInstance().getReference().child("users-invitations/" + userID);
                                                        usersInvitations.updateChildren(map);
                                                        startActivity(intent);

                                                        User user = userSnapshot.getValue(User.class);
                                                        sendNotification(userID, user.getUsername());
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });

                                            }




                                        } else {
                                            Log.d(TAG, "User is null: ");
                                            Toast.makeText(ExpandedProfileActivity.this, "User not found", Toast.LENGTH_SHORT).show();

                                            startActivity(intent);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });



        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExpandedProfileActivity.this, ChatActivity.class);
                intent.putExtra("userID", userID);
                startActivity(intent);
            }
        });

        btnExpandedProfileRemoveUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseDatabase.getInstance().getReference().child("users-favourites/" + currentUserID + "/" + userID).removeValue();
                FirebaseDatabase.getInstance().getReference().child("users-favourites/" + userID + "/" + currentUserID).removeValue();

                finish();
            }
        });

        // button to show common interests with other users
        clickForInterestsExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ExpandedProfileInterestsActivity.class);
                intent.putExtra("currentUserID", currentUserID);
                intent.putExtra("otherUserID", userID);
                startActivity(intent);
            }
        });

        // Show add button if users are not in favourites, show delete, chat button otherwise
        DatabaseReference usersFavRef = FirebaseDatabase.getInstance().getReference().child("users-favourites/" + currentUserID + "/" + getIntent().getStringExtra("userID"));
        usersFavRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "PATH: " + "users-favourites/" + currentUserID + "/" + userID);
                // user is in favourites
                Log.d(TAG, "onDataChange: " + snapshot.getValue());
                if (snapshot.getValue() != null) {
                    Log.d(TAG, "is not null: ");
                    btnExpandedProfileAddToFavourites.setVisibility(View.GONE);
                    btnExpandedProfileRemoveUser.setVisibility(View.VISIBLE);
                    btnChat.setVisibility(View.VISIBLE);
                } else {
                    Log.d(TAG, "is null: ");
                    btnExpandedProfileAddToFavourites.setVisibility(View.VISIBLE);
                    btnExpandedProfileRemoveUser.setVisibility(View.GONE);
                    btnChat.setVisibility(View.GONE);
                }

                // check if profile was opened from invitation list
                boolean isFromInvitationList = getIntent().getBooleanExtra("isInvitator", false);
                if (isFromInvitationList) {
                    btnExpandedProfileAddToFavourites.setVisibility(View.GONE);
                    btnExpandedProfileRemoveUser.setVisibility(View.GONE);
                }

                DatabaseReference usersInvRef = FirebaseDatabase.getInstance().getReference().child("users-invitations/" + userID);
                usersInvRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(currentUserID)) {
                            btnExpandedProfileAddToFavourites.setVisibility(View.GONE);
                            btnExpandedProfileRemoveUser.setVisibility(View.GONE);
                            tvInvitationSentMessage.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // making all onDataChange things in one place
        rulesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot rulesSnapshot) {

                int maxInterestsPerUser = Integer.parseInt(rulesSnapshot.child("maxInterestsPerUser").getValue().toString());
                final int miPercent = 25;
                int ciPercent = 50 / maxInterestsPerUser;

                if(interestsCounter == maxInterestsPerUser){
                    similarityPercentage += 50;
                } else {
                    similarityPercentage += interestsCounter * ciPercent;
                }

                dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot snapshot) {
                        currentUserRef.child("mainInterests").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot2) {

                                // Show percentage
                                Log.d(TAG, "[1.1] what's similarityPercentage -> " + similarityPercentage);
                                Log.d(TAG, "[2.0] what is snapshot -> " + snapshot);
                                Log.d(TAG, "[2.1] what is snapshot2 -> " + snapshot2);
                                for(DataSnapshot snap : snapshot.child("mainInterests").getChildren()){
                                    for(DataSnapshot snap2 : snapshot2.getChildren()){
                                        Log.d(TAG, "onDataChange: snap.getvalue() = " + snap.getValue() + " snap2.getvalue " + snap2.getValue());
                                        if(snap.getValue().equals(snap2.getValue())){
                                            similarityPercentage += miPercent;
                                            break;
                                        }
                                    }
                                }
                                tvExpandedProfileSimPercentage.setText(similarityPercentage + "%");
                                Log.d(TAG, "[1.2] what's similarityPercentage -> " + similarityPercentage);

                                // Show user info
                                if(snapshot.child("username").exists()){
                                    String username = snapshot.child("username").getValue().toString();
                                    tvExpandedProfileUsername.setText(username);
                                }
                                if(snapshot.child("description").exists()){
                                    String description = snapshot.child("description").getValue().toString();
                                    tvExpandedProfileDescription.setText(description);
                                    tvExpandedProfileDescription.setMovementMethod(new ScrollingMovementMethod());
                                }
                                if(snapshot.child("mainInterests").exists()){
                                    int[] counter = {0};
                                    for(DataSnapshot snap : snapshot.child("mainInterests").getChildren()){
                                        counter[0]++;
                                        if(counter[0] == 1){
                                            tvExpandedProfileFirstInterest.setText("" + snap.getValue());
//                                            tvExpandedProfileFirstInterest.setBackgroundColor(Color.parseColor("#F1C40F"));

                                        } else if(counter[0] == 2){
                                            tvExpandedProfileSecondInterest.setText("" + snap.getValue());
//                                            tvExpandedProfileSecondInterest.setBackgroundColor(Color.parseColor("#F1C40F"));
                                        }
                                    }
                                    if(counter[0] == 1){
                                        tvExpandedProfileSecondInterest.setVisibility(View.GONE);
//                                        tvExpandedProfileSecondInterest.setText("-");
//                                        tvExpandedProfileSecondInterest.setBackgroundColor(Color.parseColor("#BBBBBB"));
                                    }
                                }
                                else {
                                    tvExpandedProfileFirstInterest.setVisibility(View.GONE);
                                    tvExpandedProfileSecondInterest.setVisibility(View.GONE);
//                                    tvExpandedProfileFirstInterest.setText("-");
//                                    tvExpandedProfileSecondInterest.setText("-");
                                }

                                progress.setVisibility(View.GONE);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.d(TAG, "onCancelled: " + error.getMessage());
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, "onCancelled: " + error.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: " + error.getMessage());
            }
        });



    }
    private void updateToken(String token) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("tokens");
        Token token1 = new Token(token);
        reference.child(currentUserID).setValue(token1);
    }

    private void sendNotification(String receiver, final String username) {
        DatabaseReference tokensRef = FirebaseDatabase.getInstance().getReference("tokens");
        Query query = tokensRef.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Token token = dataSnapshot.getValue(Token.class);
                    Data data = new Data(currentUserID, R.drawable.user_invitation, username + " sent you a friend request", "New invitation",
                            userID, "InvitationsActivity");

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success == 1) {
                                            Log.d(TAG, "response.code(): " + response.code() + " , response.body().success: " + response.body().success);
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

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