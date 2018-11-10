package com.example.mitsui.cuisson_sous_vide_app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;

public class CommandActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_command);

        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.temp_spinner);
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

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
