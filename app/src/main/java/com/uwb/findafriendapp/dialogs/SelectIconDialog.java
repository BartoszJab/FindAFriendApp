package com.uwb.findafriendapp.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.uwb.findafriendapp.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectIconDialog extends DialogFragment {

    private static final String TAG = "SelectIconDialog";

    public interface OnInputListener {
        void sendInput(String iconName);
    }
    public OnInputListener myOnInputListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.dialog_icon_selector, container, false);

        final LinearLayout layoutIconSelector = view.findViewById(R.id.layoutIconSelector);

        FirebaseStorage.getInstance().getReference().child("icons").listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {

                        @SuppressLint("ResourceType") String bgColor = getResources().getString(R.color.event_blue);

                        LinearLayout tempLinearLayout = new LinearLayout(getContext());
                        LinearLayout.LayoutParams tempLayoutParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, 250
                        );
                        tempLayoutParams.setMargins(0, 0, 0, 10);

                        int count = 0;
                        for (final StorageReference item : listResult.getItems()) {
                            Log.d(TAG, "ITEM: " + item);
                            Log.d(TAG, "onSuccess: " + item.getName());

                            final ImageView imageView = new ImageView(getContext());

                            // set image
                            StorageReference iconStorageRef = FirebaseStorage.getInstance().getReference().child("icons/" + item.getName());
                            iconStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Log.d(TAG, "URI IN PICASSO: " + uri);
                                    Picasso.get()
                                            .load(uri)
                                            .centerInside()
                                            .fit()
                                            .into(imageView);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "SOMETHING WENT WRONG");
                                }
                            });

                            imageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Log.d(TAG, "Sent " + item.getName());
                                    Log.d(TAG, "download url: " + item.getDownloadUrl());
                                    StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("icons/" + item.getName());
                                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Log.d(TAG, "storageReference.getDownloadUrl().onSuccess");
                                            Log.d(TAG, "Sent URI: " + uri);
                                            myOnInputListener.sendInput(item.getName());
                                            getDialog().dismiss();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d(TAG, "storageReference.getDownloadUrl().onFailure");
                                        }
                                    });
                                }
                            });

                            imageView.setBackgroundColor(Color.parseColor(bgColor));
                            imageView.setPadding(20,20,20,20);

                            // set whole layout
                            if (count % 2 == 0) {
                                // set layout that holds icons
                                tempLinearLayout = new LinearLayout(getContext());
                                tempLinearLayout.setLayoutParams(tempLayoutParams);
                                tempLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                                tempLinearLayout.setPadding(10,10,10,10);

                                // image view params
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 1
                                );
                                imageView.setLayoutParams(params);

                                tempLinearLayout.addView(imageView);
                            } else {
                                // image view params
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 1
                                );
                                params.setMargins(20,0,0,0);
                                imageView.setLayoutParams(params);

                                tempLinearLayout.addView(imageView);
                                layoutIconSelector.addView(tempLinearLayout);
                            }

                            Log.d(TAG, "COUNT: " + count);
                            count++;
                        }
                        if (count % 2 != 0) {
                            layoutIconSelector.addView(tempLinearLayout);
                        }


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            myOnInputListener = (OnInputListener) getActivity();
        } catch (ClassCastException e) {
            Log.d(TAG, "onAttach: ClassCastException: " + e.getMessage());
        }
    }


}
