package com.jameswolfeoliver.pigeon.Server.Endpoints;


import java.util.regex.Pattern;

public class Endpoints {
    public final static Pattern LOGIN_URI_PATTERN = Pattern.compile("/login");
    public final static Pattern INBOX_URI_PATTERN = Pattern.compile("/inbox");
    public final static Pattern MESSAGES_URI_PATTERN = Pattern.compile("/messages");
    public final static Pattern CONTACTS_URI_PATTERN = Pattern.compile("\\/contacts\\/(\\d\\d\\d)");
    public final static Pattern AVATAR_URI_PATTERN = Pattern.compile("\\/contacts\\/(\\d\\d\\d)\\/avatar");

    public final static int LOGIN_ENDPOINT = 0;
    public final static int INBOX_ENDPOINT = 1;
    public final static int CONTACTS_ENDPOINT = 2;
    public final static int AVATAR_ENDPOINT = 3;
    public final static int MESSAGES_ENDPOINT = 4;


    public static int getEndpoint(String uri) {
        if (LOGIN_URI_PATTERN.matcher(uri).find()) {
            return LOGIN_ENDPOINT;
        } else if (INBOX_URI_PATTERN.matcher(uri).find()) {
            return INBOX_ENDPOINT;
        } else if (AVATAR_URI_PATTERN.matcher(uri).find()) {
            return AVATAR_ENDPOINT;
        } else if (CONTACTS_URI_PATTERN.matcher(uri).find()) {
            return CONTACTS_ENDPOINT;
        } else if (MESSAGES_URI_PATTERN.matcher(uri).find()) {
            return MESSAGES_ENDPOINT;
        } else {
            return -1;
        }
    }
}
