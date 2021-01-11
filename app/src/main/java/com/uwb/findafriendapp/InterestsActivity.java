package com.uwb.findafriendapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class InterestsActivity extends AppCompatActivity {

    private static final String TAG = "InterestsActivity";
    private DatabaseReference dbRefInterests, dbRefUser, dbRefRules;
    int maxInterestsPerUser = 0;
    private long mLastClickTime = 0;

    private String mainColor;
    private String regularColor;
    private String transparent_white_8;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interests);

        Intent intent = getIntent();

        final LinearLayout.LayoutParams parametersLL = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        final LinearLayout.LayoutParams parametersBTN = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1);
        final ArrayList<DataSnapshot> interests = new ArrayList<>();
        final LinearLayout verticalLayout = findViewById(R.id.buttonLayout);

        // Firabase
        dbRefInterests = FirebaseDatabase.getInstance().getReference().child("interests");
        dbRefRules = FirebaseDatabase.getInstance().getReference().child("rules");
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbRefUser = FirebaseDatabase.getInstance().getReference().child("users").child(userID);

        // Getting value to maxInterestsPerUser
        dbRefRules.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int max = Integer.parseInt(snapshot.child("maxInterestsPerUser").getValue().toString());
                setMaxInterestsPerUser(max);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Declaring colors
        mainColor = getResources().getString(R.color.main_interest);
        regularColor = getResources().getString(R.color.regular_interest);
        transparent_white_8 = getResources().getString(R.color.transparent_white_8);

        // Generating buttons v2
        dbRefInterests.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot snapshot1) {
                // snapshot1 - Generating buttons part
                dbRefUser.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot snapshot2) {
                        // snapshot2 - Searching for regular (green) or main (yellow) interests part
                        LinearLayout tempLinearLayout = new LinearLayout(InterestsActivity.this);
                        int counter = 0;
                        for (final DataSnapshot snap1 : snapshot1.getChildren()) {
                            // button - name of LinearLayout instead old simple buttons
                            final LinearLayout button = new LinearLayout(InterestsActivity.this);
                            final ImageView imageView = new ImageView(InterestsActivity.this); // image of interest
                            final TextView textView = new TextView(InterestsActivity.this); // name of interest

                            // text params
                            LinearLayout.LayoutParams parametersTV = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);
                            parametersTV.setMargins(0, 10, 0, 0);
                            textView.setLayoutParams(parametersTV);
                            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                            textView.setText(snap1.getValue().toString());
                            textView.setTypeface(Typeface.DEFAULT_BOLD);
                            textView.setTextColor(R.color.colorPrimary);

                            // image params
                            LinearLayout.LayoutParams parametersIV = new LinearLayout.LayoutParams(100, 100);
                            parametersIV.setMargins(0, 10, 0, 0);
                            imageView.setLayoutParams(parametersIV);
                            int resID = getResources().getIdentifier(snap1.getKey(), "drawable", getPackageName());
                            Picasso.get().load(resID).placeholder(R.drawable.placeholder).fit().centerInside().into(imageView);
                            imageView.getDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);

                            // button params
                            LinearLayout.LayoutParams parametersBTN = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    1);
                            parametersBTN.setMargins(3, 3, 3, 3);
                            button.setTag(snap1.getKey());
                            button.setPadding(20, 20, 20, 20);
                            button.setOrientation(LinearLayout.VERTICAL);
                            button.setGravity(Gravity.CENTER_HORIZONTAL);
                            button.setLayoutParams(parametersBTN);
                            button.setBackgroundColor(Color.parseColor(transparent_white_8));

                            button.addView(imageView);
                            button.addView(textView);

                            // Searching for regular (green) or main (yellow) interests
                            if (snapshot2.child("interests").hasChild(snap1.getKey())) {
                                button.setBackgroundColor(Color.parseColor(regularColor));
                            } else if (snapshot2.child("mainInterests").hasChild(snap1.getKey())) {
                                button.setBackgroundColor(Color.parseColor(mainColor));
                            }

                            int interestsCount = Integer.parseInt(snapshot2.child("interestsCount").getValue().toString());
                            int mainInterestsCount = Integer.parseInt(snapshot2.child("mainInterestsCount").getValue().toString());
                            Log.d(TAG, "[interest 100]: interestsCount -> " + interestsCount + " mainInterestsCount -> " + mainInterestsCount);
                            dbRefUser.child("interestsCount").setValue(interestsCount);
                            dbRefUser.child("mainInterestsCount").setValue(mainInterestsCount);

                            // Choosing an interests
                            button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dbRefUser.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot3) {
                                            if (SystemClock.elapsedRealtime() - mLastClickTime < 300) {
                                                Toast.makeText(InterestsActivity.this, "You're clicking too fast!", Toast.LENGTH_SHORT).show();
                                            } else {
                                                int interestsCount = Integer.parseInt(snapshot3.child("interestsCount").getValue().toString());
                                                int mainInterestsCount = Integer.parseInt(snapshot3.child("mainInterestsCount").getValue().toString());
                                                int whatToDoWithInterest = 0;
                                                // --------------------------
                                                // whatToDoWithInterest list:
                                                // 0 - error
                                                // 1 - add as main interest
                                                // 2 - add as regular interest
                                                // 3 - change from regular interest to main interest
                                                // 4 - remove main interest
                                                // 5 - remove regular interest
                                                // 6 - toast about max interests
                                                // 7 - toast about min 1 main interest
                                                // --------------------------
                                                // [I] If user starts with no interests
                                                if (mainInterestsCount == 0 && interestsCount == 0) {
                                                    Log.d(TAG, "[if I]: ");
                                                    whatToDoWithInterest = 1;
                                                }
                                                // [II] If user have 1 main interests and 0 regular
                                                else if (mainInterestsCount == 1 && interestsCount == 0) {
                                                    Log.d(TAG, "[if II]: ");
                                                    // If user click gray interest - add it as main
                                                    if (!snapshot3.child("interests").hasChild("" + snap1.getKey()) && !snapshot3.child("mainInterests").hasChild("" + snap1.getKey()))
                                                        whatToDoWithInterest = 1;
                                                        // If user click main interest - inform
                                                    else if (snapshot3.child("mainInterests").hasChild("" + snap1.getKey()))
                                                        whatToDoWithInterest = 6;
                                                }
                                                // [III] If user have 1 main interest and 0-7 regular
                                                else if (mainInterestsCount == 1 && interestsCount < maxInterestsPerUser) {
                                                    Log.d(TAG, "[if III]: ");
                                                    // If user click gray interest - add it
                                                    if (!snapshot3.child("interests").hasChild("" + snap1.getKey()) && !snapshot3.child("mainInterests").hasChild("" + snap1.getKey()))
                                                        whatToDoWithInterest = 1;
                                                        // If user click regular interest - change it to main
                                                    else if (snapshot3.child("interests").hasChild("" + snap1.getKey()))
                                                        whatToDoWithInterest = 3;
                                                        // If user click main interest - inform
                                                    else if (snapshot3.child("mainInterests").hasChild("" + snap1.getKey()))
                                                        whatToDoWithInterest = 6;
                                                }
                                                // [IV] If user have 1 main interest and max regular
                                                else if (mainInterestsCount == 1 && interestsCount == maxInterestsPerUser) {
                                                    Log.d(TAG, "[if IV]: ");
                                                    // If user click gray interest - change it to main
                                                    if (!snapshot3.child("interests").hasChild("" + snap1.getKey()) && !snapshot3.child("mainInterests").hasChild("" + snap1.getKey()))
                                                        whatToDoWithInterest = 1;
                                                        // If user click regular interest - change it to main
                                                    else if (snapshot3.child("interests").hasChild("" + snap1.getKey()))
                                                        whatToDoWithInterest = 3;
                                                        // If user click main interest - inform
                                                    else if (snapshot3.child("mainInterests").hasChild("" + snap1.getKey()))
                                                        whatToDoWithInterest = 7;
                                                }
                                                // [V] If user have 2 main interests and 0-7 regular
                                                else if (mainInterestsCount == 2 && interestsCount < maxInterestsPerUser) {
                                                    Log.d(TAG, "[if V]: ");
                                                    // If user click gray interest - add it
                                                    if (!snapshot3.child("interests").hasChild("" + snap1.getKey()) && !snapshot3.child("mainInterests").hasChild("" + snap1.getKey()))
                                                        whatToDoWithInterest = 2;
                                                        // If user click main interest - remove it
                                                    else if (snapshot3.child("mainInterests").hasChild("" + snap1.getKey()))
                                                        whatToDoWithInterest = 4;
                                                        // If user click regular interest - remove it
                                                    else if (snapshot3.child("interests").hasChild("" + snap1.getKey()))
                                                        whatToDoWithInterest = 5;
                                                }
                                                // [VI] If user have max main and regular interests
                                                else {
                                                    Log.d(TAG, "[if VI]: ");
                                                    // If user click main interest - remove it
                                                    if (snapshot3.child("mainInterests").hasChild("" + snap1.getKey()))
                                                        whatToDoWithInterest = 4;
                                                        // If user click regular interest - remove it
                                                    else if (snapshot3.child("interests").hasChild("" + snap1.getKey()))
                                                        whatToDoWithInterest = 5;
                                                        // If user click gray interest - inform
                                                    else whatToDoWithInterest = 6;
                                                }

                                                switch (whatToDoWithInterest) {
                                                    case 1:
                                                        Log.d(TAG, "[case 1] ");
                                                        button.setBackgroundColor(Color.parseColor(mainColor));
                                                        addNewMainInterestToUser("" + snap1.getKey(), snap1.getValue().toString()); // Method to add main interest to user
                                                        mainInterestsCount++;
                                                        break;
                                                    case 2:
                                                        Log.d(TAG, "[case 2] ");
                                                        button.setBackgroundColor(Color.parseColor(regularColor));
                                                        addNewInterestsToUser("" + snap1.getKey(), snap1.getValue().toString()); // Method to add interest to user
                                                        interestsCount++;
                                                        break;
                                                    case 3:
                                                        Log.d(TAG, "[case 3] ");
                                                        button.setBackgroundColor(Color.parseColor(mainColor));
                                                        addNewMainInterestToUser("" + snap1.getKey(), snap1.getValue().toString()); // Method to add main interest to user
                                                        mainInterestsCount++;
                                                        removeInterestFromUser("" + snap1.getKey()); // Method to remove interest from user
                                                        interestsCount--;
                                                        break;
                                                    case 4:
                                                        Log.d(TAG, "[case 4] ");
                                                        button.setBackgroundColor(Color.parseColor(transparent_white_8));
                                                        removeMainInterestFromUser("" + snap1.getKey()); // Method to remove main interest from user
                                                        mainInterestsCount--;
                                                        break;
                                                    case 5:
                                                        Log.d(TAG, "[case 5] ");
                                                        button.setBackgroundColor(Color.parseColor(transparent_white_8));
                                                        removeInterestFromUser("" + snap1.getKey()); // Method to remove interest from user
                                                        interestsCount--;
                                                        break;
                                                    case 6:
                                                        Log.d(TAG, "[case 6] ");
                                                        Toast toastCase6 = Toast.makeText(getApplicationContext(), "You've reached maximum of " + maxInterestsPerUser + " interests!", Toast.LENGTH_SHORT);
                                                        toastCase6.show();
                                                        break;
                                                    case 7:
                                                        Log.d(TAG, "[case 7] ");
                                                        Toast toastCase7 = Toast.makeText(getApplicationContext(), "You must have at least 1 main interest!", Toast.LENGTH_SHORT);
                                                        toastCase7.show();
                                                        break;
                                                    case 0:
                                                        Log.d(TAG, "whatToDoWithInterests undefined error");
                                                }

                                                Log.d(TAG, "[100]: Click " + button.getTag());
                                                dbRefUser.child("interestsCount").setValue(interestsCount);
                                                dbRefUser.child("mainInterestsCount").setValue(mainInterestsCount);
                                            }

                                            mLastClickTime = SystemClock.elapsedRealtime();
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                            });

                            if (counter % 3 == 0) {
                                tempLinearLayout = new LinearLayout(InterestsActivity.this);
                                tempLinearLayout.setLayoutParams(parametersLL);
                                tempLinearLayout.setOrientation(LinearLayout.HORIZONTAL);

                                tempLinearLayout.addView(button);
                                counter++;
                            } else if (counter == 2) {
                                tempLinearLayout.addView(button);
                                verticalLayout.addView(tempLinearLayout);
                                counter = 0;
                                tempLinearLayout = new LinearLayout(InterestsActivity.this);
                            } else {
                                tempLinearLayout.addView(button);
                                counter++;
                            }

                            Log.d(TAG, "interest passed: " + counter + " - " + snap1.getValue());
                        }
                        if (counter % 3 != 0) verticalLayout.addView(tempLinearLayout);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: " + error.getMessage());
            }
        });
    }

    // Method to add interest to user
    public void addNewInterestsToUser(String id, String text){
        HashMap map = new HashMap();
        map.put(id, text);
        dbRefUser.child("interests").updateChildren(map);
    }

    // Method to add main interest to user
    public void addNewMainInterestToUser(String id, String text){
        HashMap map = new HashMap();
        map.put(id, text);
        dbRefUser.child("mainInterests").updateChildren(map);
    }

    // Method to remove interest from user
    public void removeInterestFromUser(String id){
        dbRefUser.child("interests").child(id).removeValue();
    }

    // Method to remove interest from user
    public void removeMainInterestFromUser(String id){
        dbRefUser.child("mainInterests").child(id).removeValue();
    }

    public void setMaxInterestsPerUser(int value){
        maxInterestsPerUser = value;
    }
}