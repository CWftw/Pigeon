package com.jameswolfeoliver.pigeon.Server.Endpoints.Contacts;


import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.jameswolfeoliver.pigeon.Models.ClientResponses.Error;
import com.jameswolfeoliver.pigeon.R;
import com.jameswolfeoliver.pigeon.Server.Endpoint;
import com.jameswolfeoliver.pigeon.Server.Endpoints.Endpoints;
import com.jameswolfeoliver.pigeon.Server.PigeonServer;
import com.jameswolfeoliver.pigeon.Server.Sessions.SessionManager;
import com.jameswolfeoliver.pigeon.Utilities.PigeonApplication;
import com.jameswolfeoliver.pigeon.Utilities.Utils;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.regex.Matcher;

public class AvatarEndpoint extends Endpoint {
    private static final String AVATAR_URI_SUFFIX = "/photo";
    private static final String AVATAR_URI_PREFIX = "content://com.android.contacts/contacts/";
    private static final int AVATAR_COMPRESSION = 50;
    private static final int ID_DEFAULT = 0;
    private static final int ID_INVALID = -1;

    public AvatarEndpoint(SessionManager sessionManager) {
        super(sessionManager);
    }

    @Override
    protected Response onGet(IHTTPSession session) {
        int contactId = getContactId(session.getUri());
        InputStream inputStream;
        switch (contactId) {
            case ID_INVALID:
                return buildJsonError(Error.Codes.YOUR_FAULT, "Bad url", Status.BAD_REQUEST);
            case ID_DEFAULT:
                return Response.newChunkedResponse(Status.OK, MIME_PNG, getDefaultAvatarInputStream());
            default:
                inputStream = getAvatarInputStream(buildAvatarUri(contactId));
                if (inputStream == null) {
                    return redirectToDefaultAvatar(session.getUri(), contactId);
                }
                return Response.newChunkedResponse(Status.OK, MIME_JPEG, inputStream);
        }
    }

    @Override
    protected Response onPost(IHTTPSession session) {
        return buildJsonError(Error.Codes.YOUR_FAULT, "Method not allowed", Status.METHOD_NOT_ALLOWED);
    }

    private static int getContactId(String sessionUri) {
        Matcher contactIdMatcher = Endpoints.AVATAR_URI_PATTERN.matcher(sessionUri);
        if (contactIdMatcher.find()) {
            return  Integer.parseInt(contactIdMatcher.group(1));
        }
        return ID_INVALID;
    }

    private static Uri buildAvatarUri(int contactId) {
        return Uri.parse(AVATAR_URI_PREFIX.concat(String.valueOf(contactId)).concat(AVATAR_URI_SUFFIX));
    }

    private static Response redirectToDefaultAvatar(String sessionUri, int contactId) {
        String contactIdPattern = String.valueOf(contactId);
        String defaultId = String.valueOf(ID_DEFAULT);
        Response redirect = Response.newFixedLengthResponse(Status.REDIRECT, MIME_PNG, new byte[0]);
        redirect.addHeader("Location", sessionUri.replace(contactIdPattern, defaultId));
        return redirect;
    }

    private static InputStream getAvatarInputStream(Uri avatarUri) {
        InputStream inputStream = null;
        try {
            inputStream = PigeonApplication.getAppContext().getContentResolver().openInputStream(avatarUri);
        } catch (FileNotFoundException e) {
            Log.e(AvatarEndpoint.class.getSimpleName(), "Failed to get contact photo", e);
        }
        return inputStream;
    }

    private static InputStream getDefaultAvatarInputStream() {
        InputStream inputStream;
        Bitmap bitmap = Utils.getBitmapFromVectorDrawable(PigeonApplication.getAppContext(), R.drawable.ic_friend);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, AVATAR_COMPRESSION, stream);
        inputStream = new ByteArrayInputStream(stream.toByteArray());
        return inputStream;
    }
}
