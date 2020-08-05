package com.laddha.agoraaudience.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.laddha.agoraaudience.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button broadcasterButton;
    private Button audienceButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        broadcasterButton = findViewById(R.id.broadcaster);
        audienceButton = findViewById(R.id.audience);

        broadcasterButton.setOnClickListener(this);
        audienceButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.broadcaster:
                Intent intentToBroadcaster = new Intent(MainActivity.this, StartAsBroadcasterActivity.class);
                startActivity(intentToBroadcaster);
                break;
            case R.id.audience:
                Intent intentToAudience = new Intent(MainActivity.this, StartAsAudienceActivity.class);
                startActivity(intentToAudience);
                break;
        }
    }
}
