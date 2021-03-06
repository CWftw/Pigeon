package com.jameswolfeoliver.pigeon.Utilities;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.multidex.MultiDexApplication;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jameswolfeoliver.pigeon.Server.Rest.RestServer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PigeonApplication extends MultiDexApplication implements Application.ActivityLifecycleCallbacks {
    private static final String SHARED_PREF = "pigeon_application_shared_pref";
    private static PigeonApplication instance = null;
    private static Context context = null;
    private static Gson gson = null;
    private static RestServer restServer = null;
    private static int started;
    private static int stopped;
    private ExecutorService helperThread;

    public PigeonApplication() {
        super();
    }

    public synchronized static PigeonApplication getInstance() {
        return instance;
    }

    public synchronized static Context getAppContext() {
        return context;
    }

    public static SharedPreferences getSharedPreferences() {
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

    public ExecutorService getHelperThread() {
        return helperThread;
    }

    public static boolean isDefaultSmsApp() {
        return Telephony.Sms.getDefaultSmsPackage(getAppContext()).equals(getAppContext().getPackageName());
    }

    public static void promptUserToChangeDefaultSmsApp() {
        Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getAppContext().getPackageName());
        getAppContext().startActivity(intent);
    }

    public static boolean isApplicationVisible() {
        return started > stopped;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        instance = this;
        helperThread = Executors.newSingleThreadExecutor();
        Fresco.initialize(this);
        registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
        ++started;
    }

    @Override
    public void onActivityResumed(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
        ++stopped;
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }
}