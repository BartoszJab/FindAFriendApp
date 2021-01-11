package com.uwb.findafriendapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.uwb.findafriendapp.adapters.InvitationsAdapter;
import com.uwb.findafriendapp.adapters.ParticipantsAdapter;
import com.uwb.findafriendapp.classes.User;

import java.util.ArrayList;
import java.util.List;

public class InvitationsActivity extends AppCompatActivity {

    public static final String TAG = "InvitationsActivity";

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private FirebaseUser firebaseUser;
    List<String> invitatorsKeys = new ArrayList<>();
    InvitationsAdapter invitatorsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitations);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        recyclerView = findViewById(R.id.recyclerViewInvitations);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        final DatabaseReference usersFavRef = FirebaseDatabase.getInstance().getReference().child("users-invitations/" + firebaseUser.getUid());
        usersFavRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot usersFavSnapshot) {
                final List<User> invitatorsList = new ArrayList<>();
                // someone invited currently logged user
                if (usersFavSnapshot.getValue() != null) {
                    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");
                    usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot usersSnapshot) {
                            for (DataSnapshot usersFavData : usersFavSnapshot.getChildren()) {
                                for (DataSnapshot usersData : usersSnapshot.getChildren()) {
                                    if (usersData.getKey().equals(usersFavData.getKey())) {
                                        User user = usersData.getValue(User.class);

                                        invitatorsKeys.add(usersData.getKey());
                                        invitatorsList.add(user);
                                    }
                                }
                            }

                            invitatorsAdapter = new InvitationsAdapter(InvitationsActivity.this, invitatorsList, invitatorsKeys);

                            invitatorsAdapter.notifyDataSetChanged();
                            recyclerView.setAdapter(invitatorsAdapter);
                            Log.d(TAG, "onDataChange: ");

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
}