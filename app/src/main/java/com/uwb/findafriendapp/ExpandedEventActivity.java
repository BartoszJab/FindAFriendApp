package com.uwb.findafriendapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
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
import com.squareup.picasso.Picasso;
import com.uwb.findafriendapp.classes.CircleTransform;
import com.uwb.findafriendapp.classes.User;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpandedEventActivity extends AppCompatActivity {

    private static final String TAG = "ExpandedEventActivity";

    // UI
    private LinearLayout editDeleteLayout;
    private ImageView ivEventIcon, ivCreatorImage;
    private TextView tvDescription, tvDate, tvTime, tvLocation;
    private Button btnJoinEvent, btnLeaveEvent, btnParticipants, btnDeleteEvent, btnEditEvent, btnEventChat;
    // Firebase
    private StorageReference storageRef;
    private DatabaseReference eventsRef, usersRef, eventsUsersRef;
    private FirebaseUser firebaseUser;
    private LinearLayout ivOwnerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expanded_event);

        final Intent intent = getIntent();

        // Firebase
        storageRef = FirebaseStorage.getInstance().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // UI
        editDeleteLayout = findViewById(R.id.editDeleteLayout);
        tvDescription = findViewById(R.id.tvExpandedEventDescription);
        tvDate = findViewById(R.id.tvExpandedEventDate);
        tvTime = findViewById(R.id.tvExpandedEventTime);
        tvLocation = findViewById(R.id.tvExpandedEventLocation);
        ivEventIcon = findViewById(R.id.ivExpandedEventIcon);
        ivCreatorImage = findViewById(R.id.ivExpandedEventCreatorImage);
        btnJoinEvent = findViewById(R.id.btnJoinEvent);
        btnLeaveEvent = findViewById(R.id.btnLeaveEvent);
        btnParticipants = findViewById(R.id.btnParticipants);
        btnDeleteEvent = findViewById(R.id.btnDeleteEvent);
        btnEventChat = findViewById(R.id.btnEventChat);
        btnEditEvent = findViewById(R.id.btnEditEvent);
        ivOwnerLayout = findViewById(R.id.ivOwnerLayout);

        tvDescription.setMovementMethod(new ScrollingMovementMethod());

        // Set event's data
        eventsRef = FirebaseDatabase.getInstance().getReference().child("events/" + intent.getStringExtra("visitEventID"));
        eventsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("date").exists()) {
                    String date = snapshot.child("date").getValue().toString();
                    tvDate.setText(date);
                }
                if (snapshot.child("eventDescription").exists()) {
                    String description = snapshot.child("eventDescription").getValue().toString();
                    tvDescription.setText(description);
                }
                if (snapshot.child("time").exists()) {
                    String time = snapshot.child("time").getValue().toString();
                    tvTime.setText(time);
                }
                if (snapshot.child("localization").exists()) {
                    String localization = snapshot.child("localization").getValue().toString();
                    tvLocation.setText(localization);
                }

                if (snapshot.child("iconRef").exists()) {
                    String iconRef = snapshot.child("iconRef").getValue().toString();
                    Log.d(TAG, "ICON REF: " + iconRef);
                    StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(iconRef);
                    storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.get()
                                    .load(uri)
                                    .centerInside()
                                    .transform(new CircleTransform())
                                    .fit()
                                    .into(ivEventIcon);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Picasso.get()
                                    .load(R.drawable.question)
                                    .centerInside()
                                    .transform(new CircleTransform())
                                    .fit()
                                    .into(ivEventIcon);
                        }
                    });
                }


                if (snapshot.child("owner").exists()) {
                    String creatorID = snapshot.child("owner").getValue().toString();
                    setEventCreatorImage(creatorID);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // CHECK IF USER PARTICIPATES IN AN EVENT
        // THIS WHEN USING events-users INDEX TO FIND EVENT'S PARTICIPANTS
        final String eventID = intent.getStringExtra("visitEventID");
        eventsRef = FirebaseDatabase.getInstance().getReference().child("events-users/" + eventID + "/" + firebaseUser.getUid());
        eventsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "EXPANDED EVENT ACTIVITY SNAP VALUE: " + snapshot.getValue());

                // if user exists in an event
                if (snapshot.getValue() != null) {
                    btnEventChat.setVisibility(View.VISIBLE);

                    if (snapshot.getValue().equals("owner")) {
                        btnJoinEvent.setVisibility(View.GONE);
                        ivCreatorImage.setVisibility(View.GONE);
                        ivOwnerLayout.setVisibility(View.GONE);
                        editDeleteLayout.setVisibility(View.VISIBLE);
                    } else {
                        btnJoinEvent.setVisibility(View.GONE);
                        btnLeaveEvent.setVisibility(View.VISIBLE);
                        ivOwnerLayout.setVisibility(View.VISIBLE);
                        editDeleteLayout.setVisibility(View.GONE);
                    }

                // if use doesnt exist in an event
                } else {
                    btnEventChat.setVisibility(View.GONE);
                    btnJoinEvent.setVisibility(View.VISIBLE);
                    btnLeaveEvent.setVisibility(View.GONE);
                    btnEditEvent.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // LOCATION LISTENER - GOOGLE MAPS
        tvLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String location = tvLocation.getText().toString();
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + location));
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });
        
        // JOINING EVENT LISTENER
        btnJoinEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String eventID = intent.getStringExtra("visitEventID");
                final String userID = firebaseUser.getUid();
                eventsRef = FirebaseDatabase.getInstance().getReference().child("events").child(eventID);
                usersRef = FirebaseDatabase.getInstance().getReference().child("users").child(userID);
                //eventsUsersRef = FirebaseDatabase.getInstance().getReference().child("events-users").child(eventID);
                eventsUsersRef = FirebaseDatabase.getInstance().getReference().child("events-users");

                // Check if event exists
                DatabaseReference expandedEventRef = FirebaseDatabase.getInstance().getReference().child("events/" + eventID);
                expandedEventRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // EVENT EXISTS
                        if (snapshot.getValue() != null) {
                            Log.d(TAG, "event is not null: ");

                            eventsUsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot eventsUsersSnapshot) {
                                    byte eventsCounter = 0;
                                    for (DataSnapshot eventsUsersData : eventsUsersSnapshot.getChildren()) {
                                        Map<String, String> map = (Map<String, String>) eventsUsersData.getValue();
                                        if (map.containsKey(firebaseUser.getUid()) && !map.get(firebaseUser.getUid()).equals("owner")) {
                                            eventsCounter += 1;
                                        }
                                        Log.d(TAG, "onDataChange: " +eventsUsersData.getValue());
                                    }
                                    Log.d(TAG, "EVENTS COUNTER: " + eventsCounter);
                                    if (eventsCounter < 3) {
                                        HashMap hashMap = new HashMap();
                                        hashMap.put(firebaseUser.getUid(), "participant");
                                        eventsUsersRef.child(eventID).updateChildren(hashMap);
                                        finish();
                                    } else {
                                        Toast.makeText(ExpandedEventActivity.this, "You can take part only in up to 3 events", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        // EVENT DOESNT EXIST
                        } else {
                            Log.d(TAG, "event is null: ");
                            Toast.makeText(ExpandedEventActivity.this, "Event not found", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



            }
        });

        // LEAVING EVENT LISTENER
        btnLeaveEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // THIS WHEN USING events-users INDEX TO FIND EVENT'S PARTICIPANTS
                eventsRef = FirebaseDatabase.getInstance().getReference().child("events-users/" + eventID + "/" + firebaseUser.getUid());
                eventsRef.getRef().removeValue();
                finish();

            }
        });

        // GO TO EDIT EVENT ACTIVITY
        btnEditEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExpandedEventActivity.this, CreateEventActivity.class);
                intent.putExtra("isEventEdition", true);
                intent.putExtra("eventID", eventID);
                Log.d(TAG, "onClick: " + eventID);
                startActivity(intent);
            }
        });

        // DELETING EVENT IF USER OWNS IT
        btnDeleteEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("events/" + eventID);
                databaseReference.getRef().removeValue();

