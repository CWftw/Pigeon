package com.jameswolfeoliver.pigeon.Utilities;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class PigeonApplication extends Application {
    private static final String SHARED_PREF = "pigeon_application_shared_pref";
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

    public static SharedPreferences getSharedPreferences(){
        return context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
    }
}