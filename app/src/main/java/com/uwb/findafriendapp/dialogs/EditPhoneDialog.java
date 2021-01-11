package com.uwb.findafriendapp.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.uwb.findafriendapp.ProfileActivity;
import com.uwb.findafriendapp.R;

import java.util.HashMap;

public class EditPhoneDialog extends DialogFragment {

    private EditText etNewPhone;
    private Button bCancel, bOk;
    private DatabaseReference dbRef;
    private ImageView ivCancelPhone;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_edit_phone, container, false);
        bCancel = view.findViewById(R.id.bCancelPhone);
        bOk = view.findViewById(R.id.bOkPhone);
        etNewPhone = view.findViewById(R.id.etNewPhone);
        ivCancelPhone = view.findViewById(R.id.ivCancelPhone);

        etNewPhone.setText(((ProfileActivity)getActivity()).tvPhone.getText().toString());

        // Firebase
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbRef = FirebaseDatabase.getInstance().getReference().child("users").child(userID);

        // Button that says "Cancel"
        bCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        ivCancelPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        // Button that says "Ok"
        bOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String input = etNewPhone.getText().toString();
                if(!input.equals("")){
                    ((ProfileActivity)getActivity()).tvPhone.setText(input);
                }

                updatePhone(input); // Adding phone number to db

                getDialog().dismiss();
            }
        });

        return view;
    }

    // Adding phone number to db method
    private void updatePhone(String newPhoneNumber){
        HashMap map = new HashMap();
        map.put("phoneNumber", newPhoneNumber);
        dbRef.updateChildren(map);
    }
}
