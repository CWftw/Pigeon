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
import com.jameswolfeoliver.pigeon.Server.ChatServer;
import com.jameswolfeoliver.pigeon.Server.PigeonServer;
import com.jameswolfeoliver.pigeon.Utilities.PigeonApplication;
import com.jameswolfeoliver.pigeon.Utilities.SharedPrefKeys;
import com.jameswolfeoliver.pigeon.Utilities.Utils;

import java.io.IOException;

public class PigeonService extends Service {
    public static final String TEXT_SERVICE_UPDATE_ACTION = "TEXT_SERVICE_UPDATE_ACTION";
    public static final String COMMAND_KEY = "command";
    public static final String SECURE_KEY = "secure";
    private static final String LOG_TAG = PigeonService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 1912;
    private PigeonServer pigeonServer;
    private ChatServer chatServer;
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
                sendBroadcast();
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
        if (pigeonServer == null || !pigeonServer.isAlive()) {
            int port = PigeonApplication.getSharedPreferences().getInt(SharedPrefKeys.PIGEON_SERVER_PORT_KEY, PigeonServer.DEFAULT_PORT);
            pigeonServer = new PigeonServer(port);
            pigeonServer.start(secureServer, new PigeonServer.StartServerCallback() {
                @Override
                public void onSuccess(Object[] e) {
                    startForeground(NOTIFICATION_ID, getNotification());
                }

                @Override
                public void onFailure(Exception e) {
                    // todo handle
                }
            });
        }

        if (chatServer == null) {
            int port = PigeonApplication.getSharedPreferences().getInt(SharedPrefKeys.CHAT_SERVER_PORT_KEY, ChatServer.DEFAULT_PORT);
            chatServer = new ChatServer(port);
            try {
                chatServer.start();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Failed to start ChatServer on port " + port, e);
            }
        }
        sendBroadcast();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void init() {
        sendBroadcast();
    }

    public void sendBroadcast() {
        Intent intent = new Intent(TEXT_SERVICE_UPDATE_ACTION);
        intent.putExtra(Broadcast.SERVER_STATUS_KEY, pigeonServer != null && pigeonServer.isAlive() ? PigeonServerStatus.RUNNING : PigeonServerStatus.STOPPED);
        intent.putExtra(Broadcast.CHAT_SERVER_STATUS_KEY, chatServer != null && chatServer.isAlive() ? ChatServerStatus.RUNNING : ChatServerStatus.STOPPED);
        intent.putExtra(Broadcast.WEBSOCKET_STATUS_KEY, chatServer != null && chatServer.isAlive() && chatServer.isWebsocketOpen() ? WebsocketStatus.WEBSOCKET_OPEN : WebsocketStatus.WEBSOCKET_CLOSED);
        intent.putExtra(Broadcast.SERVER_SECURE_KEY, pigeonServer != null && pigeonServer.getIsSecure());
        intent.putExtra(Broadcast.SERVER_ADDRESS_KEY, pigeonServer != null ? pigeonServer.getServerUri() : "");
        intent.putExtra(Broadcast.SERVER_PORT_KEY, pigeonServer != null ? pigeonServer.getListeningPort() : 0);
        intent.putExtra(Broadcast.WEBSOCKET_PORT_KEY, chatServer != null ? chatServer.getListeningPort() : 0);
        Log.i(LOG_TAG, Utils.intentToString(intent));
        sendBroadcast(intent);
    }

    private void deInit() {
        if (pigeonServer != null && pigeonServer.isAlive()) {
            pigeonServer.stop();
            pigeonServer = null;
        }
        if (chatServer != null && chatServer.isAlive()) {
            chatServer.stop();
            chatServer = null;
        }
        clearNotification();
        sendBroadcast();
        stopSelf();
    }

    private Notification getNotification() {
        Intent serviceIntent = new Intent(PigeonApplication.getAppContext(), PigeonService.class);
        serviceIntent.putExtra(PigeonService.COMMAND_KEY, PigeonService.Commands.COMMAND_STOP);
        PendingIntent stopIntent = PendingIntent.getService(this, 0, serviceIntent, 0);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, ConnectionActivity.class), 0);
        return new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.app_icon)
                .setWhen(System.currentTimeMillis())
                .addAction(R.drawable.ic_phonelink_off_light, getString(R.string.disconnect), stopIntent)
                .setOngoing(true)
                .setContentTitle(getText(R.string.app_name))
                .setContentText(getText(R.string.server_running))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(String.format(getString(R.string.connected_message), pigeonServer.getServerUri())))
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

    public static class PigeonServerStatus {
        public static final int RUNNING = 1;
        public static final int STOPPED = 2;
    }

    public static class ChatServerStatus {
        public static final int RUNNING = 1;
        public static final int STOPPED = 2;
    }

    public static class WebsocketStatus {
        public static final int WEBSOCKET_OPEN = 3;
        public static final int WEBSOCKET_CLOSED = 4;
    }

    public static class Broadcast {
        public static final String WEBSOCKET_STATUS_KEY = "websocket_status";
        public static final String CHAT_SERVER_STATUS_KEY = "chat_server_status";
        public static final String SERVER_STATUS_KEY = "server_status";
        public static final String SERVER_ADDRESS_KEY = "server_address";
        public static final String SERVER_SECURE_KEY = "server_is_secure";
        public static final String SERVER_PORT_KEY = "server_port";
        public static final String WEBSOCKET_PORT_KEY = "websocket_port";
    }
}
