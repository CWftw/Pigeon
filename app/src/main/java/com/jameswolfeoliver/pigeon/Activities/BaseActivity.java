package com.jameswolfeoliver.pigeon.Activities;

import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.jameswolfeoliver.pigeon.Presenters.BasePresenter;
import com.jameswolfeoliver.pigeon.R;
import com.jameswolfeoliver.pigeon.Utilities.NetworkStateReceiver;
import com.jameswolfeoliver.pigeon.Utilities.PigeonApplication;

public abstract class BaseActivity extends AppCompatActivity implements NetworkStateReceiver.NetworkStateReceiverListener {
    protected BasePresenter basePresenter;
    private NetworkStateReceiver networkStateReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        basePresenter = new BasePresenter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopReceiver();
    }

    @Override
    abstract public void onNetworkStateChange(boolean isAvailable);

    protected void showConnectionSuccess(String loginUrl) {
        Snackbar.make(getWindow().getDecorView().getRootView(), String.format(getString(R.string.connected_message), loginUrl), Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        basePresenter.tearDownServer(null);
                    }
                })
                .show();
    }

    public void startReceiver() {
        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        PigeonApplication.getAppContext().registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
    }

    public void stopReceiver() {
        if (networkStateReceiver != null) {
            networkStateReceiver.removeListener(this);
            PigeonApplication.getAppContext().unregisterReceiver(networkStateReceiver);
            networkStateReceiver = null;
        }
    }
}