//                eventsRef = FirebaseDatabase.getInstance().getReference().child("events-users/" + eventID + "/" + firebaseUser.getUid());
                eventsRef = FirebaseDatabase.getInstance().getReference().child("events-users/" + eventID);
                eventsRef.getRef().removeValue();

                StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("events/" + eventID + "/icon.png");
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

                finish();
            }
        });

        // VISITING EVENT'S CREATOR PROFILE LISTENER
        ivCreatorImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent getDataIntent = getIntent();
                Intent intent = new Intent(ExpandedEventActivity.this, ExpandedProfileActivity.class);
                Log.d(TAG, "CREATOR ID   111: " + getDataIntent.getStringExtra("ivEventCreatorID"));
                intent.putExtra("userID", getDataIntent.getStringExtra("ivEventCreatorID"));
                startActivity(intent);
            }
        });


        // GO TO PARTICIPANTS LIST
        btnParticipants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExpandedEventActivity.this, EventParticipantsActivity.class);
                intent.putExtra("eventID", eventID);
                startActivity(intent);
            }
        });

        // GO TO EVENT'S CHAT
        btnEventChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExpandedEventActivity.this, EventChatActivity.class);
                intent.putExtra("eventID", eventID);
                startActivity(intent);
            }
        });

    }



    public void setEventCreatorImage(String ownerID) {
        storageRef = storageRef.child("users/" + ownerID + "/profile.jpg");
        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get()
                        .load(uri)
                        .centerCrop()
                        .transform(new CircleTransform())
                        .fit()
                        .into(ivCreatorImage);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Picasso.get()
                        .load(R.drawable.default_user_avatar)
                        .centerCrop()
                        .transform(new CircleTransform())
                        .fit()
                        .into(ivCreatorImage);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
    }
}