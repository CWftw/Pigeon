package com.jameswolfeoliver.pigeon.Server.Rest;


import android.util.Log;

import com.jameswolfeoliver.pigeon.Server.Models.Requests.VersionResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RestServer {
    private static final String LOG_TAG = RestServer.class.getSimpleName();

    public static final String INSECURE_LOGIN_PATH = "login/login_insecure_v";
    public static final String SECURE_LOGIN_PATH = "login/login_secure_v";
    public static final String INBOX_PATH = "inbox/inbox_v";
    public static final String ERROR_PATH = "error/error_general_v";

    public void getVersionInfo(final RestCallback<VersionResponse> callback) {
        RestClient.getInstance().getRestService().getLoginVersions().enqueue(new Callback<VersionResponse>() {
            @Override
            public void onResponse(Call<VersionResponse> call, Response<VersionResponse> response) {
                if (response.isSuccessful() && response.body() != null ) {
                    callback.onResult(response.body());
                } else {
                    Log.d(LOG_TAG, String.format("Failed to GET %s. Error: %d", call.request().url().toString(), response.code()));
                    callback.onResult(null);
                }
            }

            @Override
            public void onFailure(Call<VersionResponse> call, Throwable t) {
                Log.d(LOG_TAG, String.format("Failed to GET %s. Error: %s", call.request().url().toString(), t.getLocalizedMessage()));
                t.printStackTrace();
                callback.onResult(null);
            }
        });
    }

    public void getPage(final String pagePath, final int version, final RestCallback<String> callback) {
        RestClient.getInstance().getStringResponseRestService().getPage(pagePath + Integer.toString(version)).enqueue(
                new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            callback.onResult(response.body());
                        } else {
                            Log.d(LOG_TAG, String.format("Failed to GET %s. Error: %d", call.request().url().toString(), response.code()));
                            callback.onResult(null);
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Log.d(LOG_TAG, String.format("Failed to GET %s. Error: %s", call.request().url().toString(), t.getLocalizedMessage()));
                        t.printStackTrace();
                        callback.onResult(null);
                    }
                }
        );
    }

    public void updateSecureLoginPage(final int version, final RestCallback<Void> callback) {

    }

    public void updateInboxPage(final int version, final RestCallback<Void> callback) {

    }

    public interface RestCallback<E> {
        void onResult(E e);
    }
}
