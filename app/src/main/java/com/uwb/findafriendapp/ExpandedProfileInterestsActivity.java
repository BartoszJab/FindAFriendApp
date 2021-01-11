package com.uwb.findafriendapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.uwb.findafriendapp.classes.CircleTransform;

import java.util.ArrayList;
import java.util.List;

public class ExpandedProfileInterestsActivity extends AppCompatActivity {

    private static final String TAG = "ExpandedProfileInterestsActivity";
    private String currentUserID;
    private String otherUserID;
    DatabaseReference dbUser;
    private ImageView ivCurrentUser;
    private ImageView ivOtherUser;
    private TextView tvCurrentUserNickname, tvOtherUserNickname;

    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expanded_profile_interests);

        final LinearLayout mainLayout = findViewById(R.id.mainLayout);
        final LinearLayout.LayoutParams parametersLL = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        // Getting intent data
        currentUserID = getIntent().getStringExtra("currentUserID");
        otherUserID = getIntent().getStringExtra("otherUserID");

        // Setting the avatars of users
        ivCurrentUser = findViewById(R.id.ivCurrentUser);
        ivOtherUser = findViewById(R.id.ivOtherUser);

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        StorageReference profileRef = storageRef.child("users/" + currentUserID + "/profile.jpg");
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get()
                        .load(uri)
                        .transform(new CircleTransform())
                        .fit()
                        .into(ivCurrentUser);
            }
        });

        profileRef = storageRef.child("users/" + otherUserID + "/profile.jpg");
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get()
                        .load(uri)
                        .transform(new CircleTransform())
                        .fit()
                        .into(ivOtherUser);
            }
        });

        // Makin' references to users in db
        dbUser = FirebaseDatabase.getInstance().getReference().child("users");

        // Searching for common main interests and adding it to ArrayList
        // Getting main interests from otherUser
        dbUser.child(otherUserID).child("mainInterests").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot snapshotOther) {
                // Getting main interests from currentUser
                dbUser.child(currentUserID).child("mainInterests").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshotCurrent) {
                        List<String> mainInterestsBoth = new ArrayList<>();
                        List<String> mainInterestsOtherUser = new ArrayList<>();
                        List<String> mainInterestsCurrentUser = new ArrayList<>();

                        for (final DataSnapshot snapOther : snapshotOther.getChildren()) {
                            for (DataSnapshot snapCurrent : snapshotCurrent.getChildren()) {
                                Log.d(TAG, "onDataChange: " + snapCurrent.getValue() + " + " + snapOther.getValue());
                                if (snapOther.getValue().toString().equals(snapCurrent.getValue().toString())) {
                                    mainInterestsBoth.add(snapOther.getValue().toString());
                                    break;
                                } else {
                                    mainInterestsCurrentUser.add(snapCurrent.getValue().toString());
                                    mainInterestsOtherUser.add(snapOther.getValue().toString());
                                }
                            }
                        }
                        if(mainInterestsOtherUser.size() == 0){
                            for (DataSnapshot snapCurrent : snapshotCurrent.getChildren()) {
                                mainInterestsCurrentUser.add(snapCurrent.getValue().toString());
                            }
                        }

                        // colors
                        @SuppressLint("ResourceType") String mainColor = getResources().getString(R.color.main_interest);

                        mainInterestsCurrentUser = checkForDuplicates(mainInterestsBoth, mainInterestsCurrentUser);
                        mainInterestsOtherUser = checkForDuplicates(mainInterestsBoth, mainInterestsOtherUser);

                        mainInterestsCurrentUser = removeDuplicates(mainInterestsCurrentUser);
                        mainInterestsOtherUser = removeDuplicates(mainInterestsOtherUser);

                        Log.d(TAG, "onCreate: BOTH: " + mainInterestsBoth.size() + " CURRENT: " + mainInterestsCurrentUser.size() + " OTHER: " + mainInterestsOtherUser.size());

                        LinearLayout tempLinearLayout;
                        if (mainInterestsBoth.size() == 1) {
                            // when users have 1 common main interest
                            mainInterestsCurrentUser.remove(mainInterestsBoth.get(0));
                            mainInterestsOtherUser.remove(mainInterestsBoth.get(0));

                            tempLinearLayout = new LinearLayout(ExpandedProfileInterestsActivity.this);
                            tempLinearLayout.setLayoutParams(parametersLL);
                            tempLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                            TextView tv = createTextView(mainInterestsBoth.get(0), mainColor);
                            tempLinearLayout.addView(tv);
                            mainLayout.addView(tempLinearLayout);

                            if(mainInterestsCurrentUser.size() != 0 || mainInterestsOtherUser.size() != 0){
                                tempLinearLayout = new LinearLayout(ExpandedProfileInterestsActivity.this);
                                tempLinearLayout.setLayoutParams(parametersLL);
                                tempLinearLayout.setOrientation(LinearLayout.HORIZONTAL);

                                if(mainInterestsCurrentUser.size() == 1){
                                    TextView tvCurrent = createTextView(mainInterestsCurrentUser.get(0), mainColor);
                                    tempLinearLayout.addView(tvCurrent);
                                } else {
                                    TextView tvCurrent = createBlankTextView();
                                    tempLinearLayout.addView(tvCurrent);
                                }

                                if(mainInterestsOtherUser.size() == 1){
                                    TextView tvOther = createTextView(mainInterestsOtherUser.get(0), mainColor);
                                    tempLinearLayout.addView(tvOther);
                                } else {
                                    TextView tvOther = createBlankTextView();
                                    tempLinearLayout.addView(tvOther);
                                }

                                mainLayout.addView(tempLinearLayout);
                            }

                        } else if (mainInterestsBoth.size() == 2) {
                            // when users have 2 common main interest
                            mainInterestsOtherUser.clear();
                            mainInterestsCurrentUser.clear();

                            for(String element : mainInterestsBoth){
                                tempLinearLayout = new LinearLayout(ExpandedProfileInterestsActivity.this);
                                tempLinearLayout.setLayoutParams(parametersLL);
                                tempLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                                TextView tv = createTextView(element, mainColor);
                                tempLinearLayout.addView(tv);
                                mainLayout.addView(tempLinearLayout);
                            }
                        } else {
                            // when users have no common main interests
                            int limit = Math.max(mainInterestsCurrentUser.size(), mainInterestsOtherUser.size());
                            for(int i = 0; i < limit; i++){
                                tempLinearLayout = new LinearLayout(ExpandedProfileInterestsActivity.this);
                                tempLinearLayout.setLayoutParams(parametersLL);
                                tempLinearLayout.setOrientation(LinearLayout.HORIZONTAL);

                                if(mainInterestsCurrentUser.size() > i){
                                    TextView tvCurrent = createTextView(mainInterestsCurrentUser.get(i), mainColor);
                                    tempLinearLayout.addView(tvCurrent);
                                } else {
                                    TextView tvCurrent = createBlankTextView();
                                    tempLinearLayout.addView(tvCurrent);
                                }

                                if(mainInterestsOtherUser.size() > i){
                                    TextView tvOther = createTextView(mainInterestsOtherUser.get(i), mainColor);
                                    tempLinearLayout.addView(tvOther);
                                } else {
                                    TextView tvOther = createBlankTextView();
                                    tempLinearLayout.addView(tvOther);
                                }

                                mainLayout.addView(tempLinearLayout);
                            }
                        }
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

        // Setting nickname on top of avatars
        dbUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvCurrentUserNickname = findViewById(R.id.tvCurrentUserNickname);
                tvCurrentUserNickname.setText(snapshot.child(currentUserID).child("username").getValue().toString());

                tvOtherUserNickname = findViewById(R.id.tvOtherUserNickname);
                tvOtherUserNickname.setText(snapshot.child(otherUserID).child("username").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Searching for common regular interests and adding it to ArrayList
        // Getting regular interests from otherUser
        dbUser.child(otherUserID).child("interests").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot snapshotOther) {
                dbUser.child(currentUserID).child("interests").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshotCurrent) {
                        List<String> regularInterestsBoth = new ArrayList<>();
                        List<String> regularInterestsOtherUser = new ArrayList<>();
                        List<String> regularInterestsCurrentUser = new ArrayList<>();

                        for (final DataSnapshot snapOther : snapshotOther.getChildren()) {
                            for (DataSnapshot snapCurrent : snapshotCurrent.getChildren()) {
                                Log.d(TAG, "onDataChange: " + snapOther.getValue() + " + " + snapCurrent.getValue());
                                if (snapOther.getValue().toString().equals(snapCurrent.getValue().toString())) {
                                    regularInterestsBoth.add(snapCurrent.getValue().toString());
                                    break;
                                } else {
                                    regularInterestsCurrentUser.add(snapCurrent.getValue().toString());
                                    regularInterestsOtherUser.add(snapOther.getValue().toString());
                                }
                            }
                        }
                        if(regularInterestsOtherUser.size() == 0){
                            for (DataSnapshot snapCurrent : snapshotCurrent.getChildren()) {
                                regularInterestsCurrentUser.add(snapCurrent.getValue().toString());
                            }
                        }

                        // colors
                        @SuppressLint("ResourceType") String regularColor = getResources().getString(R.color.regular_interest);

                        regularInterestsCurrentUser = checkForDuplicates(regularInterestsBoth, regularInterestsCurrentUser);
                        regularInterestsOtherUser = checkForDuplicates(regularInterestsBoth, regularInterestsOtherUser);

                        regularInterestsCurrentUser = removeDuplicates(regularInterestsCurrentUser);
                        regularInterestsOtherUser = removeDuplicates(regularInterestsOtherUser);

                        LinearLayout tempLinearLayout;
                        for(String element : regularInterestsBoth){
                            tempLinearLayout = new LinearLayout(ExpandedProfileInterestsActivity.this);
                            tempLinearLayout.setLayoutParams(parametersLL);
                            tempLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                            TextView tv = createTextView(element, regularColor);
                            tempLinearLayout.addView(tv);
                            mainLayout.addView(tempLinearLayout);
                        }

                        int limit = Math.max(regularInterestsCurrentUser.size(), regularInterestsOtherUser.size());
                        for(int i = 0; i < limit; i++){
                            tempLinearLayout = new LinearLayout(ExpandedProfileInterestsActivity.this);
                            tempLinearLayout.setLayoutParams(parametersLL);
                            tempLinearLayout.setOrientation(LinearLayout.HORIZONTAL);

                            if(regularInterestsCurrentUser.size() > i){
                                TextView tvCurrent = createTextView(regularInterestsCurrentUser.get(i), regularColor);
                                tempLinearLayout.addView(tvCurrent);
                            } else {
                                TextView tvCurrent = createBlankTextView();
                                tempLinearLayout.addView(tvCurrent);
                            }

                            if(regularInterestsOtherUser.size() > i){
                                TextView tvOther = createTextView(regularInterestsOtherUser.get(i), regularColor);
                                tempLinearLayout.addView(tvOther);
                            } else {
                                TextView tvOther = createBlankTextView();
                                tempLinearLayout.addView(tvOther);
                            }

                            mainLayout.addView(tempLinearLayout);
                        }


                        Log.d(TAG, "onCreate: BOTH: " + regularInterestsBoth.size() + " CURRENT: " + regularInterestsCurrentUser.size() + " OTHER: " + regularInterestsOtherUser.size());
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

    public List<String> removeDuplicates(List<String> list) {
        List<String> newArrayList = new ArrayList<>();
        for (String element : list) {
            if (!newArrayList.contains(element)) {
                newArrayList.add(element);
            }
        }
        return newArrayList;
    }

    public List<String> checkForDuplicates(List<String> duplicates, List<String> list){
        List<String> newArrayList = new ArrayList<>();
        newArrayList.addAll(list);
        for (String element : list) {
            if (duplicates.contains(element)) {
                newArrayList.remove(element);
            }
        }
        return newArrayList;
    }

    TextView createTextView(String nameOfInterest, String color) {
        TextView tv = new TextView(ExpandedProfileInterestsActivity.this);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1);

        tv.setText(nameOfInterest);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(0 , 12, 0 ,12);
        params.setMargins(12, 12, 6, 0);
        tv.setLayoutParams(params);
        tv.setBackgroundColor(Color.parseColor(color));

        return tv;
    }

    TextView createBlankTextView() {
        TextView tv = new TextView(ExpandedProfileInterestsActivity.this);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1);

        tv.setGravity(Gravity.CENTER);
        tv.setPadding(0 , 12, 0 ,12);
        params.setMargins(12, 12, 6, 0);
        tv.setLayoutParams(params);

        return tv;
    }
}