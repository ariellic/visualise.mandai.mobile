package com.itpteam11.visualisemandai;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This activity allows the manager to select recipients (staff who are working) to send to for
 * the message that has been crafted in the previous activity
 */
public class CustomAlertRecipientsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "CustomAlertRecipients";

    private String userID;
    private DatabaseReference dbRef;
    private StorageReference storageRef;

    private List<User> listOfWorkingUsers;
    Map<String, Boolean> receiver = new HashMap<String, Boolean>();
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

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReferenceFromUrl("gs://visualise-mandai.appspot.com");

        //Get user ID
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //Get message from textbox and the image path in prev activity
        Intent intent = getIntent();
        final String message = intent.getStringExtra("CustomAlert");
        final String imgPath = intent.getStringExtra("ImagePath");

        sendButton = (Button) findViewById(R.id.buttonSend);
        recipientsListView = (ListView) findViewById(R.id.listViewRecipients);

        dbRef = FirebaseDatabase.getInstance().getReference();

        listOfWorkingUsers = new ArrayList<>();

        dbRef.child("user").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    User user = child.getValue(User.class);
                    // Get users who are working
                    if (user.getStatus() != null && !user.getStatus().equals("off") && (!child.getKey().equals(userID))) {
                        Map<String, String> userIdAndInfo = new HashMap<String, String>();
                        listOfWorkingUsers.add(user);
                        userIdAndInfo.put(user.getName(), child.getKey());
                        Log.d("UserKey", child.getKey().toString());
                        Log.d("UserListInLoop", user.getName() + ", " + user.getStatus());
                    }
                }

                // Set the adapter of the listview to be populated with the list of working users
                adapter = new RecipientsAdapter(CustomAlertRecipientsActivity.this, listOfWorkingUsers);
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
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            User user = child.getValue(User.class);
                            // Get selected users from checkbox in adapter and make sure their
                            // status is still 'working'
                            if (user.getStatus() != null && (!user.getStatus().equals("off")) && adapter.checkedValue.contains(user.getName()) && (!child.getKey().equals(userID))) {
                                receiver.put(child.getKey(), false);
                            }
                        }

                        if (!receiver.isEmpty()) {
                            Log.d("RECEIVER", receiver.toString());

                            //Get user's coordinates
                            Double latitude = null;
                            Double longitude = null;
                            if (StaffLocationService.isLocationPermissionGranted()) {
                                latitude = StaffLocationService.getLatitude();
                                longitude = StaffLocationService.getLongitude();
                            }

                            Log.d(TAG, "Image path: " + imgPath);

                            Notification notification = new Notification();

                            //Store image if available
                            if (!imgPath.equals("")) {
                                String imgName = uploadImage(imgPath, notification, message, latitude, longitude);
                                //notification.sendNotification(Notification.IMAGE_NOTIFICATION, message, latitude, longitude, userID, receiver, imgName);
                            } else {
                                notification.sendNotification(Notification.NORMAL_NOTIFICATION, message, latitude, longitude, userID, receiver, null);
                            }

                            //Send notifications to users that are selected
                            Toast.makeText(CustomAlertRecipientsActivity.this, "Alert sent", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(CustomAlertRecipientsActivity.this, MainActivity.class);
                            intent.putExtra("userID", userID);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                        else {
                            Toast.makeText(CustomAlertRecipientsActivity.this, "Please select at least 1 recipient", Toast.LENGTH_SHORT).show();

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

    }

    /**
     * Upload photo file to Firebase Storage and send notifications to reespective staff when
     * uploading succeeds
     * @param path
     * @param noti
     * @param msg
     * @param lati
     * @param longi
     * @return
     */
    public String uploadImage(String path, final Notification noti, final String msg, final Double lati, final Double longi) {
        Uri file = Uri.fromFile(new File(path));
        StorageReference imgRef = storageRef.child("custom_alerts/" + file.getLastPathSegment());
        UploadTask uploadTask = imgRef.putFile(file);
        final String imgName = file.getLastPathSegment();
        Log.d(TAG, "Image file name: " + imgName);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(TAG, "Upload unsuccessful");
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Log.d(TAG, "Upload successful");
                noti.sendNotification(Notification.IMAGE_NOTIFICATION, msg, lati, longi, userID, receiver, imgName);
            }
        });
        return imgName;
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
