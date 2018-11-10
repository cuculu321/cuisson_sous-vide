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

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for(int i = 30; i < 100; i++){
            adapter.add(String.valueOf(i));
        }
        Spinner temp_spinner = (Spinner) findViewById(R.id.temp_spinner);

        // SpinnerにAdapterを設定
        temp_spinner.setAdapter(adapter);

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
