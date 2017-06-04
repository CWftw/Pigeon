package com.jameswolfeoliver.pigeon.Server.Endpoints.Contacts;


import android.net.Uri;

import com.jameswolfeoliver.pigeon.Server.Endpoint;
import com.jameswolfeoliver.pigeon.Server.Endpoints.Endpoints;
import com.jameswolfeoliver.pigeon.Server.TextServer;
import com.jameswolfeoliver.pigeon.Utilities.PigeonApplication;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.regex.Matcher;

import fi.iki.elonen.NanoHTTPD;

public class AvatarEndpoint extends Endpoint {
    private static final String AVATAR_URI_SUFFIX = "/photo";
    private static final String AVATAR_URI_PREFIX = "content://com.android.contacts/contacts/";

    public static NanoHTTPD.Response serve(NanoHTTPD.IHTTPSession session) {
        switch (session.getMethod()) {
            case GET:
                return onGet(session);
            default:
                return buildHtmlResponse(TextServer.getBadRequest(), NanoHTTPD.Response.Status.BAD_REQUEST);
        }
    }

    //content://com.android.contacts/contacts/113/photo
    private static NanoHTTPD.Response onGet(NanoHTTPD.IHTTPSession session) {
        String avatarUri = buildAvartarUri(session.getUri());
        if (avatarUri != null) {
            InputStream inputStream = getAvatarInputStream(avatarUri);
            if (inputStream != null) {
                return NanoHTTPD.newChunkedResponse(NanoHTTPD.Response.Status.OK, MIME_JPEG, inputStream);
            }
        }
        return buildHtmlResponse(TextServer.getNotFound(), NanoHTTPD.Response.Status.NOT_FOUND);
    }

    private static String buildAvartarUri(String sessionUri) {
        StringBuilder avatarUriBuilder = new StringBuilder();
        avatarUriBuilder.append(AVATAR_URI_PREFIX);
        Matcher contactIdMatcher = Endpoints.AVATAR_URI_PATTERN.matcher(sessionUri);
        if (contactIdMatcher.find()) {
            avatarUriBuilder.append(contactIdMatcher.group(1));
            avatarUriBuilder.append(AVATAR_URI_SUFFIX);
            return avatarUriBuilder.toString();
        }
        return null;
    }

    private static InputStream getAvatarInputStream(String avatarUri) {
        InputStream inputStream = null;
        try {
            inputStream = PigeonApplication.getAppContext().getContentResolver().openInputStream(Uri.parse(avatarUri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return inputStream;
    }
}
