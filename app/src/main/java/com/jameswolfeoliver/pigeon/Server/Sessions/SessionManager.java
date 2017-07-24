package com.jameswolfeoliver.pigeon.Server.Sessions;


import android.util.Log;

import com.jameswolfeoliver.pigeon.Utilities.PigeonApplication;

import org.nanohttpd.protocols.http.IHTTPSession;

import java.security.SecureRandom;

import io.reactivex.annotations.NonNull;
import io.realm.Realm;
import io.realm.RealmObject;

public class SessionManager {
    private static final String COOKIE_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int COOKIE_LENGTH = 32;
    private final SecureRandom secureRandom;

    public SessionManager() {
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

    private Session generateCookie(@NonNull String cookieName) {
        Session session = new Session(cookieName, generateCookieValue());
        long issueTimestamp = System.currentTimeMillis();
        long expiryTimestamp = issueTimestamp + (20L*24L*60L*60L*1000L);
        session.setIssueTimestamp(System.currentTimeMillis());
        session.setExpiryTimestamp(expiryTimestamp);
        session.setSessionValid(true);
        return session;
    }

    public Session createNewSession(@NonNull String cookieName) {
        Realm realm = Realm.getDefaultInstance();
        Session session = generateCookie(cookieName);
        Log.d("James", session.toString());
        realm.beginTransaction();
        Session realmSession = realm.copyToRealm(session);
        Log.d("James", realmSession.toString());
        realm.commitTransaction();
        realm.close();
        return session;
    }

    public boolean validateSession(Session session) {
        Log.d("James", session.toString());
        Realm realm = Realm.getDefaultInstance();
        Session realmSession = realm.where(Session.class).equalTo("id", session.getId()).findFirst();
        if (realmSession != null) {
            Log.d("James", realmSession.toString());
        }
        boolean isValid = realmSession != null && realmSession.isSessionValid();
        realm.close();
        return isValid;
    }

    public void invalidateSession(IHTTPSession session) {
        // todo create cookie
        // todo delete cookie from realm
    }
}
