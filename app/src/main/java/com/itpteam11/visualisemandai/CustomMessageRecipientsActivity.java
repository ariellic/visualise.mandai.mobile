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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CustomMessageRecipientsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private String userID;
    private String userGrp;
    private DatabaseReference dbRef;

    private List<User> listOfWorkingUsers;
    private RecipientsAdapter adapter = null;

    ListView recipientsListView;
    Button sendButton;
    CheckBox box;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_message_recipients);

        //Name action bar
        getSupportActionBar().setTitle("Select Recipients");

        //Get user ID
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //Get message from textbox in prev activity
        Intent intent = getIntent();
        final String message = intent.getStringExtra("CustomMessage");

        sendButton = (Button) findViewById(R.id.buttonSend);
        recipientsListView = (ListView) findViewById(R.id.listViewRecipients);

        dbRef = FirebaseDatabase.getInstance().getReference();

        listOfWorkingUsers = new ArrayList<>();

        dbRef.child("user").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    User user = child.getValue(User.class);
                    if (!user.getStatus().equals("off") && (!child.getKey().equals(userID))) {
                        Map<String, String> userIdAndInfo = new HashMap<String, String>();
                        listOfWorkingUsers.add(user);
                        userIdAndInfo.put(user.getName(), child.getKey());
                        Log.d("UserKey", child.getKey().toString());
                        Log.d("UserListInLoop", user.getName() + ", " + user.getStatus());
                    }
                }

                adapter = new RecipientsAdapter(CustomMessageRecipientsActivity.this, listOfWorkingUsers);
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
                dbRef.child("user").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Map<String, Boolean> receiver = new HashMap<String, Boolean>();
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            User user = child.getValue(User.class);
                            //Get selected users from checkbox in adapter
                            if ((!user.getStatus().equals("off")) && adapter.checkedValue.contains(user.getName()) && (!child.getKey().equals(userID))) {
                                receiver.put(child.getKey(), false);
                            }
                        }

                        Log.d("RECEIVER", receiver.toString());
                        String coordinates = null;
                        if (StaffLocationService.isLocationPermissionGranted()) {
                            coordinates = StaffLocationService.getLatitude() + "-" + StaffLocationService.getLongitude();
                        }
                        //Send notifications to users that are selected
                        Notification notification = new Notification();
                        notification.sendNotification(Notification.NORMAL_NOTIFICATION, message, coordinates, userID, receiver);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                Toast.makeText(CustomMessageRecipientsActivity.this, "Message sent", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(v.getContext(), MainActivity.class);
                intent.putExtra("userID", userID);
                startActivity(intent);
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
