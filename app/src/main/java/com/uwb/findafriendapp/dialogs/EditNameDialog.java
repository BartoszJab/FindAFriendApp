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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.uwb.findafriendapp.ProfileActivity;
import com.uwb.findafriendapp.R;

import java.util.HashMap;

public class EditNameDialog extends DialogFragment {

    private EditText etNewName;
    private Button bCancel, bOk;
    private DatabaseReference dbRef;
    private ImageView ivCancelName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_edit_name, container, false);
        bCancel = view.findViewById(R.id.bCancelName);
        bOk = view.findViewById(R.id.bOkName);
        etNewName = view.findViewById(R.id.etNewName);
        ivCancelName = view.findViewById(R.id.ivCancelName);

        etNewName.setText(((ProfileActivity)getActivity()).tvName.getText().toString());
//        etNewLastName.setText(((ProfileActivity)getActivity()).tvLastName.getText().toString());

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

        ivCancelName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        // Button that says "Ok"
        bOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String inputName = etNewName.getText().toString();
                ((ProfileActivity)getActivity()).tvName.setText(inputName);

                updateName(inputName); // Adding name to db

                getDialog().dismiss();
            }
        });

        return view;
    }

    // Adding name to db method
    private void updateName(String newName){
        HashMap map = new HashMap();
        map.put("name", newName);
        dbRef.updateChildren(map);
    }
}
