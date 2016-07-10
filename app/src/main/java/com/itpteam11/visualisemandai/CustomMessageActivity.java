package com.itpteam11.visualisemandai;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class CustomMessageActivity extends AppCompatActivity {
    EditText editTextMessage;
    Button nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_message);

        //Setup Action bar
        getSupportActionBar().setTitle("Send custom message");

        editTextMessage = (EditText) findViewById(R.id.edit_text_message);
        nextButton = (Button) findViewById(R.id.button_next);

        nextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Intent intent = new Intent(v.getContext(), CustomMessageRecipientsActivity.class);
                String message = editTextMessage.getText().toString();
                intent.putExtra("CustomMessage", message);
                startActivity(intent);
            }
        });
    }
}
