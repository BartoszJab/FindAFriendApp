package com.uwb.findafriendapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.uwb.findafriendapp.R;
import com.uwb.findafriendapp.adapters.FindFriendsAdapter;
import com.uwb.findafriendapp.classes.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class FindFriendsFragment extends Fragment {

    private static String TAG = "FindFriendsFragment";
    private static StorageReference storageRef;
    private static int interestsCounter = 0;
    private String userID;
    private static DatabaseReference dbUser;
    private DatabaseReference dbUsersFavourites;
    private RelativeLayout progress;
    private View view;
    private TextView tvNoFriends;
    private RecyclerView.LayoutManager layoutManager;
    private FindFriendsAdapter usersAdapter;
    RecyclerView rvFindFriends;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.row_find_friends, container, false);

        //Firebase
        userID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        dbUser = FirebaseDatabase.getInstance().getReference().child("users");
        dbUsersFavourites = FirebaseDatabase.getInstance().getReference().child("users-favourites");

        tvNoFriends = view.findViewById(R.id.tvNoFriends);
        rvFindFriends = view.findViewById(R.id.rvFindFriends);
        rvFindFriends.getRecycledViewPool().setMaxRecycledViews(0, 0);
        layoutManager = new LinearLayoutManager(view.getContext());
        rvFindFriends.setHasFixedSize(true);
        rvFindFriends.setLayoutManager(layoutManager);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        dbUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot otherUserId) {

                dbUser.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot currentUser) {

                        dbUsersFavourites.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull final DataSnapshot currentUserFav) {
                                final DatabaseReference usersInvRef = FirebaseDatabase.getInstance().getReference().child("users-invitations");
                                usersInvRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot usersInvSnapshot) {

                                        // progress bar to load data
                                        progress = view.findViewById(R.id.llProgressBar);
                                        progress.setVisibility(view.VISIBLE);

                                        Log.d(TAG, "[200]: " + currentUser);
                                        Log.d(TAG, "[201]: " + currentUser.child(userID).child("mainInterestsCount").getValue());

                                        List<String> usersKey = new ArrayList<>();
                                        List<User> usersList = new ArrayList<>();
                                        List<List<String>> usersMi = new ArrayList<>();
                                        List<Integer> usersCiCount = new ArrayList<>();

                                        for(DataSnapshot otherUser : otherUserId.getChildren()){

                                            if(!userID.equals(otherUser.getKey())){
                                                boolean areUsersHaveCommonMI = false;
                                                if(otherUser.child("mainInterests").hasChildren() && currentUser.child(userID).child("mainInterests").hasChildren()){
                                                    for (DataSnapshot snapOther : otherUser.child("mainInterests").getChildren()) {
                                                        for (DataSnapshot snapCurrent : currentUser.child(userID).child("mainInterests").getChildren()) {
                                                            if(snapOther.getValue().equals(snapCurrent.getValue())){
                                                                Log.d(TAG, "Checking main interests: " + snapCurrent.getValue() + " <-> " + snapOther.getValue());
                                                                areUsersHaveCommonMI = true;
                                                                break;
                                                            }
                                                        }
                                                        if(areUsersHaveCommonMI) break;
                                                    }
                                                }

                                                boolean isUserInInvitations = false;
                                                for (DataSnapshot usersInvSnap : usersInvSnapshot.getChildren()) {

                                                    if (usersInvSnap.getKey().equals(otherUser.getKey())) {
                                                        HashMap map = (HashMap) usersInvSnap.getValue();
                                                        if (map.containsKey(userID)) {
                                                            Log.d(TAG, "map.containsKey(userID): " + map.containsKey(userID));
                                                            isUserInInvitations = true;
                                                            break;
                                                        }
                                                    }
                                                }

                                                if(areUsersHaveCommonMI){
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


                                                    Log.d(TAG, "isUserInInvitations: " + isUserInInvitations);
                                                    Log.d(TAG, "isUserInFavourites: " + isUserInFavourites);

                                                    if(!isUserInFavourites){
                                                        // if user is not in fav then check if is not in invitation list as well
                                                        if (!isUserInInvitations) {
                                                            List<String> singleUserMi = new ArrayList<>();
//                                                Log.d(TAG, "otherUserId -> " + otherUser);
                                                            for(DataSnapshot snapOtherUserMi : otherUser.child("mainInterests").getChildren()){
                                                                for(DataSnapshot snapCurrentUserMi : currentUser.child(userID).child("mainInterests").getChildren()){
                                                                    if(snapOtherUserMi.getValue().equals(snapCurrentUserMi.getValue())){
                                                                        singleUserMi.add(snapOtherUserMi.getValue().toString());
                                                                    }
                                                                }
                                                            }
                                                            usersMi.add(singleUserMi);

                                                            interestsCounter = 0;
                                                            for(DataSnapshot snapOtherUserCi : otherUser.child("interests").getChildren()){
                                                                for(DataSnapshot snapCurrentUserCi : currentUser.child(userID).child("interests").getChildren()){
                                                                    if(snapCurrentUserCi.getValue().equals(snapOtherUserCi.getValue())){
                                                                        interestsCounter++;
                                                                        break;
                                                                    }
                                                                }
                                                            }
                                                            usersCiCount.add(interestsCounter);

                                                            HashMap map = new HashMap();
                                                            map.put(otherUser.getKey(), interestsCounter);
                                                            DatabaseReference dbCommonInterestsRef = FirebaseDatabase.getInstance().getReference().child("usersCommonInterestsCounter").child(userID);
                                                            dbCommonInterestsRef.updateChildren(map);

                                                            User user = otherUser.getValue(User.class);

                                                            usersKey.add(otherUser.getKey());
                                                            usersList.add(user);
                                                        }

                                                    } else {
                                                        Log.d(TAG, "onDataChange: Users are in favourites - don't add");
                                                    }
                                                } else {
                                                    Log.d(TAG, "onDataChange: Users have no common main interests - don't add");
                                                }
                                            }

                                            Log.d(TAG, "USERS: " + usersList);
                                            Log.d(TAG, "KEYS: " + usersKey);
                                            Log.d(TAG, "MAIN INTERESTS: " + usersMi);
                                            Log.d(TAG, "COMMON INTERESTS COUNTER: " + usersCiCount);

                                            progress.setVisibility(view.GONE);
                                            usersAdapter = new FindFriendsAdapter(view.getContext(), usersList, usersKey, usersMi, usersCiCount);

                                            if (usersAdapter.getItemCount() == 0) tvNoFriends.setVisibility(View.VISIBLE);
                                            else tvNoFriends.setVisibility(View.GONE);

                                            rvFindFriends.setAdapter(usersAdapter);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

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

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: " + error.getMessage());
            }
        });
    }

    public static FindFriendsFragment getInstance(){
        return new FindFriendsFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
