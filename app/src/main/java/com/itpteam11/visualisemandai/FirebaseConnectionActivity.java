package com.itpteam11.visualisemandai;

import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

public class FirebaseConnectionActivity extends AppCompatActivity implements OnClickListener {

    private Button btnAdd;
    private EditText etEmail, etPassword;

    private FirebaseAuth authentication;
    private FirebaseAuth.AuthStateListener authenticationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase_connection);

        etEmail = (EditText) findViewById(R.id.firebaseconnection_edittext_email);
        etPassword = (EditText) findViewById(R.id.firebaseconnection_edittext_password);
        btnAdd = (Button) findViewById(R.id.firebaseconnection_button_add);

        btnAdd.setOnClickListener(this);

        // Create Firebase Authentication instance and append a listener to check login status
        authentication = FirebaseAuth.getInstance();
        authenticationListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    System.out.println("onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    System.out.println("onAuthStateChanged:signed_out");
                }
            }
        };

        // Signin using firebase authentication to use the database
        authentication.signInWithEmailAndPassword(getString(R.string.authentication_email), getString(R.string.authentication_password))
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        System.out.println("signInWithEmail:onComplete:" + task.isSuccessful());

                        if (!task.isSuccessful()) {
                            System.out.println("signInWithEmail " + task.getException());
                        }
                    }
                });

        System.out.println("Timestamp: " + ServerValue.TIMESTAMP);
    }

    @Override
    public void onClick(View v) {
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);

        Button button = (Button) v;

        switch(button.getId()) {
            case R.id.firebaseconnection_button_add:
                //Create an object and assign respective value into it
                //User testUser = new User(etEmail.getText().toString(), etPassword.getText().toString());
                System.out.println("Email: " + etEmail.getText().toString());
                System.out.println("Password: " + etPassword.getText().toString());

                // Create Firebase instance
                DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("test");

                // Add object into database
                db.push().setValue(ServerValue.TIMESTAMP);
                //db.push().setValue(testUser);

                // The following code is read data when changes happen
                /*db.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Object value = dataSnapshot.getValue();
                        System.out.println("Value is: " + value.toString());
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        System.out.println(error.toException());
                    }
                });*/
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        authentication.addAuthStateListener(authenticationListener);

        // Create database reference to check for changes
        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("test");
        db.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        System.out.println("Database Changed");
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        System.out.println(error.toException());
                    }
                });
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authenticationListener != null) {
            authentication.removeAuthStateListener(authenticationListener);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //Sign out from account
        FirebaseAuth.getInstance().signOut();
    }
}
