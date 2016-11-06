package com.jameswolfeoliver.pigeon.Utilities;

import android.app.Application;
import android.content.Context;

public class PigeonApplication extends Application {
    private static PigeonApplication instance = null;
    private static Context context = null;

    public PigeonApplication() {
        super();
    }

    public synchronized static PigeonApplication getInstance() {
        if (instance == null) {
            instance = new PigeonApplication();
        }
        return instance;
    }

    public synchronized static Context getAppContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }
}