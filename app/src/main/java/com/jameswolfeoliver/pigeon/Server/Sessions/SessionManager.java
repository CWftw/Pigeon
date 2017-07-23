package com.jameswolfeoliver.pigeon.Server.Sessions;


import com.jameswolfeoliver.pigeon.Utilities.PigeonApplication;

import org.nanohttpd.protocols.http.IHTTPSession;

import java.security.SecureRandom;

import io.reactivex.annotations.NonNull;
import io.realm.Realm;

public class SessionManager {
    private static final String COOKIE_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int COOKIE_LENGTH = 32;
    private final SecureRandom secureRandom;
    private final Realm realm;

    public SessionManager() {
        this.realm = Realm.getDefaultInstance();
        this.secureRandom = PigeonApplication.getSecureRandom();

    }

    private String generateCookieValue() {
        StringBuilder sb;
        synchronized (secureRandom) {
            sb = new StringBuilder(COOKIE_LENGTH);
            for (int i = 0; i < COOKIE_LENGTH; i++)
                sb.append(COOKIE_CHARS.charAt(secureRandom.nextInt(COOKIE_CHARS.length())));
        }
        return sb.toString();
    }

    private Cookie generateCookie(@NonNull String cookieName) {
        Cookie cookie = new Cookie(cookieName, generateCookieValue());
        long issueTimestamp = System.currentTimeMillis();
        long expiryTimestamp = issueTimestamp + (20L*24L*60L*60L*1000L);
        cookie.setIssueTimestamp(System.currentTimeMillis());
        cookie.setExpiryTimestamp(expiryTimestamp);
        return cookie;
    }

    public Cookie createNewSession(@NonNull String cookieName) {
        Cookie cookie = generateCookie(cookieName);
        synchronized (realm) {
            realm.beginTransaction();
            realm.copyToRealm(cookie);
            realm.commitTransaction();
        }
        return cookie;
    }

    public boolean isSessionValid(IHTTPSession session) {
        return true;
    }
}
