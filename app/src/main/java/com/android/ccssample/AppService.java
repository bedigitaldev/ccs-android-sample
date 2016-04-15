package com.android.ccssample;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import com.ccs.android.client.Configuration;
import com.ccs.android.client.service.ClientService;

/**
 * Created by Bedigital Developer on 24/02/2016.
 * Email bedigital.devs@gmail.com
 */
public class AppService extends ClientService {

    @Override
    protected Configuration getConfiguration() {
        Configuration configuration = new Configuration(this)
                .certificate(getString(R.string.certificate))
                .enableVoice(true)
                .enableMessaging(true)
                .transport(Configuration.TRANSPORT_TLS);
        return configuration;
    }

    @Override
    protected void onCreateService() {

    }

    @Override
    protected void onStartCommandService(Intent intent, int i, int i1) {
        Toast.makeText(AppService.this, "Service CCS started", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected boolean onUnbindService(Intent intent) {
        return false;
    }

    @Override
    protected void onDestroyService() {

    }

    @Override
    protected IBinder getBinder() {
        return new ServiceBinder();
    }

    public class ServiceBinder extends Binder {
        public AppService getService() {
            return AppService.this;
        }
    }
}
