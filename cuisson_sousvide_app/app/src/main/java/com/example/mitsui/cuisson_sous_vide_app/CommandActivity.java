package com.example.mitsui.cuisson_sous_vide_app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

import static com.example.mitsui.cuisson_sous_vide_app.MainActivity.RESULT_SUBACTIVITY;

public class CommandActivity extends AppCompatActivity{

    String clientId = MqttClient.generateClientId();
    String USER_NAME = "kies";
    String USER_PASS = "wtpotnt";
    private String device_address;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_command);
        Intent it = getIntent(); //インテントを受け取る
        device_address = it.getStringExtra("device_addr");
        Log.d("message",device_address);

        final MqttAndroidClient client =
                new MqttAndroidClient(this.getApplicationContext(), "tcp://"+ device_address +":1883", clientId);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(USER_NAME);
        options.setPassword(USER_PASS.toCharArray());

        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d("success", "onSuccess");
                    Mqtt_Publish(client, "android/data", "command_activity");

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d("failure", "onFailure");
                }
            });
        } catch (MqttException e){
            e.printStackTrace();
        }

        final Spinner temp_spinner = (Spinner) findViewById(R.id.temp_spinner);
        final Spinner time_spinner = (Spinner) findViewById(R.id.time_spinner);

        ArrayAdapter<String> temp_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        temp_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<String> time_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        time_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for(int i = 30; i < 100; i++){
            temp_adapter.add(String.valueOf(i));
        }

        // temp_pinnerにtemp_dapterを設定
        temp_spinner.setAdapter(temp_adapter);

        for(int i = 0; i < 300; i++){
            time_adapter.add(String.valueOf(i));
        }
        // time_spinnerにtime_dapterを設定
        time_spinner.setAdapter(time_adapter);

        Button clearButton = findViewById(R.id.clear_button);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), WatchActivity.class);
                startActivity(intent);
            }
        });

        Button cookButton = findViewById(R.id.cook_button);
        cookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp = (String)temp_spinner.getSelectedItem();
                String time = (String)time_spinner.getSelectedItem();
                Log.d("temp", temp);
                Log.d("time", time);
                Mqtt_Publish(client, "android/data", "temp: " + temp);
                Mqtt_Publish(client, "android/data", "time: " + time);
                Intent intent = new Intent(getApplication(), WatchActivity.class);
                intent.putExtra("device_addr", device_address);
                startActivityForResult(intent, RESULT_SUBACTIVITY);
            }
        });
    }

    public void Mqtt_Publish(MqttAndroidClient client, String topic, String payload){
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            client.publish(topic, message);
        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }
    }
}
