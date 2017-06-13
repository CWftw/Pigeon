package com.jameswolfeoliver.pigeon.Receivers;

import android.content.BroadcastReceiver;

public abstract class SmsBroadcastReceiver extends BroadcastReceiver {
    protected int numberOfMessageParts;

    public SmsBroadcastReceiver(int numberOfMessageParts) {
        this.numberOfMessageParts = numberOfMessageParts;
    }
}
