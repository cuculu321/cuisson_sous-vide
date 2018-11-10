package com.example.mitsui.cuisson_sous_vide_app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    static final int RESULT_SUBACTIVITY = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final EditText AddressText = findViewById(R.id.device_address);

        Button connectdButton = findViewById(R.id.connect_button);
        connectdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String device_address = AddressText.getText().toString();

                Intent intent = new Intent(getApplication(), ConnectionAcitivity.class);
                intent.putExtra("device_addr", device_address);
                startActivityForResult(intent, RESULT_SUBACTIVITY);
            }
        });

        Button clearButton = findViewById(R.id.clear_button);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddressText.getText().clear();
            }
        });
    }
}