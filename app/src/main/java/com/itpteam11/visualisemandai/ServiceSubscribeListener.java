package com.itpteam11.visualisemandai;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

/**
 *  This class create an event listener to listen on user's subscribed services to
 *  notify for any updates
 */
public class ServiceSubscribeListener {
    private String userID;
    private Map<String, Boolean> subscribedServiceList;

    public ServiceSubscribeListener(String userID, Map<String, Boolean> subscribedServiceList) {
        this.userID = userID;
        this.subscribedServiceList = subscribedServiceList;
    }

    //This method will create and listen to the subscribed service's notification-id node
    //If there were to be any update, the notification-id will changes and user'd will be
    //notified by inserting the notification ID into user's receive notification node
    public void startListening() {
        if(subscribedServiceList != null) {
            //Get a list for subscribed service
            String[] serviceName = subscribedServiceList.keySet().toArray(new String[0]);

            //For each service, create an event listener
            for(int i=0; i<serviceName.length; i++) {
                System.out.println("ServiceSubscribeListener - Start listening for: " + serviceName[i]);

                //create event listener
                FirebaseDatabase.getInstance().getReference().child("service").child(serviceName[i]).child("notification-id").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Retrieve notification ID
                        String notificationID = dataSnapshot.getValue(String.class);

                        //Add user id into notification's receive list for record purpose. Value is set false until recipient show notification(set true)
                        FirebaseDatabase.getInstance().getReference().child("notification").child(notificationID).child("receiver").child(userID).setValue(false);

                        //Add notification ID into user's receive list so to be notified. Value is set false until recipient show notification(set true)
                        FirebaseDatabase.getInstance().getReference().child("notification-lookup").child(userID).child("receive").child(notificationID).setValue(false);
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {
                        //Failed on listening for changes
                        System.out.println("ServiceSubscribeListener - Failed on listening for changes: " + error.toException());
                    }
                });
            }
        }
    }
}
