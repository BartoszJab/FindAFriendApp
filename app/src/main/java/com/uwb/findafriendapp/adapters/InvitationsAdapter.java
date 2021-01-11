package com.uwb.findafriendapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.uwb.findafriendapp.ExpandedProfileActivity;
import com.uwb.findafriendapp.MainActivity;
import com.uwb.findafriendapp.R;
import com.uwb.findafriendapp.classes.CircleTransform;
import com.uwb.findafriendapp.classes.User;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.List;

public class InvitationsAdapter extends RecyclerView.Adapter<InvitationsAdapter.ViewHolder> {

    private Context mContext;
    private List<User> invitatorsList;
    private List<String> invitatorsKeys;
    private FirebaseUser firebaseUser;

    public InvitationsAdapter(Context mContext, List<User> invitatorsList, List<String> invitatorsKeys) {
        this.mContext = mContext;
        this.invitatorsList = invitatorsList;
        this.invitatorsKeys = invitatorsKeys;
    }

    @NonNull
    @Override
    public InvitationsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.invitation_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final InvitationsAdapter.ViewHolder holder, final int position) {
        final User user = invitatorsList.get(position);
        final String invitatorKey = invitatorsKeys.get(position);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        holder.setInvitatorUsername(user.getUsername());
        holder.setInvitatorIcon(invitatorKey);

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ExpandedProfileActivity.class);
                intent.putExtra("userID", invitatorKey);
                intent.putExtra("isInvitator", true);
                view.getContext().startActivity(intent);
            }
        });

        holder.btnInvitationAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseDatabase.getInstance().getReference().child("users-invitations/" + firebaseUser.getUid() + "/" + invitatorKey).removeValue();

                HashMap userInvitatorMap = new HashMap();
                userInvitatorMap.put(invitatorKey, true);
                FirebaseDatabase.getInstance().getReference().child("users-favourites/" + firebaseUser.getUid()).updateChildren(userInvitatorMap);

                HashMap invitatorUserMap = new HashMap();
                invitatorUserMap.put(firebaseUser.getUid(), true);
                FirebaseDatabase.getInstance().getReference().child("users-favourites/" + invitatorKey).updateChildren(invitatorUserMap);

                handleInvitationAndRefresh(position);
            }
        });

        holder.btnInvitationRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseDatabase.getInstance().getReference().child("users-invitations/" + firebaseUser.getUid() + "/" + invitatorKey).removeValue();
                handleInvitationAndRefresh(position);
            }
        });

        holder.position = position;
    }

    private void handleInvitationAndRefresh(int position) {
        invitatorsList.remove(position);
        invitatorsKeys.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, invitatorsList.size());
        notifyDataSetChanged();
        if (getItemCount() == 0) {
            ((Activity) mContext).finish();
        }
    }

    @Override
    public int getItemCount() {
        return invitatorsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View view;
        ImageView ivInvitatorIcon;
        TextView tvInvitatorUsername;
        Button btnInvitationAccept, btnInvitationRemove;

        int position;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            btnInvitationAccept = view.findViewById(R.id.btnInvitationAccept);
            btnInvitationRemove = view.findViewById(R.id.btnInvitationRemove);
        }

        public void setInvitatorUsername(String username) {
            tvInvitatorUsername = view.findViewById(R.id.tvInvitatorUsername);
            tvInvitatorUsername.setText(username);
        }

        public void setInvitatorIcon(String userID) {
            ivInvitatorIcon = view.findViewById(R.id.ivInvitatorIcon);
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
                            .into(ivInvitatorIcon);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Picasso.get()
                            .load(R.drawable.default_user_avatar)
                            .centerInside()
                            .fit()
                            .transform(new CircleTransform())
                            .into(ivInvitatorIcon);
                }
            });
        }
    }
}
