package com.uwb.findafriendapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.uwb.findafriendapp.adapters.ParticipantsAdapter;
import com.uwb.findafriendapp.classes.User;

import java.util.ArrayList;
import java.util.List;

public class EventParticipantsActivity extends AppCompatActivity {

    private static final String TAG = "EventParticipants";

    private TextView tvNoParticipants;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    List<String> usersKey = new ArrayList<>();
    ParticipantsAdapter usersAdapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_participants);

        tvNoParticipants = findViewById(R.id.tvNoParticipants);

        recyclerView = findViewById(R.id.recyclerViewParticipants);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);


        // Fill list of Users that take part in an event user expanded and add it to adapter
        Intent getDataIntent = getIntent();
        final String eventID = getDataIntent.getStringExtra("eventID");
        final DatabaseReference eventsUsersRef = FirebaseDatabase.getInstance().getReference().child("events-users/" + eventID);

        eventsUsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot eventsUsersSnapshot) {
                final List<User> usersList = new ArrayList<>();
                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");
                usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot usersRefSnapshot) {
                        for (DataSnapshot usersData : usersRefSnapshot.getChildren()) {
                            for (DataSnapshot eventsUsersData : eventsUsersSnapshot.getChildren()) {

                                if (usersData.getKey().equals(eventsUsersData.getKey())) {
                                    // id's are equal
                                    Log.d(TAG, "EVENTS USERS DATA VALUE: " + eventsUsersData.getValue());
                                    // dont show event's creator on participants list
                                    if (!eventsUsersData.getValue().equals("owner")) {
                                        User user = usersData.getValue(User.class);

                                        usersKey.add(usersData.getKey());
                                        usersList.add(user);
                                    }
                                }
                            }
                        }

                        usersAdapter = new ParticipantsAdapter(EventParticipantsActivity.this, usersList, usersKey);
                        Log.d(TAG, "USERS: " + usersList);
                        Log.d(TAG, "KEYS: " + usersKey);
                        for (User user : usersList) {
                            Log.d(TAG, "FOR EACH USER: " + user.getUsername());
                        }
                        if (usersList.size() != 0) {
                            usersAdapter.notifyDataSetChanged();
                            recyclerView.setAdapter(usersAdapter);
                        } else {
                            tvNoParticipants.setVisibility(View.VISIBLE);
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

}