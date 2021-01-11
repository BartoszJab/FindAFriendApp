package com.uwb.findafriendapp.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.squareup.picasso.Target;
import com.uwb.findafriendapp.ExpandedEventActivity;
import com.uwb.findafriendapp.ProfileActivity;
import com.uwb.findafriendapp.R;
import com.uwb.findafriendapp.adapters.FindFriendsAdapter;
import com.uwb.findafriendapp.classes.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.wasabeef.picasso.transformations.BlurTransformation;

public class UserListFragment extends Fragment {

    private static final String TAG = "UserListFragment";

    private LinearLayout eventsIconsLayout, favEventsLayout, eventsLocationLayout, followedUsersLayout;
    private static int interestsCounter = 0;
    private View view;
    private TextView nothingToDisplay;

    private FirebaseUser firebaseUser;
    private RecyclerView rvFavUsers;
    private ProgressBar progressBar;
    private LinearLayout layoutFavourites;
    private RecyclerView.LayoutManager layoutManager;
    private static DatabaseReference dbUser;
    private DatabaseReference dbUsersFavourites;
    private FindFriendsAdapter usersAdapter;

    public static UserListFragment getInstance(){
        UserListFragment userListFragment = new UserListFragment();
        return userListFragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.row_user_list, container, false);

        eventsIconsLayout = view.findViewById(R.id.eventsIconsLayout);
        eventsLocationLayout = view.findViewById(R.id.eventsLocationLayout);
        favEventsLayout = view.findViewById(R.id.favEventsLayout);
        followedUsersLayout = view.findViewById(R.id.followedUsersLayout);
        nothingToDisplay = view.findViewById(R.id.nothingToDisplay);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        progressBar = view.findViewById(R.id.progressFavourites);
        layoutFavourites = view.findViewById(R.id.layoutFavourites);
        rvFavUsers = view.findViewById(R.id.rvFavUsers);
        rvFavUsers.getRecycledViewPool().setMaxRecycledViews(0, 0);
        layoutManager = new LinearLayoutManager(view.getContext());
        rvFavUsers.setHasFixedSize(true);
        rvFavUsers.setLayoutManager(layoutManager);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        progressBar.setVisibility(View.VISIBLE);
        layoutFavourites.setVisibility(View.GONE);
        // refresh events (clear layout and create it again)
        eventsLocationLayout.removeAllViews();
        eventsIconsLayout.removeAllViews();
        Log.d(TAG, "ON RESUUUUUUUUUUUMEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE: ");
        if (isAdded()) {
            Log.d(TAG, "IS RESUUUUUUUUUUUMEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE ADDED: ");
            favouriteEvents();
            favouriteUsers();
        }
    }

    private void favouriteUsers() {
        Log.d(TAG, "favouriteUsers: ");
        dbUser = FirebaseDatabase.getInstance().getReference().child("users");
        dbUsersFavourites = FirebaseDatabase.getInstance().getReference().child("users-favourites");

        dbUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot otherUserId) {

                dbUser.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot currentUser) {

                        dbUsersFavourites.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot currentUserFav) {

                                List<String> usersKey = new ArrayList<>();
                                List<User> usersList = new ArrayList<>();
                                List<List<String>> usersMi = new ArrayList<>();
                                List<Integer> usersCiCount = new ArrayList<>();

                                for(DataSnapshot otherUser : otherUserId.getChildren()){
                                    if(!firebaseUser.getUid().equals(otherUser.getKey())) {

                                        boolean isUserInFavourites = false;
                                        if(currentUserFav.hasChildren()){
                                            for(DataSnapshot favouritesSnap : currentUserFav.getChildren()){
                                                Log.d(TAG, "Checking is user in favourites: " + otherUser.getKey() + " <-> " + favouritesSnap.getKey());
                                                if(otherUser.getKey().equals(favouritesSnap.getKey())){
                                                    isUserInFavourites = true;
                                                    break;
                                                }
                                            }
                                        }

                                        if (isUserInFavourites) {
                                            Log.d(TAG, "onDataChange: Users are in favourites - don't add");

                                            List<String> singleUserMi = new ArrayList<>();
//                                                Log.d(TAG, "otherUserId -> " + otherUser);
                                            for(DataSnapshot snapOtherUserMi : otherUser.child("mainInterests").getChildren()){
                                                for(DataSnapshot snapCurrentUserMi : currentUser.child(firebaseUser.getUid()).child("mainInterests").getChildren()){
                                                    if(snapOtherUserMi.getValue().equals(snapCurrentUserMi.getValue())){
                                                        singleUserMi.add(snapOtherUserMi.getValue().toString());
                                                    }
                                                }
                                            }
                                            usersMi.add(singleUserMi);

                                            interestsCounter = 0;
                                            for(DataSnapshot snapOtherUserCi : otherUser.child("interests").getChildren()){
                                                for(DataSnapshot snapCurrentUserCi : currentUser.child(firebaseUser.getUid()).child("interests").getChildren()){
                                                    if(snapCurrentUserCi.getValue().equals(snapOtherUserCi.getValue())){
                                                        interestsCounter++;
                                                        break;
                                                    }
                                                }
                                            }
                                            usersCiCount.add(interestsCounter);

                                            HashMap map = new HashMap();
                                            map.put(otherUser.getKey(), interestsCounter);
                                            DatabaseReference dbCommonInterestsRef = FirebaseDatabase.getInstance().getReference().child("usersCommonInterestsCounter").child(firebaseUser.getUid());
                                            dbCommonInterestsRef.updateChildren(map);

                                            User user = otherUser.getValue(User.class);
                                            Log.d(TAG, "other user get value: " + otherUser.getValue());
// :)
                                            usersKey.add(otherUser.getKey());
                                            usersList.add(user);
                                        }
                                    } else {
                                        Log.d(TAG, "onDataChange: Same users - don't add");
                                    }

                                }

                                Log.d(TAG, "USERS: " + usersList);
                                Log.d(TAG, "KEYS: " + usersKey);
                                Log.d(TAG, "MAIN INTERESTS: " + usersMi);
                                Log.d(TAG, "COMMON INTERESTS COUNTER: " + usersCiCount);

                                if(usersList.size() == 0){
                                    followedUsersLayout.setVisibility(View.GONE);
                                }
                                if(followedUsersLayout.getVisibility() == View.GONE && favEventsLayout.getVisibility() == View.GONE){
                                    nothingToDisplay.setVisibility(View.VISIBLE);
                                } else {
                                    nothingToDisplay.setVisibility(View.GONE);
                                }

//                                usersAdapter = new FindFriendsAdapter(view.getContext(), usersList, usersKey);
                                usersAdapter = new FindFriendsAdapter(view.getContext(), usersList, usersKey, usersMi, usersCiCount);
//                                usersAdapter.notifyDataSetChanged();

                                progressBar.setVisibility(View.GONE);
                                layoutFavourites.setVisibility(View.VISIBLE);

                                rvFavUsers.setAdapter(usersAdapter);
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
    
    private void favouriteEvents() {
        DatabaseReference eventsUsersRef = FirebaseDatabase.getInstance().getReference().child("events-users");
        eventsUsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                byte numOfEvents = 0;
                for (final DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.getValue() instanceof HashMap) {

                        final Map<String, String> map = (HashMap<String, String>) dataSnapshot.getValue();

                        // user exists in an event
                        if (map.containsKey(firebaseUser.getUid())) {

                            numOfEvents++;
                            final ImageView imageView = new ImageView(getContext());
                            final TextView textView = new TextView(getContext());

                            Log.d(TAG, "events/" + dataSnapshot.getKey());
                            DatabaseReference iconEventRef = FirebaseDatabase.getInstance().getReference().child("events/" + dataSnapshot.getKey() + "/iconRef");
                            iconEventRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull final DataSnapshot snapshot) {

                                    DatabaseReference dbEvents = FirebaseDatabase.getInstance().getReference().child("events").child(dataSnapshot.getKey());
                                    dbEvents.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshotDbEvents) {
                                            if (snapshot.getValue() != null) {
                                                // reference to event's icon reference
                                                StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(snapshot.getValue().toString());
                                                storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        Picasso.get()
                                                                .load(uri)
                                                                .error(R.drawable.default_user_avatar)
                                                                .centerInside()
                                                                .fit()
                                                                .into(imageView);
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Picasso.get()
                                                                .load(R.drawable.question)
                                                                .centerInside()
                                                                .fit()
                                                                .into(imageView);
                                                    }
                                                });
                                            }

                                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                                    100, LinearLayout.LayoutParams.MATCH_PARENT, 1
                                            );
                                            params.setMargins(5,0,5,0);

                                            imageView.setLayoutParams(params);
                                            imageView.setPadding(20, 20, 20, 20);

                                            // mark event created by user
                                            if (map.get(firebaseUser.getUid()).equals("owner")) {
                                                @SuppressLint("ResourceType") String color = getResources().getString(R.color.main_interest);
                                                imageView.setBackgroundColor(Color.parseColor(color));
                                                textView.setBackgroundColor(Color.parseColor(color));
                                            } else {
                                                @SuppressLint("ResourceType") String color = getResources().getString(R.color.transparent_white_4);
                                                imageView.setBackgroundColor(Color.parseColor(color));
                                                textView.setBackgroundColor(Color.parseColor(color));
                                            }

                                            //setting name of event
                                            LinearLayout.LayoutParams paramsLocation = new LinearLayout.LayoutParams(
                                                    100, LinearLayout.LayoutParams.MATCH_PARENT, 1
                                            );
                                            paramsLocation.setMargins(5,0,5,0);

                                            textView.setText(snapshotDbEvents.child("localization").getValue().toString());
                                            textView.setLayoutParams(paramsLocation);
                                            textView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                                            textView.setMaxLines(2);
                                            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                                            eventsIconsLayout.addView(imageView);
                                            eventsLocationLayout.addView(textView);

                                            Log.d(TAG, "MAP " + map);

                                            imageView.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    final String ownerID = getKeyFromValue(map, "owner");
                                                    Log.d(TAG, "OWNER ID: " + ownerID);
                                                    Intent intent = new Intent(getContext(), ExpandedEventActivity.class);
                                                    intent.putExtra("visitEventID", dataSnapshot.getKey());
                                                    intent.putExtra("ivEventCreatorID", ownerID);
                                                    startActivity(intent);
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
                    }
                }

                if (numOfEvents <= 0) {
                    favEventsLayout.setVisibility(View.GONE);
                }
                if(followedUsersLayout.getVisibility() == View.GONE && favEventsLayout.getVisibility() == View.GONE){
                    nothingToDisplay.setVisibility(View.VISIBLE);
                } else {
                    nothingToDisplay.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: " + error.getMessage());
            }
        });
    }

    private static String getKeyFromValue(Map map, String value) {
        for (Object o : map.keySet()) {
            Log.d(TAG, "o.toString(): " + o.toString());
            if (map.get(o.toString()).equals(value)) {
                return o.toString();
            }
        }
        return null;
    }

}
