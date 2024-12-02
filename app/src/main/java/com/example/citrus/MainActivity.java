package com.example.citrus;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public CardView card1, card2, card3, card4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize the card views and set click listeners
        card1 = findViewById(R.id.btn_capture);
        card2 = findViewById(R.id.btn_instruction);
        card3 = findViewById(R.id.btn_about);
        card4 = findViewById(R.id.btn_manage);

        card1.setOnClickListener(this);
        card2.setOnClickListener(this);
        card3.setOnClickListener(this);
        card4.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent i = null;

        if (v.getId() == R.id.btn_capture) {
            i = new Intent(this, Camera.class);
        } else if (v.getId() == R.id.btn_instruction) {
            i = new Intent(this, instruction.class);  // Make sure this matches your class name
        } else if (v.getId() == R.id.btn_about) {
            i = new Intent(this, about.class);  // Make sure this matches your class name
        } else if (v.getId() == R.id.btn_manage) {
            i = new Intent(this, management.class);  // Make sure this matches your class name
        }

        if (i != null) {
            startActivity(i);
        }
    }
}
