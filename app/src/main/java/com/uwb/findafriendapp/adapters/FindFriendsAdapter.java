package com.uwb.findafriendapp.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.uwb.findafriendapp.ExpandedProfileActivity;
import com.uwb.findafriendapp.R;
import com.uwb.findafriendapp.classes.CircleTransform;
import com.uwb.findafriendapp.classes.User;

import java.util.List;
import java.util.Objects;

public class FindFriendsAdapter extends RecyclerView.Adapter<FindFriendsAdapter.UsersViewHolder> {

    private static final String TAG = "FindFriendsAdapter";
    private List<User> usersList;
    private List<String> usersKey;
    private LayoutInflater inflater;
    private static Context myContext;
    private DatabaseReference dbUsersCommonInterestsCounter;
    private List<List<String>> usersMi;
    private List<Integer> usersCiCount;

    public FindFriendsAdapter(Context context, List<User> usersList, List<String> usersKey, List<List<String>> usersMi, List<Integer> usersCiCount) {
        myContext = context;
        inflater = LayoutInflater.from(context);
        this.usersList = usersList;
        this.usersKey = usersKey;
        this.usersMi = usersMi;
        this.usersCiCount = usersCiCount;
    }

    @NonNull
    @Override
    public FindFriendsAdapter.UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemUser = inflater.inflate(R.layout.cardview_find_friends, parent, false);
        return new FindFriendsAdapter.UsersViewHolder(itemUser);
    }

    @Override
    public void onBindViewHolder(@NonNull FindFriendsAdapter.UsersViewHolder holder, final int position) {
        final String currentUserID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        holder.position = position;
        final String userKey = usersKey.get(position);

        User user = usersList.get(position);

        holder.setPeopleID(userKey);
        holder.setUsername(user.getUsername());
        holder.setAvatar(userKey);
        holder.setMainInterests(usersMi.get(position));
        holder.countCommonInterests(usersCiCount.get(position));

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
            dbUsersCommonInterestsCounter = FirebaseDatabase.getInstance().getReference().child("usersCommonInterestsCounter");
            dbUsersCommonInterestsCounter.child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Intent intent = new Intent(view.getContext(), ExpandedProfileActivity.class);
                    Log.d(TAG, "[2]: userKey -> " + userKey);
                    if (snapshot.child(userKey).exists()) {
                        int localInterestsCounter = usersCiCount.get(position);
                        Log.d(TAG, "[1] localInterestsCounter -> " + localInterestsCounter);
                        intent.putExtra("localInterestsCounter", localInterestsCounter);
                    } else {
                        intent.putExtra("localInterestsCounter", 0);
                    }
                    intent.putExtra("userID", userKey);
                    view.getContext().startActivity(intent);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d(TAG, "onCancelled: " + error.getMessage());
                }
            });
            }
        });

    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        String peopleID;
        View view;
        StorageReference storageRef;
        int position;

        public UsersViewHolder(View itemView){
            super(itemView);
            view = itemView;
            storageRef = FirebaseStorage.getInstance().getReference();
        }

        public void setPeopleID(String id){
            peopleID = id;
        }

        public void setUsername(String username){

            TextView findFriendUsername = view.findViewById(R.id.findFriendUsername);
            findFriendUsername.setText(username);
        }

        public void setMainInterests(List<String> mi){
            final TextView findFriendFirstMainInterest = view.findViewById(R.id.findFriendFirstMainInterest);
            final TextView findFriendSecondMainInterest = view.findViewById(R.id.findFriendSecondMainInterest);

//            Log.d(TAG, "[1] mi.size(): " + mi.get(0) + " + " + mi.get(1));

            if(mi.size() == 1){
                findFriendFirstMainInterest.setText(mi.get(0));
                findFriendSecondMainInterest.setVisibility(View.GONE);
            } else if(mi.size() == 2) {
                findFriendFirstMainInterest.setText(mi.get(0));
                findFriendSecondMainInterest.setText(mi.get(1));
            } else {
                findFriendFirstMainInterest.setVisibility(View.GONE);
                findFriendSecondMainInterest.setVisibility(View.GONE);
            }
        }

        public void countCommonInterests(int counter){
            final TextView findFriendOtherInterests = view.findViewById(R.id.findFriendOtherInterests);

            if(counter > 0) findFriendOtherInterests.setText("+ " + counter);
            else findFriendOtherInterests.setVisibility(View.GONE);
        }

        public void setAvatar(final String key){
            final ImageView ivFriendAvatar = view.findViewById(R.id.findFriendAvatar);
            storageRef = storageRef.child("users/" + key + "/profile.jpg");
            storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get()
                            .load(uri)
                            .centerCrop()
                            .resize(100, 100)
                            .onlyScaleDown()
                            .transform(new CircleTransform())
                            .into(ivFriendAvatar);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Picasso.get()
                            .load(R.drawable.default_user_avatar)
                            .centerCrop()
                            .resize(100, 100)
                            .onlyScaleDown()
                            .transform(new CircleTransform())
                            .into(ivFriendAvatar);
                }
            });
        }
    }
}