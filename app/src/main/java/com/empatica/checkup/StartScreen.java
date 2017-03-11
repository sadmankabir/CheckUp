package com.empatica.checkup;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.view.View.OnClickListener;


public class StartScreen extends AppCompatActivity {

    Button button;
    Button button2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);

        // Locate the button in activity_main.xml
        button = (Button) findViewById(R.id.button);
        button2 = (Button) findViewById(R.id.button2);

        // Capture button clicks
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {

                // Start NewActivity.class
                Intent myIntent = new Intent(StartScreen.this,
                        PatientView.class);
                startActivity(myIntent);
            }
        });

        // Capture button clicks
        button2.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {

                // Start NewActivity.class
                Intent myIntent = new Intent(StartScreen.this,
                        MainActivity.class);
                startActivity(myIntent);
            }
        });


    }
}
