package com.uwb.findafriendapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

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
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.uwb.findafriendapp.classes.Event;
import com.uwb.findafriendapp.classes.User;
import com.uwb.findafriendapp.dialogs.SelectIconDialog;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CreateEventActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener, SelectIconDialog.OnInputListener{

    private static final String TAG = "CreateEventActivity";
    // default icon
    private String icon = "question.png";

    private boolean wasIconChanged = false;
    @Override
    public void sendInput(String iconName) {
        Log.d(TAG, "sendInput: got the input: " + iconName);

        icon = iconName;

        // get icon's ID and put it into ImageView
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("icons/" + icon);
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get()
                        .load(uri)
                        .centerInside()
                        .fit()
                        .into(ivEvent);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: ");
            }
        });


        wasIconChanged = true;
    }

    // UI
    private EditText etEventDescription, etEventDate, etEventTime, etEventLocalization;
    private Button btnCreateEvent, btnEditEvent;
    public ImageView ivEvent;

    // Firebase
    private FirebaseAuth myFirebaseAuth;
    private FirebaseDatabase database;
    private FirebaseUser firebaseUser;
    private DatabaseReference dbRef, eventsUsersRef, usersRef, editEventRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        // UI
        etEventDescription = findViewById(R.id.etEventDescription);
        etEventDate = findViewById(R.id.etEventDate);
        etEventTime = findViewById(R.id.etEventTime);
        etEventLocalization = findViewById(R.id.etEventLocalization);
        btnCreateEvent = findViewById(R.id.btnCreateEvent);
        btnEditEvent = findViewById(R.id.btnEditEvent);
        ivEvent = findViewById(R.id.ivEvent);

        // Firebase
        myFirebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        firebaseUser = myFirebaseAuth.getCurrentUser();

        // Edit Text for Date
        etEventDate.setFocusable(false);
        etEventDate.setClickable(true);
        etEventDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });

        // Edit Text for Time
        etEventTime.setFocusable(false);
        etEventTime.setClickable(true);
        etEventTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePickerDialog();
            }
        });

        boolean isEventEdition = getIntent().getBooleanExtra("isEventEdition", false);
        final String eventID = getIntent().getStringExtra("eventID");
        editEventRef = FirebaseDatabase.getInstance().getReference().child("events/" + eventID);
        // edit event if user pressed edit button
        if (isEventEdition) {
            btnCreateEvent.setVisibility(View.GONE);
            btnEditEvent.setVisibility(View.VISIBLE);

            // set data from database
            editEventRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull final DataSnapshot snapshot) {
                    final Event editEvent = snapshot.getValue(Event.class);

                    setEventIcon(editEvent.getIconRef());
                    etEventDescription.setText(editEvent.getEventDescription());
                    etEventDate.setText(editEvent.getDate());
                    etEventTime.setText(editEvent.getTime());
                    etEventLocalization.setText(editEvent.getLocalization());

                    // set edit event button listener
                    btnEditEvent.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // replace new data if user didnt leave any field empty
                            if (etEventDescription.getText().toString().isEmpty() || etEventDate.getText().toString().isEmpty() ||
                                    etEventTime.getText().toString().isEmpty() || etEventLocalization.getText().toString().isEmpty()) {
                                Toast.makeText(CreateEventActivity.this, "Fill all fields", Toast.LENGTH_SHORT).show();
                            } else {
                                Event editedEvent = new Event(firebaseUser.getUid(), etEventDescription.getText().toString(), etEventDate.getText().toString(),
                                        etEventTime.getText().toString(), etEventLocalization.getText().toString(), editEvent.getIconRef());

                                // set event's icon if new selected
                                if (wasIconChanged) {
                                    editedEvent.setIconRef("icons/" + icon);
                                }


                                editEventRef.setValue(editedEvent);
                                Intent intent = new Intent(CreateEventActivity.this, ExpandedEventActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("visitEventID", eventID);
                                startActivity(intent);
                            }
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        } else {
            btnCreateEvent.setVisibility(View.VISIBLE);
            btnEditEvent.setVisibility(View.GONE);
        }



        // Create event Button
        btnCreateEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String description = etEventDescription.getText().toString();
                final String localization = etEventLocalization.getText().toString();
                final String date = etEventDate.getText().toString();
                final String time = etEventTime.getText().toString();

                usersRef = FirebaseDatabase.getInstance().getReference().child("users/" + myFirebaseAuth.getCurrentUser().getUid());
                usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child("events-users");

                        // check if fields are filled
                        if (description.isEmpty() || localization.isEmpty() || date.isEmpty() || time.isEmpty()) {
                            Toast.makeText(CreateEventActivity.this, "Fill all fields", Toast.LENGTH_SHORT).show();
                        } // check if user has an event already
                        else {
                            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    boolean userOwnsEvent = false;
                                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                        Map<String, String> map = (HashMap<String, String>) dataSnapshot.getValue();
                                        // Iterate and check for key and value if user created
                                        if (map.containsKey(firebaseUser.getUid())) {
                                            if (map.get(firebaseUser.getUid()).equals("owner")) {
                                                userOwnsEvent = true;
                                                break;
                                            }
                                        }
                                    }
                                    // create event if user has not events at all
                                    if (!userOwnsEvent) {
                                        dbRef = database.getReference("events").push();
                                        String iconRef = "icons/" + icon;
                                        Event newEvent = new Event(firebaseUser.getUid(), description, date, time, localization, iconRef);
                                        dbRef.setValue(newEvent);

                                        HashMap hashMap = new HashMap();
                                        hashMap.put(firebaseUser.getUid(), "owner");
                                        eventsUsersRef = FirebaseDatabase.getInstance().getReference("events-users/" + dbRef.getKey());
                                        eventsUsersRef.updateChildren(hashMap);

                                        finish();
                                    } else {
                                        Toast.makeText(CreateEventActivity.this, "You cannot have more than 1 event", Toast.LENGTH_SHORT).show();
                                    }
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
        });

        // Image View event
        ivEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectIconDialog selectIconDialog = new SelectIconDialog();
                selectIconDialog.show(getSupportFragmentManager(), "SelectIconDialog");
            }
        });



    }

    private void showTimePickerDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                this,
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                Calendar.getInstance().get(Calendar.MINUTE),
                true
        );

        timePickerDialog.show();
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        // disable past date
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    /* TODO
        Make user not insert past time*/
    @Override
    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
        String time = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
        etEventTime.setText(time);
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
        month += 1;
        String date = String.format(Locale.getDefault(), "%02d/%02d/%4d", dayOfMonth, month, year);
        etEventDate.setText(date);
    }

    private void setEventIcon(String iconRef) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(iconRef);
        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get()
                        .load(uri)
                        .centerInside()
                        .fit()
                        .into(ivEvent);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Picasso.get()
                        .load(R.drawable.question)
                        .centerInside()
                        .fit()
                        .into(ivEvent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
    }
}