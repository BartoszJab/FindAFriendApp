package com.uwb.findafriendapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.uwb.findafriendapp.Notifications.Client;
import com.uwb.findafriendapp.Notifications.Data;
import com.uwb.findafriendapp.Notifications.MyResponse;
import com.uwb.findafriendapp.Notifications.Sender;
import com.uwb.findafriendapp.Notifications.Token;
import com.uwb.findafriendapp.adapters.MessageAdapter;
import com.uwb.findafriendapp.classes.Chat;
import com.uwb.findafriendapp.classes.User;
import com.uwb.findafriendapp.fragments.APIService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {
    private final static String TAG = "ChatActivity";

    private EditText etWriteMessage;
    private ImageButton btnSendMessage;
    private List<Chat> chatList;
    private MessageAdapter messageAdapter;
    private String messageReceiver;
    LinearLayoutManager layoutManager;

    private RecyclerView recyclerView;
//    private RecyclerView.LayoutManager layoutManager;

    private FirebaseUser firebaseUser;

    APIService apiService;
    boolean notify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageReceiver = getIntent().getStringExtra("userID");
        Log.d(TAG, "onCreate: " + messageReceiver);
        etWriteMessage = findViewById(R.id.etWriteMessage);
        btnSendMessage = findViewById(R.id.btnSendMessage);

        recyclerView = findViewById(R.id.recyclerViewMessages);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, final int bottom, int i4, int i5, int i6, int oldBottom) {
                // keyboard is in up state
                if (bottom < oldBottom) {
                    Log.d(TAG, "Keyboard opened! ");

                    recyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (recyclerView.getAdapter().getItemCount() > 0) {
                                Log.d(TAG, "recyclerView.getAdapter().getItemCount() - 1 " + (recyclerView.getAdapter().getItemCount() - 1));
                                recyclerView.smoothScrollToPosition(
                                        recyclerView.getAdapter().getItemCount() - 1);
                            }
                        }
                    }, 100);
                }
            }
        });

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        readMessage(firebaseUser.getUid(), getIntent().getStringExtra("userID"));

        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify = true;
                String message = etWriteMessage.getText().toString();
                if (!message.isEmpty()) {
                    sendMessage(firebaseUser.getUid(), messageReceiver, message);
                    etWriteMessage.setText("");
                }
            }
        });
    }

    private void sendMessage(final String senderID, final String receiverID, final String message) {
        updateToken(FirebaseInstanceId.getInstance().getToken());
        DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference().child("chats");

        chatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange: " + snapshot);
                Log.d(TAG, "onDataChange: " + snapshot.getChildren());
                Log.d(TAG, "onDataChange: " + snapshot.hasChild(senderID + "_" + receiverID));

                DatabaseReference databaseReference;

                if (snapshot.hasChild(senderID + "_" + receiverID)) {
                    databaseReference = FirebaseDatabase.getInstance().getReference().child("chats/" + senderID + "_" + receiverID);
                } else {
                    databaseReference = FirebaseDatabase.getInstance().getReference().child("chats/" + receiverID + "_" + senderID);
                }

                final HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("senderID", senderID);
                hashMap.put("receiverID", receiverID);
                hashMap.put("message", message);

                databaseReference.push().setValue(hashMap);

                final String msg = message;
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.getUid());
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (notify) {
                            sendNotification(messageReceiver, user.getUsername(), msg);
                        }
                        notify = false;
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

    private void updateToken(String token) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("tokens");
        Token token1 = new Token(token);
        reference.child(firebaseUser.getUid()).setValue(token1);
    }

    private void sendNotification(String receiver, final String username, final String message) {
        DatabaseReference tokensRef = FirebaseDatabase.getInstance().getReference("tokens");
        Query query = tokensRef.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Token token = dataSnapshot.getValue(Token.class);
                    Data data = new Data(firebaseUser.getUid(), R.drawable.chat_bubble, username + ": " + message, "New message",
                            messageReceiver, "ChatActivity");

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success == 1) {
                                            Log.d(TAG, "response.code(): " + response.code() + " , response.body().success: " + response.body().success);
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readMessage(final String senderID, final String receiverID) {
        chatList = new ArrayList<>();

        DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference().child("chats");
        chatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DatabaseReference databaseReference;
                if (snapshot.hasChild(senderID + "_" + receiverID)) {
                    databaseReference = FirebaseDatabase.getInstance().getReference().child("chats/" + senderID + "_" + receiverID);
                } else {
                    databaseReference = FirebaseDatabase.getInstance().getReference().child("chats/" + receiverID + "_" + senderID);
                }
                messageAdapter = new MessageAdapter(ChatActivity.this, chatList);
                recyclerView.setAdapter(messageAdapter);
                databaseReference.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        Chat chat = snapshot.getValue(Chat.class);
                        if (chat.getReceiverID().equals(receiverID) && chat.getSenderID().equals(senderID) ||
                                chat.getReceiverID().equals(senderID) && chat.getSenderID().equals(receiverID)) {
                            chatList.add(chat);

                            messageAdapter.notifyDataSetChanged();

                            recyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                        Chat chat = snapshot.getValue(Chat.class);
                        if (chat.getReceiverID().equals(messageReceiver) && chat.getSenderID().equals(firebaseUser.getUid()) ||
                                chat.getReceiverID().equals(firebaseUser.getUid()) && chat.getSenderID().equals(messageReceiver)) {
                            chatList.remove(chat);
                            messageAdapter.notifyDataSetChanged();
                        }
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