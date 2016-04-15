package com.android.ccssample;

import android.media.AudioManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.ccs.android.client.call.CallClient;
import com.ccs.android.client.call.CallSession;
import com.ccs.android.client.call.listener.OnCallStateListener;

/**
 * Created by Bedigital Developer on 02/10/2015.
 * Email bedigital.devs@gmail.com
 */
public class IncomingCallScreenActivity extends BaseActivity implements OnCallStateListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private final static String TAG = IncomingCallScreenActivity.class.getSimpleName();
    private Button hangup;
    private Button accept;
    private Chronometer chronometer;
    private TextView textView;
    private TextView displayName;
    private ToggleButton speaker;
    private ToggleButton mic;
    private ToggleButton hold;
    private CallClient callClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call_screen);
        textView = (TextView) findViewById(R.id.textView);
        displayName = (TextView) findViewById(R.id.displayName);
        hangup = (Button) findViewById(R.id.hangup);
        accept = (Button) findViewById(R.id.accept);
        chronometer = (Chronometer) findViewById(R.id.chronometer);
        speaker = (ToggleButton) findViewById(R.id.speaker);
        mic = (ToggleButton) findViewById(R.id.mute);
        hold = (ToggleButton) findViewById(R.id.hold);
        setTitle("Incoming Call");
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            CallSession callSession = bundle.getParcelable("call_session");
            displayName.setText(callSession.getDisplayName());
        }

        hangup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callClient.hangUp();
            }
        });

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callClient.answerCall();
            }
        });

        speaker.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                callClient.setSpeaker(b);
            }
        });

        mic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                callClient.muteCall(b);
            }
        });

        hold.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    callClient.setHold();
                } else {
                    callClient.unHold();
                }
            }
        });

    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        callClient = getService().getCallClient(this);
        callClient.setOnCallStateListener(this);
        displayName.setText(callClient.getCallSession().getDisplayName());
    }


    @Override
    public void onCallEnded(final CallSession calls) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(calls.getLastReason());
                chronometer.stop();
                finish();
            }
        });
    }

    @Override
    public void onCallEstablished(final CallSession calls) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(calls.getLastReason());
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.start();
                accept.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onCallProgressing(final CallSession calls) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(calls.getLastReason());
            }
        });
    }

    @Override
    public void onHold(boolean isHold) {
        hold.setChecked(isHold);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
                int action = AudioManager.ADJUST_RAISE;
                if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                    action = AudioManager.ADJUST_LOWER;
                }
                if(callClient!=null) {
                    callClient.adjustVolume(action, AudioManager.FLAG_SHOW_UI);
                }
                return true;
            default:
                // Nothing to do
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        callClient.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
