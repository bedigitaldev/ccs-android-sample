package com.android.ccssample.call;

import android.media.AudioManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.android.ccssample.BaseActivity;
import com.android.ccssample.R;
import com.ccs.android.client.call.CallClient;
import com.ccs.android.client.call.CallSession;
import com.ccs.android.client.call.listener.OnCallStateListener;

/**
 * Created by Bedigital Developer on 24/02/2016.
 * Email bedigital.devs@gmail.com
 */
public class OutgoingCallScreenActivity extends BaseActivity implements OnCallStateListener {

    private final static String TAG = OutgoingCallScreenActivity.class.getSimpleName();
    private TextView textView;
    private TextView displayName;
    private Button button;
    private ToggleButton speaker;
    private ToggleButton mic;
    private ToggleButton hold;
    private Chronometer chronometer;
    private EditText editText;
    private CallClient callClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing_call_screen);
        textView = (TextView) findViewById(R.id.textView2);
        displayName = (TextView) findViewById(R.id.displayName);
        button = (Button) findViewById(R.id.button);
        chronometer = (Chronometer) findViewById(R.id.chronometer);
        speaker = (ToggleButton) findViewById(R.id.speaker);
        mic = (ToggleButton) findViewById(R.id.mute);
        hold = (ToggleButton) findViewById(R.id.hold);
        setTitle("Outgoing Call");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callClient.hangUp();
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
        Log.d(TAG, "onServiceConnected");
        callClient = getService().getCallClient(this);
        callClient.setOnCallStateListener(this);
        displayName.setText(callClient.getCallSession().getDisplayName());
    }

    @Override
    public void onCallEnded(final CallSession callSession) {
        Log.d(TAG, callSession.toString());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(callSession.getLastReason());
                chronometer.stop();
                finish();
            }
        });
    }

    @Override
    public void onCallEstablished(final CallSession callSession) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(callSession.getLastReason());
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.start();
            }
        });
    }

    @Override
    public void onCallProgressing(final CallSession callSession) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(callSession.getLastReason());
            }
        });
    }

    @Override
    public void onHold(boolean isHold) {

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
}
