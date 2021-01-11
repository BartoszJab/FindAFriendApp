package com.uwb.findafriendapp.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.transition.Hold;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.uwb.findafriendapp.R;
import com.uwb.findafriendapp.classes.Chat;
import com.uwb.findafriendapp.classes.CircleTransform;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessagesViewHolder> {
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context mContext;
    private List<Chat> chatList;

    public MessageAdapter(Context mContext, List<Chat> chatList) {
        this.mContext = mContext;
        this.chatList = chatList;
    }


    @NonNull
    @Override
    public MessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
            return new MessagesViewHolder(view);
        }  else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
            return new MessagesViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull MessagesViewHolder holder, int position) {
        Chat chat = chatList.get(position);
        holder.setMessageText(chat.getMessage());
        holder.setIvMessageSender(chat.getSenderID());

        holder.position = position;
    }

    @Override
    public int getItemViewType(int position) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (chatList.get(position).getSenderID().equals(firebaseUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public static class MessagesViewHolder extends RecyclerView.ViewHolder {

        View view;
        ImageView ivMessageSender;
        TextView tvMessageText;

        int position;

        public MessagesViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            tvMessageText = view.findViewById(R.id.tvMessageText);
            ivMessageSender = view.findViewById(R.id.ivProfileImage);
        }

        public void setMessageText(String text) {
            tvMessageText.setText(text);
        }

        public void setIvMessageSender(String userID) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("users/" + userID + "/profile.jpg");

            storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get()
                            .load(uri)
                            .error(R.drawable.default_user_avatar)
                            .centerInside()
                            .fit()
                            .transform(new CircleTransform())
                            .into(ivMessageSender);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Picasso.get()
                            .load(R.drawable.default_user_avatar)
                            .centerInside()
                            .fit()
                            .transform(new CircleTransform())
                            .into(ivMessageSender);
                }
            });
        }
    }
}
