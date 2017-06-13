package com.jameswolfeoliver.pigeon.Server.Endpoints.Login;

import com.jameswolfeoliver.pigeon.Managers.SecurityHelper;
import com.jameswolfeoliver.pigeon.Server.Endpoint;
import com.jameswolfeoliver.pigeon.Models.ClientRequests.LoginRequest;
import com.jameswolfeoliver.pigeon.Models.ClientResponses.LoginResponse;
import com.jameswolfeoliver.pigeon.Server.TextServer;
import com.jameswolfeoliver.pigeon.Utilities.PigeonApplication;
import com.jaredrummler.android.device.DeviceName;

import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class SecureLoginEndpoint extends Endpoint {
    public static final String LOG_TAG = SecureLoginEndpoint.class.getSimpleName();

    // Login errors
    private final static int NO_ERROR = 0;
    private final static int INVALID_CREDENTIALS = 1;
    private final static int TERMS_UNCHECKED = 2;

    // Login Vars
    private static final String DEVICE_NAME = DeviceName.getDeviceName();

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


        return buildJsonResponse(buildLogInResponse(
                PigeonApplication.getGson().fromJson(session.getQueryParameterString(),
                        LoginRequest.class)),
                NanoHTTPD.Response.Status.OK);
    }

    private static NanoHTTPD.Response onGet() {
        return buildHtmlResponse(TextServer.getLoginSecure(), NanoHTTPD.Response.Status.OK);
    }

    private static String buildLogInResponse(LoginRequest request)  {
        LoginResponse response = new LoginResponse(false, NO_ERROR);

        if (!request.hasUserAgreed()) {
            response.setErrorCode(TERMS_UNCHECKED);
        } else if (!request.getDeviceName().equals(DEVICE_NAME)) {
            response.setErrorCode(INVALID_CREDENTIALS);
        } else if (!SecurityHelper.getInstance().checkUserPassword(request.getPassword())) {
            response.setErrorCode(INVALID_CREDENTIALS);
        } else {
            response.setWasSuccessful(true);
        }

        return PigeonApplication.getGson().toJson(response, LoginResponse.class);
    }
}
