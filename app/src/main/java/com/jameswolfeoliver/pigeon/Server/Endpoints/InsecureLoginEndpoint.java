package com.jameswolfeoliver.pigeon.Server.Endpoints;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import com.jameswolfeoliver.pigeon.Activities.InboxActivity;
import com.jameswolfeoliver.pigeon.R;
import com.jameswolfeoliver.pigeon.Server.Endpoint;
import com.jameswolfeoliver.pigeon.Server.TextServer;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import fi.iki.elonen.NanoHTTPD;

public class InsecureLoginEndpoint extends Endpoint{
    public static final String LOG_TAG = InsecureLoginEndpoint.class.getSimpleName();

    private static final int USER_WAIT_IN_MILLIS = 1000;
    private static AtomicBoolean userAnsweredPrompt = new AtomicBoolean(false);
    private static AtomicBoolean accessDenied = new AtomicBoolean(true);

    // Login errors
    private final static int NO_ERROR = 0;
    private final static int DENIED = 1;
    private final static int TERMS_UNCHECKED = 2;
    private final static int NO_USER_RESPONSE = 3;

    public static NanoHTTPD.Response serve(NanoHTTPD.IHTTPSession session) {
        switch (session.getMethod()) {
            case GET:
                return onGet();
            case POST:
                return onPost(session);
            default:
                return buildHtmlResponse(TextServer.getBadRequest(), NanoHTTPD.Response.Status.BAD_REQUEST);
        }
    }

    private static NanoHTTPD.Response onPost(NanoHTTPD.IHTTPSession session) {
        Map<String, String> bodyMap = new HashMap<>();
        try {
            session.parseBody(bodyMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        bodyMap = session.getParms();
        return buildJsonResponse(buildLogInResponse(bodyMap, session.getRemoteHostName(), session.getRemoteIpAddress()), NanoHTTPD.Response.Status.OK);
    }

    private static NanoHTTPD.Response onGet() {
        return buildHtmlResponse(TextServer.getLoginInsecure(), NanoHTTPD.Response.Status.OK);
    }

    private static String buildLogInResponse(Map<String, String> bodyMap, String name, String ip)  {
        int error = NO_ERROR;
        final boolean termsChecked = bodyMap.get("hasAgreedToTerms").equals("true");
        JSONObject responseJson = new JSONObject();

        Log.d(LOG_TAG, String.format("On Thread: %s", Thread.currentThread().getName()));
        Log.d(LOG_TAG, String.format("checkBox: %b", termsChecked));

        try {
            responseJson.put("successful", false);
            if (!termsChecked) {
                error = TERMS_UNCHECKED;
            } else {
                promptUserForClientAccess(name, ip);
                while (!userAnsweredPrompt.get()) {
                    try {
                        Thread.sleep(USER_WAIT_IN_MILLIS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (accessDenied.get()) {
                    error = DENIED;
                } else {
                    responseJson.put("successful", true);
                }
            }
            responseJson.put("error", error);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(LOG_TAG, responseJson.toString());
        return responseJson.toString();
    }

    private static void promptUserForClientAccess(final String clientName, final String clientIp) {
        TextServer.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder promptUser = new AlertDialog.Builder(InboxActivity.context);
                promptUser.setTitle("Allow PC Connection?")
                        .setIcon(R.drawable.app_icon)
                        .setMessage(String.format("%s (IP: %s) would like to connect. Trust this computer?", clientName, clientIp))
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                userAnsweredPrompt.compareAndSet(false, true);
                                accessDenied.compareAndSet(true, false);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                userAnsweredPrompt.compareAndSet(false, true);
                                accessDenied.compareAndSet(true, true);
                            }
                        });
                promptUser.show();
            }
        });
    }


}
