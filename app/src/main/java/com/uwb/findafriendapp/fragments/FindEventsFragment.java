package com.uwb.findafriendapp.fragments;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.service.autofill.Dataset;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.uwb.findafriendapp.ExpandedEventActivity;
import com.uwb.findafriendapp.R;
import com.uwb.findafriendapp.classes.Event;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.uwb.findafriendapp.dialogs.ConfirmPasswordDialog;
import java.util.ArrayList;
import java.util.List;
public class FindEventsFragment extends Fragment {
    private static final String TAG = "FindEventsFragment";
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView recyclerEventsList;
    private static String PACKAGE_NAME;
    private TextView tvNoEvents;
    // Firebase
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    private FirebaseUser firebaseUser;
    private DatabaseReference usersRef, eventsUsersRef;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.row_find_events, container, false);
        // Set package name to receive iconID later on in EventViewHolder
        PACKAGE_NAME = getActivity().getPackageName();
        tvNoEvents = view.findViewById(R.id.tvNoEvents);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        usersRef = FirebaseDatabase.getInstance().getReference().child("users/"+firebaseUser.getUid()+"/mainInterestsCount");
        final String userID = firebaseUser.getUid();
        recyclerEventsList = view.findViewById(R.id.eventsRecyclerView);
        recyclerEventsList.getRecycledViewPool().setMaxRecycledViews(0, 0);
        recyclerEventsList.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(view.getContext());
        recyclerEventsList.setLayoutManager(layoutManager);


        final Query query = FirebaseDatabase.getInstance().getReference().child("events");
        firebaseAdapterHandler(query);
        recyclerEventsList.setAdapter(firebaseRecyclerAdapter);
        // hide events recycler view if user has not selected main interest
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer mainInterestsCount = Integer.parseInt(snapshot.getValue().toString());
                if (!mainInterestsCount.equals(0)) {
                    recyclerEventsList.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getContext(), "Select interests", Toast.LENGTH_SHORT).show();
                    recyclerEventsList.setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        // check if any event exists
        FirebaseDatabase.getInstance().getReference("events")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) tvNoEvents.setVisibility(View.VISIBLE);
                else tvNoEvents.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }
    private void firebaseAdapterHandler(final Query query) {
        final FirebaseRecyclerOptions<Event> eventsOptions = new FirebaseRecyclerOptions.Builder<Event>().setQuery(query, Event.class).build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Event, EventViewHolder>(eventsOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final EventViewHolder eventViewHolder, final int position, @NonNull final Event event) {
                // for now to make app not crash
                if (event.getIconRef() != null) {
                    eventViewHolder.setEventIcon(event.getIconRef());
                }
                // eventViewHolder.setEventCreatorImage(event.getOwner());
                eventViewHolder.setEventLocation(event.getLocalization());
                eventViewHolder.setEventTime(event.getTime());
                eventViewHolder.setEventDate(event.getDate());
                eventViewHolder.setEventDescription(event.getEventDescription());
                eventsUsersRef = FirebaseDatabase.getInstance().getReference().child("events-users/" + getRef(position).getKey() + "/" + firebaseUser.getUid());
                Log.d(TAG, "POSITION: " + position);
                Log.d(TAG, "VIEW HOLDER POSITION: " + eventViewHolder.getBindingAdapterPosition());
                // hide elements on given statement
                eventsUsersRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        Log.d(TAG, "current path: " + "events-users/" + getRef(position).getKey() + "/" + firebaseUser.getUid());
//                        Log.d(TAG, "snapshot KEY: " + snapshot.getKey());
//                        Log.d(TAG, "snapshot VALUE: " + snapshot.getValue());
                        // user participates in an event so hide it from him
                        if (snapshot.exists()) {
                            if (snapshot.getValue() != null) {
                                ViewGroup.LayoutParams params = eventViewHolder.myView.getLayoutParams();
                                RecyclerView.LayoutParams paramsRecycler = (RecyclerView.LayoutParams) eventViewHolder.myView.getLayoutParams();
                                paramsRecycler.height = 0;
                                paramsRecycler.width = 0;
                                paramsRecycler.setMargins(0, 0, 0, 0);
                                eventViewHolder.myView.setLayoutParams(paramsRecycler);
                                eventViewHolder.myView.setVisibility(View.GONE);
                            } else {
                                ViewGroup.LayoutParams params = eventViewHolder.myView.getLayoutParams();
                                params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                                params.width = LinearLayout.LayoutParams.MATCH_PARENT;
                                eventViewHolder.myView.setLayoutParams(params);
                                eventViewHolder.myView.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
                eventViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(TAG, "getRef position get key: " + getRef(position).getKey());
                        String visitEventID = getRef(position).getKey();
                        Intent eventIntent = new Intent(view.getContext(), ExpandedEventActivity.class);
                        // put event's data
                        eventIntent.putExtra("visitEventID", visitEventID);
                        eventIntent.putExtra("ivEventCreatorID", event.getOwner());
                        startActivity(eventIntent);
                    }
                });
            }
            @NonNull
            @Override
            public EventViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.cardview_events, parent, false);
                return new EventViewHolder(view);
            }
        };
    }
    public static class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvEventDate, tvEventTime, tvEventLocation, tvEventDescription;
        private StorageReference storageRef;
        View myView;
        public EventViewHolder(View itemView) {
            super(itemView);
            myView = itemView;
            storageRef = FirebaseStorage.getInstance().getReference();
        }
        public void setEventIcon(String iconRef) {
            ivIcon = myView.findViewById(R.id.ivEventIcon);

            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(iconRef);
            storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get()
                            .load(uri)
                            .centerCrop()
                            .fit()
                            .into(ivIcon);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Picasso.get()
                            .load(R.drawable.question)
                            .centerCrop()
                            .fit()
                            .into(ivIcon);
                }
            });
        }
        public void setEventDescription(String eventDescription) {
            tvEventDescription = myView.findViewById(R.id.tvEventDescription);
            tvEventDescription.setText(eventDescription);
        }
        public void setEventDate(String eventDate) {
            tvEventDate = myView.findViewById(R.id.tvEventDate);
            tvEventDate.setText(eventDate);
        }
        public void setEventTime(String eventTime) {
            tvEventTime = myView.findViewById(R.id.tvEventTime);
            tvEventTime.setText(eventTime);
        }
        public void setEventLocation(String eventLocation) {
            tvEventLocation = myView.findViewById(R.id.tvEventLocation);
            tvEventLocation.setText(eventLocation);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firebaseRecyclerAdapter.startListening();
        Log.d(TAG, "onViewCreated: Inside");
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        firebaseRecyclerAdapter.stopListening();
    }
    @Override
    public void onStart() {
        super.onStart();
        // firebaseRecyclerAdapter.startListening();
    }
    @Override
    public void onStop() {
        super.onStop();
        //firebaseRecyclerAdapter.stopListening();
    }
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Inside");
        // check if user has selected main interest or not
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer mainInterestsCount = Integer.parseInt(snapshot.getValue().toString());
                if (!mainInterestsCount.equals(0)) {
                    recyclerEventsList.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        firebaseRecyclerAdapter.notifyDataSetChanged();
    }
    public static FindEventsFragment getInstance() {
        FindEventsFragment findEventsFragment = new FindEventsFragment();
        return findEventsFragment;
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }
}