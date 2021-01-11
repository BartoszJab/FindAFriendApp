package com.uwb.findafriendapp.adapters;

import android.content.Context;
import android.content.Intent;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.uwb.findafriendapp.ExpandedProfileActivity;
import com.uwb.findafriendapp.ProfileActivity;
import com.uwb.findafriendapp.R;
import com.uwb.findafriendapp.classes.CircleTransform;
import com.uwb.findafriendapp.classes.User;

import java.util.List;

public class ParticipantsAdapter extends RecyclerView.Adapter<ParticipantsAdapter.UsersViewHolder> {
    private static final String TAG = "ParticipantsAdapter";


    private LayoutInflater inflater;
    private Context myContext;
    private List<User> usersList;
    private List<String> usersKey;
    private FirebaseUser firebaseUser;

    public ParticipantsAdapter(Context context, List<User> usersList, List<String> usersKey) {
        myContext = context;
        inflater = LayoutInflater.from(context);
        this.usersList = usersList;
        this.usersKey = usersKey;
    }

    @NonNull
    @Override
    public ParticipantsAdapter.UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemUser = inflater.inflate(R.layout.row_participant, parent, false);

        return new UsersViewHolder(itemUser);
    }

    @Override
    public void onBindViewHolder(@NonNull ParticipantsAdapter.UsersViewHolder holder, final int position) {
        User user = usersList.get(position);
        final String userKey = usersKey.get(position);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        holder.setUsername(user.getUsername());
        holder.setIcon(userKey);


        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // currently logged user was pressed in participants list
                if (usersKey.get(position).equals(firebaseUser.getUid())) {
                    Intent intent = new Intent(view.getContext(), ProfileActivity.class);
                    view.getContext().startActivity(intent);
                } else {
                    Intent intent = new Intent(view.getContext(), ExpandedProfileActivity.class);
                    intent.putExtra("userID", usersKey.get(position));
                    Log.d(TAG, "onClick: " + usersKey.get(position));
                    view.getContext().startActivity(intent);
                }

            }
        });
        holder.position = position;
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder {
        View view;
        TextView tvUsername;
        ImageView ivIcon;

        int position;

        public UsersViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
        }

        public void setUsername(String username) {
            tvUsername = view.findViewById(R.id.tvParticipantUsername);
            tvUsername.setText(username);
        }

        public void setIcon(String userID) {
            ivIcon = view.findViewById(R.id.ivParticipant);
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("users/" + userID + "/profile.jpg");

            storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get()
                            .load(uri)
                            .error(R.drawable.default_user_avatar)
                            .transform(new CircleTransform())
                            .centerCrop()
                            .fit()
                            .into(ivIcon);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Picasso.get()
                            .load(R.drawable.default_user_avatar)
                            .transform(new CircleTransform())
                            .centerCrop()
                            .fit()
                            .into(ivIcon);
                }
            });
        }
    }
}
