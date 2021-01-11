package com.uwb.findafriendapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import com.uwb.findafriendapp.Notifications.Token;
import com.uwb.findafriendapp.classes.AlertReceiver;
import com.uwb.findafriendapp.classes.App;
import com.uwb.findafriendapp.classes.CircleTransform;
import com.uwb.findafriendapp.classes.Event;
import com.uwb.findafriendapp.fragments.FindEventsFragment;
import com.uwb.findafriendapp.fragments.FindFriendsFragment;
import com.uwb.findafriendapp.fragments.UserListFragment;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int alarmRequest = 1001;

    private ImageView bProfile;
    private Button btnCreateEvent;
    private TextView tvShowCurrentUser;
    private FirebaseUser currentFirebaseUser;
    private FirebaseAuth myFirebaseAuth;
    private TabAdapter adapter;
    private LinearLayout btnInvitations;
    private int[] tabIcons;
    private RelativeLayout layoutSplashScreen;
    private byte loadingCounter = 2;
    //increase when there is someting to load
    //1 - hello username
    //2 - avatar
    //3 - button below

    // TabLayout
    private TabLayout tlMain;
    private ViewPager vpMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layoutSplashScreen = findViewById(R.id.layoutSplashScreen);
        layoutSplashScreen.setVisibility(View.VISIBLE);

        // FIREBASE
        myFirebaseAuth = FirebaseAuth.getInstance();
        currentFirebaseUser = myFirebaseAuth.getCurrentUser();

        // BUTTON
        bProfile = findViewById(R.id.bProfile);
        btnCreateEvent = findViewById(R.id.btnCreateEvent);
        btnInvitations = findViewById(R.id.btnInvitations);

        tvShowCurrentUser = findViewById(R.id.tvShowUser);

        informUserAboutInterestSelection(true);

        // TabLayout
        tlMain = findViewById(R.id.tlMain);
        vpMain = findViewById(R.id.vpMain);

        setupViewPager(vpMain);

        FirebaseDatabase.getInstance().getReference("users-invitations/" + currentFirebaseUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            btnInvitations.setVisibility(View.VISIBLE);
                        } else {
                            btnInvitations.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        vpMain.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                highLightCurrentTab(position);

//                checkForInvitations();
            }
                
            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        btnInvitations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, InvitationsActivity.class);
                startActivity(intent);
            }
        });

//        vpMain.setCurrentItem(1);
//        getTabs()

