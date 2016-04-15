package com.android.ccssample;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.ccssample.gcm.QuickstartPreferences;
import com.ccs.android.client.ErrorType;
import com.ccs.android.client.call.listener.CallbackResults;

/**
 * Created by Bedigital Developer on 24/02/2016.
 * Email bedigital.devs@gmail.com
 */
public class LoginActivity extends BaseActivity implements CallbackResults {

    private final String TAG = LoginActivity.class.getSimpleName();
    private EditText userTxt;
    private EditText passwordTxt;
    private Button loginBtn;
    private TextView registerBtn;
    private ProgressDialog mSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        userTxt = (EditText) findViewById(R.id.username);
        passwordTxt = (EditText) findViewById(R.id.password);
        loginBtn = (Button) findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startClient();
            }
        });
        startService(new Intent(this, AppService.class));
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        if (getService().isCcsClientStarted()) {
            openMainActivity();
        }
    }

    private void startClient() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String username = userTxt.getText().toString();
        String password = passwordTxt.getText().toString();
        String gcm_token = sharedPreferences.getString(QuickstartPreferences.REGISTRATION_ID, "");
        if (!getService().isCcsClientStarted()) {
            showSpinner();
            getService().startCcsClient(username, password, gcm_token, this);
        }
    }

    @Override
    public void onFailed(ErrorType errorType) {
        if (mSpinner != null) {
            mSpinner.dismiss();
        }
        Toast.makeText(LoginActivity.this, errorType.getDescription(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSuccess() {
        if (mSpinner != null) {
            mSpinner.dismiss();
        }
        openMainActivity();
    }

    private void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void showSpinner() {
        mSpinner = new ProgressDialog(this);
        mSpinner.setTitle("Logging in");
        mSpinner.setMessage("Please wait...");
        mSpinner.show();
    }

}
