package com.itpteam11.visualisemandai;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class SpecialPowerActivity extends AppCompatActivity {
    private Button clearAllNotificationButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_special_power);

        clearAllNotificationButton = (Button) findViewById(R.id.special_power_activity_clear_notifications);

        clearAllNotificationButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Show dialog for disable user
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(SpecialPowerActivity.this);
                alertDialog.setTitle("Clear All Notifications");
                alertDialog.setMessage("All users receive and send notifications lookup together with all actual notifications will be deleted. There is no way back! Are you really sure to delete all notifications?");
                alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
                alertDialog.setCancelable(false);

                alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseDatabase.getInstance().getReference().child("notification-lookup").removeValue();
                        FirebaseDatabase.getInstance().getReference().child("notification").removeValue();
                    }
                });


                alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // No action
                    }
                });

                alertDialog.show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //Sign out from Firebase Authentication account
        FirebaseAuth.getInstance().signOut();
    }
}