//        int id = getIntent().getIntExtra("FRAGMENT_ID", 1);

        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference profileRef = storageRef.child("users/" + userID + "/profile.jpg");
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get()
                        .load(uri)
                        .fit()
                        .transform(new CircleTransform())
                        .into(bProfile);
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
                        .into(bProfile);
            }

        });

        DatabaseReference dbUserNickname = FirebaseDatabase.getInstance().getReference().child("users").child(userID).child("username");
        dbUserNickname.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                TextView tvHelloUser = findViewById(R.id.tvHelloUser);
                tvHelloUser.setText("Hello " + snapshot.getValue() + "!");

                loadingProgress();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: " + error.getMessage());
            }
        });
        
        updateToken(FirebaseInstanceId.getInstance().getToken());
    }


    @Override
    protected void onResume() {
        super.onResume();

        informUserAboutInterestSelection(false);

//        checkForInvitations();
        Log.d(TAG, "onResume: ");

        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference profileRef = storageRef.child("users/" + userID + "/profile.jpg");
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get()
                        .load(uri)
                        .fit()
                        .transform(new CircleTransform())
                        .into(bProfile);
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
                        .into(bProfile);
            }

        });
        loadingProgress();

        setAlarm();
        Log.d(TAG, "onResume: ");
    }

    private void setAlarm() {
        DatabaseReference eventsUsersRef = FirebaseDatabase.getInstance().getReference().child("events-users");
        eventsUsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot eventsUsersSnap) {
                DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference().child("events");
                eventsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot eventsSnapshot) {
                        final List<String> eventsKeys = new ArrayList<>();
                        Log.d(TAG, "onDataChange: " + eventsUsersSnap.getChildren());
                        // find all event's keys in which user participates in
                        HashMap hashMap;
                        for (DataSnapshot dataSnapshot : eventsUsersSnap.getChildren()) {
                            hashMap = (HashMap) dataSnapshot.getValue();
                            Log.d(TAG, "datasnapshot.getvalue: " + dataSnapshot.getValue());
                            Log.d(TAG, "datasnapshot.getkeu: " + dataSnapshot.getKey());
                            if (hashMap.containsKey(currentFirebaseUser.getUid())) {
                                Log.d(TAG, "added key is: " + dataSnapshot.getKey());
                                eventsKeys.add(dataSnapshot.getKey());
                            }
                        }
                        // CHECK IF ALARM IS ALREADY SET
                        Intent intentCheckAlarmSet = new Intent(MainActivity.this, AlertReceiver.class);
                        boolean isAlarmSet = (PendingIntent.getBroadcast(MainActivity.this, alarmRequest, intentCheckAlarmSet, 0) != null);
                        Log.d(TAG, "is Alarm set");

                        if (isAlarmSet) {
                            // if alarm is set up and there are no events for a user then cancel alarm
                            if (eventsKeys.isEmpty()) {
                                stopAlarm();
                                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                notificationManager.cancel(0);
                            } else {
                                // alarm is set up but user participates in at least one event (check which date is the closest)
                                calculateAndSetEarliestCalendar(eventsSnapshot, eventsKeys);
                            }
                        } else {
                            // if alarm is not set up but user takes part in an event then find the earliest one and set timer on it
                            if (!eventsKeys.isEmpty()) {
                                calculateAndSetEarliestCalendar(eventsSnapshot, eventsKeys);
                            }
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

    private void calculateAndSetEarliestCalendar(DataSnapshot eventsSnapshot, List<String> eventsKeys) {
        // ADD CALENDAR TO CALENDARS LIST
        List<Calendar> calendars = new ArrayList<>();
        for (DataSnapshot eventDataSnap : eventsSnapshot.getChildren()) {
            for (final String key : eventsKeys) {
                // get data of event that user takes part in
                if (eventDataSnap.getKey().equals(key)) {
                    Event event = eventDataSnap.getValue(Event.class);
                    Log.d(TAG, "DATE OF EVENT: " + event.getDate());
                    Log.d(TAG, "TIME OF EVENT: " + event.getTime());
                    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

                    // dateArr[0] - day, dateArr[1] - month, dateArr[2] - year
                    int[] dateArr = new int[3];
                    int i = 0;
                    for (String str : event.getDate().split("/")) {
                        dateArr[i] = Integer.parseInt(str);
                        i++;
                    }

                    // timeArr[0] - hour, timeArr[1] - minute
                    int[] timeArr = new int[2];
                    i = 0;
                    for (String str : event.getTime().split(":")) {
                        timeArr[i] = Integer.parseInt(str);
                        i++;
                    }

                    calendar.set(dateArr[2], dateArr[1] - 1, dateArr[0], timeArr[0] - 2, timeArr[1]);
                    Log.d(TAG, "onDataChange: " + dateArr[2] + " " +dateArr[1] + " " +dateArr[0] + " " +timeArr[0] + " " + timeArr[1]);
                    calendars.add(calendar);
//                                    startAlarm(calendar, key);
                    Log.d(TAG, "KEY: " + key);
                }
            }
        }

        long currentTimeInMillis = System.currentTimeMillis();


        List<Calendar> calendars2 = new ArrayList<>();
        // TAKE ALL DATES THAT ARE HIGHER THAN CURRENT DATE (IT MEANS THAT EVENT HAS NOT STARTED YET)
        for (Calendar calendar : calendars) {
            if (currentTimeInMillis < calendar.getTimeInMillis()) {
                calendars2.add(calendar);
            }
        }

        // FIND THE MIN OF CALENDARS WITH TIME HIGHER THAN CURRENT TIME IF SUCH EXIST
        if (!calendars2.isEmpty()) {
            Calendar earliestCalendar = calendars2.get(0);
            for (Calendar calendar : calendars2) {
                if (calendar.getTimeInMillis() < earliestCalendar.getTimeInMillis()) earliestCalendar = calendar;
            }
            startAlarm(earliestCalendar);
            Log.d(TAG, "calculateAndSetEarliestCalendar -> earliest is " + earliestCalendar.getTimeInMillis());
        }
        Log.d(TAG, "CURRENT TIME: " + System.currentTimeMillis());

    }

    private void startAlarm(Calendar calendar) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, alarmRequest, intent, 0);
        Log.d(TAG, "calendar get time in millis: " + calendar.getTimeInMillis());
        Log.d(TAG, "START ALARM");
        // start alarm one day before event
        long oneDay = 86_400_000L;
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() - oneDay, pendingIntent);
    }

    private void stopAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, alarmRequest, intent, 0);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
        Log.d(TAG, "STOP ALARM");
    }

    private void checkForInvitations() {
        DatabaseReference usersInvRef = FirebaseDatabase.getInstance().getReference().child("users-invitations/" + currentFirebaseUser.getUid());
        usersInvRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    btnInvitations.setVisibility(View.VISIBLE);
                } else {
                    btnInvitations.setVisibility(View.GONE);
                }
                loadingProgress();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /*
        method that informs user if he needs to select main interest and update it accordingly
        if used in onCreate then moves user to interests activity, otherwise only changes text
     */
    private void informUserAboutInterestSelection(final boolean isOnCreate) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users/"+currentFirebaseUser.getUid()+"/mainInterestsCount");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange: " + snapshot.getValue());
                // if user has 0 main interests go to interests activity to make user select one
                Integer mainInterestsCount = Integer.parseInt(snapshot.getValue().toString());
                if (mainInterestsCount.equals(0)) {
//                    tvShowCurrentUser.setText(currentFirebaseUser.getEmail() + " is your mail");
                    tvShowCurrentUser.setText("You have not set your interests yet!");
                    if (isOnCreate) {
                        Intent intent = new Intent(MainActivity.this, InterestsActivity.class);
                        startActivity(intent);
                    }
                } else {
                    tvShowCurrentUser.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void highLightCurrentTab(int position){
        for (int i = 0; i < tlMain.getTabCount(); i++){
            TabLayout.Tab tab = tlMain.getTabAt(i);
            assert tab != null;
            tab.setCustomView(null);
            tab.setCustomView(adapter.getTabView(i));
        }

        TabLayout.Tab tab = tlMain.getTabAt(position);
        assert tab != null;
        tab.setCustomView(null);
        tab.setCustomView(adapter.getSelectedTabView(position));
    }

    private void setupViewPager(ViewPager viewPager){
        adapter = new TabAdapter(getSupportFragmentManager(), this);

        tabIcons = new int[]{
                R.drawable.find_friends_icon,
                R.drawable.event_icon,
                R.drawable.star_icon
        };
        adapter.addFragment(new FindFriendsFragment(), "Find Friends", tabIcons[0]);
        adapter.addFragment(new FindEventsFragment(), "Events", tabIcons[1]);
        adapter.addFragment(new UserListFragment(), "Favourites", tabIcons[2]);

        viewPager.setAdapter(adapter);
        tlMain.setupWithViewPager(vpMain);
        highLightCurrentTab(0);

    }

    private void updateToken(String token) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("tokens");
        Token token1 = new Token(token);
        reference.child(currentFirebaseUser.getUid()).setValue(token1);
    }

    class TabAdapter extends FragmentPagerAdapter{
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();
        private final List<Integer> mFragmentIconList = new ArrayList<>();
        private Context context;

        public TabAdapter(FragmentManager manager, Context context){
            super(manager);
            this.context = context;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title, int tabIcon){
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
            mFragmentIconList.add(tabIcon);
        }

        @Override
        public CharSequence getPageTitle(int position){
            return mFragmentTitleList.get(position);
        }

        @SuppressLint("ResourceType")
        public View getSelectedTabView(int position){
            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.custom_tab, null);
            TextView tabTextView = view.findViewById(R.id.tabTextView);
            tabTextView.setText(mFragmentTitleList.get(position));
            tabTextView.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.blue));

            ImageView tabImageView = view.findViewById(R.id.tabImageView);
            tabImageView.setImageResource(mFragmentIconList.get(position));
            tabImageView.getDrawable().setColorFilter(getResources().getColor(R.color.blue), PorterDuff.Mode.SRC_ATOP);
            return view;
        }

        public View getTabView(int position){
            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.custom_tab, null);
            TextView tabTextView = view.findViewById(R.id.tabTextView);
            tabTextView.setText(mFragmentTitleList.get(position));

            ImageView tabImageView = view.findViewById(R.id.tabImageView);
            tabImageView.setImageResource(mFragmentIconList.get(position));
            return view;
        }
    }

    public void goToProfileActivity(View view) {
        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(intent);
    }

    public void goToCreateEventActivity(View view) {
        Intent intent = new Intent(MainActivity.this, CreateEventActivity.class);
        startActivity(intent);
    }

    public void loadingProgress(){
        loadingCounter--;
        if(loadingCounter == 0){
            layoutSplashScreen.setVisibility(View.GONE);
        }
    }
}