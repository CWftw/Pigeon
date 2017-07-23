package com.jameswolfeoliver.pigeon.Server.Interceptor;

import android.util.Log;

import com.jameswolfeoliver.pigeon.Server.Sessions.Cookie;
import com.jameswolfeoliver.pigeon.Server.Sessions.SessionManager;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.content.CookieHandler;
import org.nanohttpd.protocols.http.response.Response;

public class CookieInterceptor implements Interceptor<IHTTPSession> {
    private static final String LOG_TAG = CookieInterceptor.class.getSimpleName();
    private static final String COOKIE_PIGEON_ID = "pigeonId";
    private static final int COOKIE_MAX_LIFE_IN_DAYS = 20;
    private SessionManager sessionManager;

    public CookieInterceptor(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void intercept(IHTTPSession session) {
        CookieHandler cookieHandler = session.getCookies();
        String pigeonId = cookieHandler.read(COOKIE_PIGEON_ID);
        if (pigeonId == null || pigeonId.isEmpty()) {
            Cookie cookie = sessionManager.createNewSession(COOKIE_PIGEON_ID);
            cookieHandler.set(cookie.getName(), cookie.getValue(), COOKIE_MAX_LIFE_IN_DAYS);
        } else {
            Log.d(LOG_TAG, pigeonId);
        }
    }
}
