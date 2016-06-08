package com.itpteam11.visualisemandai;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class InfoFragment extends Fragment implements View.OnClickListener {
    private Button btnTest;

    public InfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_info, container, false);

       // btnTest = (Button) view.findViewById(R.id.test_button);
        //btnTest.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        /*TOdo: Button button = (Button) v;

        //Run respective switch statement accordingly to the pressed button
        switch(button.getId()) {
            case R.id.test_button:
                DatabaseReference database = FirebaseDatabase.getInstance().getReference();

                User u = ((MainActivity)getActivity()).getUser();
                database.child("user").push().setValue(u);
                break;
        }*/
    }
}
