package com.example.mitsui.cuisson_sous_vide_app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

public class ConnectionAcitivity extends AppCompatActivity {

    String clientId = MqttClient.generateClientId();
    String USER_NAME = "kies";
    String USER_PASS = "wtpotnt";
    private String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_connection);

        Intent it = getIntent(); //インテントを受け取る
        message = it.getStringExtra("device_addr");
        Log.d("message",message);

        final MqttAndroidClient client =
                new MqttAndroidClient(this.getApplicationContext(), "tcp://192.168.0.3:1883", clientId);
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
                    Mqtt_Publish(client, "android/data", "hello android");
                    Intent intent = new Intent(getApplication(), CommandActivity.class);
                    startActivity(intent);

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

