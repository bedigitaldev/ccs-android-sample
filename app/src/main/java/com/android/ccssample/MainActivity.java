package com.android.ccssample;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.ccssample.call.OutgoingCallScreenActivity;
import com.android.ccssample.messaging.ChatRoomActivity;
import com.ccs.android.client.ErrorType;
import com.ccs.android.client.call.CallClient;
import com.ccs.android.client.call.listener.OnCallListener;

public class MainActivity extends BaseActivity {

    private CallClient call;
    private EditText numberText;
    private Button onCallBtn, offnetCallBtn, chatBtn;
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        numberText = (EditText) findViewById(R.id.number);
        onCallBtn = (Button) findViewById(R.id.onetCallBtn);
        offnetCallBtn = (Button) findViewById(R.id.offnetCallBtn);
        chatBtn = (Button) findViewById(R.id.chat);

        onCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String number = numberText.getText().toString();
                call = getService().getCallClient(MainActivity.this).onetCall(number, new MyCallListener());
            }
        });

        offnetCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String number = numberText.getText().toString();
                call = getService().getCallClient(MainActivity.this).offnetCall(number, new MyCallListener());
            }
        });

        chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userId = numberText.getText().toString();
                if (userId.isEmpty())
                    return;
                startActivity(ChatRoomActivity.createIntent(MainActivity.this, userId));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_close:
                getService().stopCcsClient();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class MyCallListener implements OnCallListener {
        @Override
        public void onCallFailed(ErrorType errorType) {
//            Log.e(TAG, errorType.getDescription());
        }

        @Override
        public void onCallStarted() {
            Intent intent = new Intent(MainActivity.this, OutgoingCallScreenActivity.class);
            startActivity(intent);
        }
    }
}
