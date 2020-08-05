package com.laddha.agoraaudience.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.laddha.agoraaudience.R;
import com.laddha.agoraaudience.interfaces.AppConstants;

import io.agora.rtc.Constants;

public class StartAsBroadcasterActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText roomName;
    private EditText audienceName;
    private Button joinChannel;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startasbroadcaster);

        roomName = findViewById(R.id.channel_name);
        audienceName = findViewById(R.id.audience_name);
        joinChannel = findViewById(R.id.join_live_stream);
        firebaseFirestore = FirebaseFirestore.getInstance();

        joinChannel.setOnClickListener(this);

    }

    public void forwardToLiveRoom(int cRole) {
        String roomN = roomName.getText().toString();
        String name = audienceName.getText().toString();

        if (!TextUtils.isEmpty(roomN)) {
            if (!TextUtils.isEmpty(name)) {
                Intent i = new Intent(StartAsBroadcasterActivity.this, LiveStreamActivity.class);
                i.putExtra(AppConstants.C_ROLE, cRole);
                i.putExtra(AppConstants.C_NAME, roomN);
                i.putExtra(AppConstants.AUDIENCE_NAME, name);
                startActivity(i);
            } else {
                Toast.makeText(this, "Please enter name", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please enter room name", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.join_live_stream:
                forwardToLiveRoom(Constants.CLIENT_ROLE_BROADCASTER);
                break;
        }
    }
}
