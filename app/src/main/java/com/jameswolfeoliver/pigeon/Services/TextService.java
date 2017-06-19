package com.jameswolfeoliver.pigeon.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.jameswolfeoliver.pigeon.Activities.ConnectionActivity;
import com.jameswolfeoliver.pigeon.R;
import com.jameswolfeoliver.pigeon.Server.TextServer;
import com.jameswolfeoliver.pigeon.Utilities.PigeonApplication;
import com.jameswolfeoliver.pigeon.Utilities.Utils;

public class TextService extends Service {
    public static final String TEXT_SERVICE_UPDATE_ACTION = "TEXT_SERVICE_UPDATE_ACTION";
    public static final String COMMAND_KEY = "command";
    public static final String SECURE_KEY = "secure";
    private static final String LOG_TAG = TextService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 1912;
    private TextServer textServer;
    private boolean secureServer;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (intent != null) {
            Log.i(LOG_TAG, Utils.intentToString(intent));
        }

        int command = intent.getIntExtra(COMMAND_KEY, Commands.COMMAND_NONE);
        switch (command) {
            case Commands.COMMAND_START:
                secureServer = intent.getBooleanExtra(SECURE_KEY, false);
                init();
                break;
            case Commands.COMMAND_STOP:
                deInit();
                break;
            case Commands.COMMAND_INFO:
                sendBroadcast(textServer != null && textServer.isStarted() ? Status.RUNNING : Status.STOPPED);
                break;
            case Commands.COMMAND_NONE:
            default:
                // todo
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (textServer == null || !textServer.isStarted()) {
            textServer = new TextServer();
            textServer.start(secureServer, new TextServer.StartServerCallback() {
                @Override
                public void onSuccess(Object[] e) {
                    startForeground(NOTIFICATION_ID, getNotification());
                    sendBroadcast(Status.RUNNING);
                }

                @Override
                public void onFailure(Exception e) {
                    // todo handle
                }
            });
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void init() {
        if (textServer != null && textServer.isStarted()) {
            sendBroadcast(Status.RUNNING);
        }
    }

    public void sendBroadcast(int status) {
        Intent intent = new Intent(TEXT_SERVICE_UPDATE_ACTION);
        intent.putExtra(Broadcast.SERVER_STATUS_KEY, status);
        intent.putExtra(Broadcast.SERVER_SECURE_KEY, textServer != null && textServer.getIsSecure());
        intent.putExtra(Broadcast.SERVER_ADDRESS_KEY, textServer != null ? textServer.getServerUri() : "");
        sendBroadcast(intent);
    }

    private void deInit() {
        if (textServer != null && textServer.isStarted()) {
            textServer.stop();
            textServer = null;
        }
        clearNotification();
        sendBroadcast(Status.STOPPED);
        stopSelf();
    }

    private Notification getNotification() {
        Intent serviceIntent = new Intent(PigeonApplication.getAppContext(), TextService.class);
        serviceIntent.putExtra(TextService.COMMAND_KEY, TextService.Commands.COMMAND_STOP);
        PendingIntent stopIntent = PendingIntent.getService(this, 0, serviceIntent, 0);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, ConnectionActivity.class), 0);
        return new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.app_icon)
                .setWhen(System.currentTimeMillis())
                .addAction(R.drawable.ic_phonelink_off_light, getString(R.string.disconnect), stopIntent)
                .setOngoing(true)
                .setContentTitle(getText(R.string.app_name))
                .setContentText(getText(R.string.server_running))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(String.format(getString(R.string.connected_message), textServer.getServerUri())))
                .setContentIntent(contentIntent)
                .build();
    }

    private void clearNotification() {
        NotificationManager notificationManager = (NotificationManager)
                getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    public static class Commands {
        public static final int COMMAND_INFO = 3;
        public static final int COMMAND_START = 2;
        public static final int COMMAND_STOP = 1;
        public static final int COMMAND_NONE = -1;
    }

    public static class Status {
        public static final int RUNNING = 1;
        public static final int STOPPED = 2;
    }

    public static class Broadcast {
        public static final String SERVER_STATUS_KEY = "server_status";
        public static final String SERVER_ADDRESS_KEY = "server_address";
        public static final String SERVER_SECURE_KEY = "server_is_secure";

    }
}
