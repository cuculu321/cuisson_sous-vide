package com.example.mitsui.cuisson_sous_vide_app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class CommandActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_command);


        Spinner temp_spinner = (Spinner) findViewById(R.id.temp_spinner);
        Spinner time_spinner = (Spinner) findViewById(R.id.time_spinner);

        ArrayAdapter<String> temp_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        temp_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<String> time_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        time_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for(int i = 30; i < 100; i++){
            temp_adapter.add(String.valueOf(i));
        }

        // temp_pinnerにAdapterを設定
        temp_spinner.setAdapter(temp_adapter);

        for(int i = 0; i < 300; i++){
            time_adapter.add(String.valueOf(i));
        }

        time_spinner.setAdapter(time_adapter);

        Button cookButton = findViewById(R.id.cook_button);
        cookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), WatchActivity.class);
                startActivity(intent);
            }
        });
    }
}
