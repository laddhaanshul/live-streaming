package com.laddha.agoraaudience.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.laddha.agoraaudience.R;
import com.laddha.agoraaudience.adapters.GenericListAdapter;
import com.laddha.agoraaudience.model.AudienceData;
import com.laddha.agoraaudience.model.CommentData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;

public class LiveStreamActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = LiveStreamActivity.class.getName();

    private static final int PERMISSION_REQ_ID_RECORD_AUDIO = 22;

    private RtcEngine mRtcEngine;
    private int mRole;
    private String mRoomName;
    private int uid;
    private String name;
    private long audienceId;
    private TextView roomName;
    private EditText enterComment;
    private Button sendComment;
    private FirebaseFirestore firebaseFirestore;
    private List<CommentData> listObj;
    private GenericListAdapter genericListAdapter;
    private ListView messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_livestream);

        mRole = getIntent().getIntExtra("CRole", 0);
        mRoomName = getIntent().getStringExtra("CName");
        name = getIntent().getStringExtra("audience_name");
        audienceId = System.currentTimeMillis();

        firebaseFirestore = FirebaseFirestore.getInstance();

        roomName = findViewById(R.id.room_name);
        enterComment = findViewById(R.id.enter_comment);
        sendComment = findViewById(R.id.send_comment);
        messageList = findViewById(R.id.msg_list);

        listObj = new ArrayList<>();

        genericListAdapter = new GenericListAdapter(this, listObj);
        messageList.setAdapter(genericListAdapter);

        roomName.setText(mRoomName);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO)) {
                initAgoraEngineAndJoinChannel();
            }
        } else {
            initAgoraEngineAndJoinChannel();
        }

        if (mRole == Constants.CLIENT_ROLE_AUDIENCE) {
            Map<String, Map<String, Object>> commentData = new HashMap<>();
            Map<String, Object> subCmtData = new HashMap<>();
            AudienceData audienceData = new AudienceData();
            audienceData.setUserId(audienceId);
            audienceData.setName(name);
            subCmtData.put(String.valueOf(audienceId), audienceData);
            commentData.put("audience",subCmtData);

            firebaseFirestore.collection("live_stream").document(mRoomName).set(commentData, SetOptions.merge())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully written!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(LiveStreamActivity.this, "Comment posted successfully!!", Toast.LENGTH_SHORT).show();
                            Log.w(TAG, "Error writing document", e);
                        }
                    });
        }

            sendComment.setOnClickListener(this);
    }

    private void initAgoraEngineAndJoinChannel() {
        initializeAgoraEngine();
        setChannelProfile();
        setClientRole();
        joinChannel();
    }

    private void setClientRole() {
        mRtcEngine.setClientRole(mRole);
    }

    public boolean checkSelfPermission(String permission, int requestCode) {
        Log.i(TAG, "checkSelfPermission " + permission + " " + requestCode);
        if (ContextCompat.checkSelfPermission(this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{permission},
                    requestCode);
            return false;
        }
        return true;
    }

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        // Listen for the onJoinChannelSuccess callback.
        // This callback occurs when the local user successfully joins the channel.
        public void onJoinChannelSuccess(String channel, final int uid, int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LiveStreamActivity.this.uid = uid;
                    Log.i("agora", "Join channel success, uid: " + (uid & 0xFFFFFFFFL));
                }
            });
        }


        @Override
        // Listen for the onUserOffline callback.
        // This callback occurs when the host leaves the channel or drops offline.
        public void onUserOffline(final int uid, int reason) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("agora", "User offline, uid: " + (uid & 0xFFFFFFFFL));
                    //onRemoteUserLeft();
                }
            });
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        final DocumentReference docRef = firebaseFirestore.collection("live_stream").document(mRoomName);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {

                    if (snapshot.getData() != null) {
                        Map<String, Object> mainData = mapToJSON(snapshot.getData());
                        Map<String, Object> data = null;
                        if (mainData != null) {
                            data = new TreeMap<String, Object>(mainData);
                        }

                        if (data != null) {
                            Gson gson = new Gson();
                            listObj.clear();
                            for (Map.Entry<String, Object> entry : data.entrySet()) {

                                String jsonStr = gson.toJson(entry.getValue());

                                CommentData value = gson.fromJson(jsonStr, CommentData.class);

                                listObj.add(value);
                            }

                            if (listObj.size() != 0) {
                                genericListAdapter.notifyDataSetChanged();
                                messageList.setSelection(genericListAdapter.getCount() - 1);
                            }
                        }
                    }

                    Log.d(TAG, "Current data: " + snapshot.getData().get("comments"));
                } else {
                    Log.d(TAG, "Current data: null");
                }

            }
        });

    }

    private Map<String, Object> mapToJSON(Map<String, Object> map) {
        JSONObject obj = new JSONObject();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();

            if (!key.contains("comments")) continue;

            Object value = entry.getValue();
            if (value instanceof Map) {
                Map<String, Object> subMap = (Map<String, Object>) value;
                return subMap;
            }
        }
        return null;
    }

    // Initialize the RtcEngine object.
    private void initializeAgoraEngine() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcEventHandler);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }

    private void setChannelProfile() {
        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
    }

    private void joinChannel() {

        mRtcEngine.enableWebSdkInteroperability(true);

        mRtcEngine.joinChannel(null, mRoomName, "Extra Optional Data", uid);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //if (!mCallEnd) {
            leaveChannel();
        //}
        RtcEngine.destroy();
    }

    private void leaveChannel() {
        // Leave the current channel.
        mRtcEngine.leaveChannel();

    }

    public void onEndCallClicked(View view) {
        Log.d("Ans","onEndCallClicked " + view);

        quitCall();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Log.d("Ans","onBackPressed");

        quitCall();
    }

    private void quitCall() {
        if (mRole == Constants.CLIENT_ROLE_AUDIENCE) {
            DocumentReference docRef = firebaseFirestore.collection("live_stream").document(mRoomName);
            Map<String, Object> updates = new HashMap<>();
//            Map<String, Object> subCmtData = new HashMap<>();
//            subCmtData.put(String.valueOf(audienceId), FieldValue.delete());
            updates.put("audience." + String.valueOf(audienceId), FieldValue.delete());

            docRef.update(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                }
            });

        }
            Intent intent = new Intent(LiveStreamActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_comment:
                publishComment();
                break;
        }
    }

    private void publishComment() {
        String commentName = enterComment.getText().toString();

        if (!TextUtils.isEmpty(commentName)) {
            Map<String, Map<String, Object>> commentData = new HashMap<>();
            Map<String, Object> subCmtData = new HashMap<>();
            CommentData data = new CommentData();
            data.setUserId(audienceId);
            data.setCommentMsg(commentName);
            data.setUserName(name);
            subCmtData.put(String.valueOf(System.currentTimeMillis()), data);
            commentData.put("comments",subCmtData);
            enterComment.setText("");
            firebaseFirestore.collection("live_stream").document(mRoomName).set(commentData, SetOptions.merge())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully written!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(LiveStreamActivity.this, "Comment posted successfully!!", Toast.LENGTH_SHORT).show();
                            Log.w(TAG, "Error writing document", e);
                        }
                    });
        } else {
            Toast.makeText(this, "Please enter comment message!!", Toast.LENGTH_SHORT).show();
        }
    }
}
