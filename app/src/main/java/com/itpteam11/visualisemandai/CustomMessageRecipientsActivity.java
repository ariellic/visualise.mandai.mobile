package com.itpteam11.visualisemandai;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomMessageRecipientsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private String userID;
    private String userGrp;
    private DatabaseReference dbRef;

    private ArrayList<Map<String, User>> userInfoPairList;
    private List<User> listOfUsers;
    private RecipientsAdapter adapter = null;

    ListView recipientsListView;
    Button sendButton;
    CheckBox box;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_message_recipients);

        getSupportActionBar().setTitle("Select Recipients");

        Intent intent = getIntent();
        String message = intent.getStringExtra("CustomMessage");


        sendButton = (Button) findViewById(R.id.buttonSend);
        recipientsListView = (ListView) findViewById(R.id.listViewRecipients);

        dbRef = FirebaseDatabase.getInstance().getReference();
        userInfoPairList = new ArrayList<Map<String, User>>();
        listOfUsers = new ArrayList<>();
        //checkedValue = new ArrayList<>();

        dbRef.child("user").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    User user = child.getValue(User.class);
                    if (!user.getStatus().equals("off")) {
                        Map<String, User> userIdAndInfo = new HashMap<String, User>();
                        listOfUsers.add(user);
                        userIdAndInfo.put(child.getKey(), user);
                        Log.d("UserKey", child.getKey().toString());
                        Log.d("UserListInLoop", user.getName() + ", " + user.getStatus());
                        userInfoPairList.add(userIdAndInfo);
                    }
                }

                Log.d("UserList", userInfoPairList.toString());
                adapter = new RecipientsAdapter(CustomMessageRecipientsActivity.this, listOfUsers);
                recipientsListView.setAdapter(adapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        recipientsListView.setOnItemClickListener(this);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CustomMessageRecipientsActivity.this, adapter.checkedValue.toString(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onItemClick(AdapterView arg0, View v, int position, long id) {
        Log.d("OnItemClick", arg0.toString() + " " + v.toString() + " " + position  + " " + id);
        CheckBox box = (CheckBox) v.findViewById(R.id.checkBox);
        TextView name = (TextView) v.findViewById(R.id.userNameStatus);
        String textViewValue = name.getText().toString();

        if (box.isChecked() && !adapter.checkedValue.contains(textViewValue)) {
            adapter.checkedValue.add(textViewValue);
        } else if (!box.isChecked() && adapter.checkedValue.contains(textViewValue)) {
            adapter.checkedValue.remove(textViewValue);
        }
    }
}
