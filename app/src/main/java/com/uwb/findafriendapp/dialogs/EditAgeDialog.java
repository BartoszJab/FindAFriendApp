package com.uwb.findafriendapp.dialogs;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.uwb.findafriendapp.ProfileActivity;
import com.uwb.findafriendapp.R;

import java.util.HashMap;

public class EditAgeDialog extends DialogFragment {

    private EditText etNewAge;
    private TextView tvAlertAge;
    private Button bCancel, bOk;
    private DatabaseReference dbRef;
    private ImageView ivCancelAge;

    @SuppressLint("ResourceAsColor")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_edit_age, container, false);
        view.setBackgroundResource(R.color.transparent);
        bCancel = view.findViewById(R.id.bCancelAge);
        bOk = view.findViewById(R.id.bOkAge);
        etNewAge = view.findViewById(R.id.etNewAge);
        tvAlertAge = view.findViewById(R.id.tvAlertAge);
        ivCancelAge = view.findViewById(R.id.ivCancelAge);

        // Firebase
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbRef = FirebaseDatabase.getInstance().getReference().child("users").child(userID);

        etNewAge.setText(((ProfileActivity)getActivity()).tvAge.getText().toString());

        // Button that says "Cancel"
        bCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        // Cancel image
        ivCancelAge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        // Button that says "Ok"
        bOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String input = etNewAge.getText().toString();
                if(Integer.parseInt(input)>130 || Integer.parseInt(input)<5){
                    tvAlertAge.setVisibility(View.VISIBLE);
                    tvAlertAge.setText("Invalid age!");
                }
                else if(!input.equals("")){
                    ((ProfileActivity)getActivity()).tvAge.setText(input);
                    updateAge(Integer.parseInt(input)); // Adding age to db
                    getDialog().dismiss();
                }
            }
        });

        return view;
    }

    // Adding age to db method
    private void updateAge(int newAge){
        HashMap map = new HashMap();
        map.put("age", newAge);
        dbRef.updateChildren(map);
    }
}
