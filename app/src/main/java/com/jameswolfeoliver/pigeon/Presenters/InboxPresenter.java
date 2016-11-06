package com.jameswolfeoliver.pigeon.Presenters;


import android.app.Service;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

import com.jameswolfeoliver.pigeon.R;
import com.jameswolfeoliver.pigeon.Server.TextServer;
import com.jameswolfeoliver.pigeon.Tasks.DownloadHtml;
import com.jameswolfeoliver.pigeon.Utilities.NetworkStateReceiver;
import com.jameswolfeoliver.pigeon.Utilities.PigeonApplication;

import java.io.IOException;

public class InboxPresenter implements NetworkStateReceiver.NetworkStateReceiverListener {
    private static final String LOG_TAG = InboxPresenter.class.getSimpleName();
    private static InboxPresenter Instance = null;
    private NetworkStateReceiver networkStateReceiver;
    private static TextServer server;

    private InboxPresenter() {
        // Discourage instances outside the singleton
    }

    public static InboxPresenter getInstance() {
        if (Instance != null) {
            return Instance;
        }
        return new InboxPresenter();
    }

    public void startServer(final Context context, final Callbacks onFinished) {
        final String loginPageUrl = context.getResources().getString(R.string.login_url);
        DownloadHtml.getHtml(context, loginPageUrl,
                new DownloadHtml.Callbacks() {
                    @Override
                    public void onResponse(boolean succeeded, String response) {
                        if (succeeded) {
                            try {
                                server = new TextServer();
                                server.start();
                                WifiManager wm = (WifiManager) context.getSystemService(Service.WIFI_SERVICE);
                                String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
                                String serverLoginUrl = String.format("http://%s:%s/login",ip,server.getListeningPort());
                                onFinished.onServerStarted(serverLoginUrl);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            server.setLoginResponse(response);
                        } else {
                            Log.d(LOG_TAG, String.format("Failed to  connect to %s. Volley Error: %s", loginPageUrl, response));
                        }
                    }
                });
    }

    public void tearDownServer() {
        if(server != null) {
            server.stop();
        }
    }

    public void startReceiver() {
        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        PigeonApplication.getAppContext().registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
    }

    public void stopReceiver(){
        if (networkStateReceiver != null) {
            networkStateReceiver.removeListener(this);
            PigeonApplication.getAppContext().unregisterReceiver(networkStateReceiver);
            networkStateReceiver = null;
        }
    }

    @Override
    public void onNetworkStateChange(boolean isAvailable) {
        if (!isAvailable && server != null && server.wasStarted()) {
            tearDownServer();
        }
        if (isAvailable && server != null && !server.wasStarted()) {
            startServer(PigeonApplication.getAppContext(), null);
        }
    }
    public interface Callbacks {
        public void onServerStarted(String serverLoginUrl);
    }
}
