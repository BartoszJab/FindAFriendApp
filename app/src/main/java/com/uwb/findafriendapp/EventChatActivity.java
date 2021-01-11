package com.uwb.findafriendapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.uwb.findafriendapp.Notifications.Token;
import com.uwb.findafriendapp.adapters.MessageAdapter;
import com.uwb.findafriendapp.classes.Chat;
import com.uwb.findafriendapp.classes.Event;
import com.uwb.findafriendapp.classes.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EventChatActivity extends AppCompatActivity {
    private static final String TAG = "EventChatActivity";
    private String eventID;
    private List<Chat> eventChatList;

    private TextView tvEventChatLocalization, tvEventChatDate;
    private ImageButton btnSendMessage;
    private EditText etWriteMessage;

    private MessageAdapter messageAdapter;
    private FirebaseUser firebaseUser;
    private RecyclerView recyclerView;
    LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_chat);

        eventID = getIntent().getStringExtra("eventID");

        tvEventChatLocalization = findViewById(R.id.tvEventChatLocalization);
        tvEventChatDate = findViewById(R.id.tvEventChatDate);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        etWriteMessage = findViewById(R.id.etWriteMessage);
        recyclerView = findViewById(R.id.recyclerViewEventMessages);

        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // READ EVENT'S DATA AND LOCALIZATION THAT ARE BEING SHOWED AT THE TOP
        FirebaseDatabase.getInstance().getReference("events/" + eventID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Event event = snapshot.getValue(Event.class);
                tvEventChatLocalization.setText(event.getLocalization());
                tvEventChatDate.setText(event.getDate());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // SCROLL CHAT WHEN KEYBOARD OPENED TO SCROLL RECYCLER VIEW TO LAST MESSAGE
        recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, final int bottom, int i4, int i5, int i6, int oldBottom) {
                // keyboard is in up state
                if (bottom < oldBottom) {
                    recyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (recyclerView.getAdapter().getItemCount() > 0) {
                                recyclerView.smoothScrollToPosition(
                                        recyclerView.getAdapter().getItemCount() - 1);
                            }
                        }
                    }, 100);
                }
            }
        });

        readMessage();

        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                notify = true;
                String message = etWriteMessage.getText().toString();
                if (!message.isEmpty()) {
                    sendMessage(firebaseUser.getUid(), message);
                    etWriteMessage.setText("");
                }
            }
        });
    }

    private void sendMessage(final String senderID, final String message) {
        updateToken(FirebaseInstanceId.getInstance().getToken());
        DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference().child("events-chats/" + eventID);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("senderID", senderID);
        hashMap.put("message", message);

        chatsRef.push().setValue(hashMap);

//        chatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                Log.d(TAG, "onDataChange: " + snapshot);
//                Log.d(TAG, "onDataChange: " + snapshot.getChildren());
//                Log.d(TAG, "onDataChange: " + snapshot.hasChild(senderID + "_" + receiverID));
//
//                DatabaseReference databaseReference;
//
//                if (snapshot.hasChild(senderID + "_" + receiverID)) {
//                    databaseReference = FirebaseDatabase.getInstance().getReference().child("chats/" + senderID + "_" + receiverID);
//                } else {
//                    databaseReference = FirebaseDatabase.getInstance().getReference().child("chats/" + receiverID + "_" + senderID);
//                }
//
//                final HashMap<String, Object> hashMap = new HashMap<>();
//                hashMap.put("senderID", senderID);
//                hashMap.put("receiverID", receiverID);
//                hashMap.put("message", message);
//
//                databaseReference.push().setValue(hashMap);
//
//                final String msg = message;
//                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.getUid());
//                reference.addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        User user = snapshot.getValue(User.class);
//                        if (notify) {
//                            sendNotification(messageReceiver, user.getUsername(), msg);
//                        }
//                        notify = false;
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
    }

    private void updateToken(String token) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("tokens");
        Token token1 = new Token(token);
        reference.child(firebaseUser.getUid()).setValue(token1);
    }

    private void readMessage() {
//        chatList = new ArrayList<>();
        eventChatList = new ArrayList<>();

        DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference().child("chats");
        chatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DatabaseReference databaseReference;
                DatabaseReference eventsChatsRef = FirebaseDatabase.getInstance().getReference("events-chats/" + eventID);

//                if (snapshot.hasChild(senderID + "_" + receiverID)) {
//                    databaseReference = FirebaseDatabase.getInstance().getReference().child("chats/" + senderID + "_" + receiverID);
//                } else {
//                    databaseReference = FirebaseDatabase.getInstance().getReference().child("chats/" + receiverID + "_" + senderID);
//                }
//                messageAdapter = new MessageAdapter(ChatActivity.this, chatList);
                messageAdapter = new MessageAdapter(EventChatActivity.this, eventChatList);
                recyclerView.setAdapter(messageAdapter);
                eventsChatsRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        Chat chat = snapshot.getValue(Chat.class);
//                        if (chat.getReceiverID().equals(receiverID) && chat.getSenderID().equals(senderID) ||
//                                chat.getReceiverID().equals(senderID) && chat.getSenderID().equals(receiverID)) {
//                            chatList.add(chat);
//
//                            messageAdapter.notifyDataSetChanged();
//
//                            recyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
//                        }

                        eventChatList.add(chat);
                        messageAdapter.notifyDataSetChanged();
                        recyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                        Chat chat = snapshot.getValue(Chat.class);
//                        if (chat.getReceiverID().equals(messageReceiver) && chat.getSenderID().equals(firebaseUser.getUid()) ||
//                                chat.getReceiverID().equals(firebaseUser.getUid()) && chat.getSenderID().equals(messageReceiver)) {
//                            chatList.remove(chat);
//                            messageAdapter.notifyDataSetChanged();
//                        }
                        eventChatList.remove(chat);
                        messageAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

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