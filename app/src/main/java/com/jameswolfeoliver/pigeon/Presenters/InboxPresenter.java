package com.jameswolfeoliver.pigeon.Presenters;


import android.content.IntentFilter;

import com.jameswolfeoliver.pigeon.Server.TextServer;
import com.jameswolfeoliver.pigeon.Utilities.NetworkStateReceiver;
import com.jameswolfeoliver.pigeon.Utilities.PigeonApplication;

public class InboxPresenter implements NetworkStateReceiver.NetworkStateReceiverListener {
    private static final String LOG_TAG = InboxPresenter.class.getSimpleName();
    private NetworkStateReceiver networkStateReceiver;
    public InboxPresenter() {

    }

    public void startServer(TextServer.ServerCallback callback) {
        TextServer.getInstance().start(false, callback);
    }

    public void tearDownServer() {
        TextServer.getInstance().stop();
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
        /*if (!isAvailable && server != null && server.wasStarted()) {
            tearDownServer();
        }
        if (isAvailable && server != null && !server.wasStarted()) {
            startServer(PigeonApplication.getAppContext(), null);
        }*/
    }
    public interface Callbacks {
        public void onServerStarted(String serverLoginUrl);
    }
}