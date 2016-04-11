package com.android.ccssample;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.android.ccssample.gcm.RegistrationIntentService;
import com.ccs.android.client.CcsClientInt;
import com.ccs.android.client.service.ClientService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

/**
* Created by eriksuprayogi on 9/29/15.
*/
public abstract class BaseActivity extends AppCompatActivity implements ServiceConnection {

    private final static String TAG = BaseActivity.class.getSimpleName();
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private AppService service;

    protected CcsClientInt ccsClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            com.ccs.android.client.utils.Log.d(TAG, "Start IntentService to register this application with GCM", com.ccs.android.client.utils.Log.SDK);
            startService(new Intent(this, RegistrationIntentService.class));
        }
        getApplicationContext().bindService(new Intent(this, AppService.class), this, BIND_AUTO_CREATE);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        if (AppService.class.getName().equals(componentName.getClassName())) {
            service = ((AppService.ServiceBinder) iBinder).getService();
            ccsClient = service.getCcsClient();
            onServiceConnected();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        service = null;
        onServiceDisconnected();
    }

    protected void onServiceConnected() {
        // for subclasses
        Log.d(TAG, "onServiceConnected");
    }

    protected void onServiceDisconnected() {
        // for subclasses
        Log.d(TAG, "onServiceDisconnected");
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    public ClientService getService() {
        return service;
    }
}
