package com.jameswolfeoliver.pigeon.Presenters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.jameswolfeoliver.pigeon.Services.PigeonService;
import com.jameswolfeoliver.pigeon.Utilities.PigeonApplication;

public class BasePresenter {
    private static final String LOG_TAG = BasePresenter.class.getSimpleName();

    public void startServer(final boolean secure, final ServerStatusCallback callback) {
        Intent serviceIntent = new Intent(PigeonApplication.getAppContext(), PigeonService.class);
        serviceIntent.putExtra(PigeonService.SECURE_KEY, secure);
        serviceIntent.putExtra(PigeonService.COMMAND_KEY, PigeonService.Commands.COMMAND_START);
        BroadcastReceiver serviceInfoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int status = intent.getIntExtra(PigeonService.Broadcast.SERVER_STATUS_KEY, PigeonService.PigeonServerStatus.STOPPED);
                boolean secure = intent.getBooleanExtra(PigeonService.Broadcast.SERVER_SECURE_KEY, false);
                String uri = intent.getStringExtra(PigeonService.Broadcast.SERVER_ADDRESS_KEY);
                PigeonApplication.getAppContext().unregisterReceiver(this);
                if (callback != null) callback.onInfoReceived(uri, secure, status);
            }
        };
        PigeonApplication.getAppContext().registerReceiver(serviceInfoReceiver,
                new IntentFilter(PigeonService.TEXT_SERVICE_UPDATE_ACTION));
        PigeonApplication.getAppContext().startService(serviceIntent);
    }

    public void isServerRunning(final ServerStatusCallback callback) {
        Intent serviceIntent = new Intent(PigeonApplication.getAppContext(), PigeonService.class);
        serviceIntent.putExtra(PigeonService.COMMAND_KEY, PigeonService.Commands.COMMAND_INFO);
        BroadcastReceiver serviceInfoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int status = intent.getIntExtra(PigeonService.Broadcast.SERVER_STATUS_KEY, PigeonService.PigeonServerStatus.STOPPED);
                boolean secure = intent.getBooleanExtra(PigeonService.Broadcast.SERVER_SECURE_KEY, false);
                String uri = intent.getStringExtra(PigeonService.Broadcast.SERVER_ADDRESS_KEY);
                PigeonApplication.getAppContext().unregisterReceiver(this);
                if (callback != null) callback.onInfoReceived(uri, secure, status);
            }
        };
        PigeonApplication.getAppContext().registerReceiver(serviceInfoReceiver,
                new IntentFilter(PigeonService.TEXT_SERVICE_UPDATE_ACTION));
        PigeonApplication.getAppContext().startService(serviceIntent);
    }

    public void tearDownServer(final ServerStatusCallback callback) {
        Intent serviceIntent = new Intent(PigeonApplication.getAppContext(), PigeonService.class);
        serviceIntent.putExtra(PigeonService.COMMAND_KEY, PigeonService.Commands.COMMAND_STOP);
        BroadcastReceiver serviceInfoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int status = intent.getIntExtra(PigeonService.Broadcast.SERVER_STATUS_KEY, PigeonService.PigeonServerStatus.STOPPED);
                boolean secure = intent.getBooleanExtra(PigeonService.Broadcast.SERVER_SECURE_KEY, false);
                String uri = intent.getStringExtra(PigeonService.Broadcast.SERVER_ADDRESS_KEY);
                PigeonApplication.getAppContext().unregisterReceiver(this);
                if (callback != null) callback.onInfoReceived(uri, secure, status);
            }
        };
        PigeonApplication.getAppContext().registerReceiver(serviceInfoReceiver,
                new IntentFilter(PigeonService.TEXT_SERVICE_UPDATE_ACTION));
        PigeonApplication.getAppContext().startService(serviceIntent);
    }

    public interface ServerStatusCallback {
        void onInfoReceived(String uri, boolean secure, int status);
    }
}