package com.jameswolfeoliver.pigeon.Utilities;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jameswolfeoliver.pigeon.Server.Rest.RestServer;

public class PigeonApplication extends Application implements Application.ActivityLifecycleCallbacks {
    private static final String SHARED_PREF = "pigeon_application_shared_pref";
    private static PigeonApplication instance = null;
    private static Context context = null;
    private static Gson gson = null;
    private static RestServer restServer = null;
    private static int started;
    private static int stopped;

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
        Fresco.initialize(this);
        registerActivityLifecycleCallbacks(this);
    }

    public static SharedPreferences getSharedPreferences(){
        return context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
    }

    public static Gson getGson() {
        if (gson == null) {
            gson = new GsonBuilder().create();
        }
        return gson;
    }

    public static RestServer getRestServer() {
        if (restServer == null) {
            restServer = new RestServer();
        }
        return restServer;
    }

    public static boolean isApplicationVisible() {
        return started > stopped;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

    @Override
    public void onActivityStarted(Activity activity) {
        ++started;
    }

    @Override
    public void onActivityResumed(Activity activity) {}

    @Override
    public void onActivityPaused(Activity activity) {}

    @Override
    public void onActivityStopped(Activity activity) {
        ++stopped;
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

    @Override
    public void onActivityDestroyed(Activity activity) {}
}