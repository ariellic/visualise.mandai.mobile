package com.itpteam11.visualisemandai;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.sql.Time;
import java.util.Date;

/**
 *  This service retrieve all different showtime from Singapore Zoo website.
 *  Retrieved showtime will be compared with existing database time.
 *  When different between website and database showtime occurs, notification will be send to the requested user.
 */
public class CheckShowtimeService extends IntentService {
    public final static String USER_ID = "userID";
    public final static String[] SHOWS = {"splash-safari-show",
                                          "animal-friends-show"};

    private String userID;

    public CheckShowtimeService() {
        super("CheckShowtimeService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Get authenticated user ID from Intent
        userID = intent.getStringExtra(USER_ID);

        //Check online showtime value for every show
        for(int i=0; i<SHOWS.length; i++) {
            //Get database reference node of the show
            FirebaseDatabase.getInstance().getReference().child("service").child(SHOWS[i]).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //Store show's details into SHowtime object
                    Showtime showtime = dataSnapshot.getValue(Showtime.class);

                    //Retrieve HTML document from a URL that is contain inside the given showtime object
                    //Process is done using AsyncTask method
                    HTMLParsingAsyncTask htmlParsing = new HTMLParsingAsyncTask();
                    htmlParsing.execute(showtime, dataSnapshot.getKey(), userID);
                }
                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to get showtime
                    System.out.println("Failed to get showtime: " + error.toException());
                }
            });
        }

    }

    //AsyncTask for getting and parsing HTML document to get showtime
    private class HTMLParsingAsyncTask extends AsyncTask<Object, Void, String> {
        private Showtime showtime;
        private String showType;
        private String userID;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Object... params) {
            showtime = (Showtime) params[0];
            showType = (String) params[1];
            userID = (String) params[2];

            System.out.println("Showtime Service - Show type: " + showType);
            try {
                //Get HTML document using jsoup library
                Document doc = Jsoup.connect(showtime.getUrl()).get();
                System.out.println("Showtime Service - Showtime URL: " + showtime.getUrl());

                //Return the retrieved showtime by using Selector-Syntex
                return doc.select(showtime.getSelector()).text();
            } catch (IOException e) { e.printStackTrace(); }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            System.out.println("Showtime Service - Showtime value in database: " + showtime.getValue());
            System.out.println("Showtime Service - Showtime vaule from website: " + result);

            if(!showtime.getValue().equals(result)) {
                //Update database when showtime changes
                FirebaseDatabase.getInstance().getReference().child("service").child(showType).child("value").setValue(result);
                System.out.println("Showtime Service - Time Changes");
            }
            else {
                //Create no time change notification to the requested user
                System.out.println("Showtime Service - User ID: " + userID);
                Message message = new Message("No change to " + showType + " showtime. " + result, showType + " Notification", new Date().getTime());
                FirebaseDatabase.getInstance().getReference().child("message").child("user").child(userID).push().setValue(message);
            }
        }
    }
}
