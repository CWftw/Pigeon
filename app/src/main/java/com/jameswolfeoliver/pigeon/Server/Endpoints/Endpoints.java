package com.jameswolfeoliver.pigeon.Server.Endpoints;


import com.jameswolfeoliver.pigeon.Server.Endpoint;
import com.jameswolfeoliver.pigeon.Server.Endpoints.Assets.CssEndpoint;
import com.jameswolfeoliver.pigeon.Server.Endpoints.Assets.JsEndpoint;
import com.jameswolfeoliver.pigeon.Server.Endpoints.Contacts.AvatarEndpoint;
import com.jameswolfeoliver.pigeon.Server.Endpoints.Contacts.ContactsEndpoint;
import com.jameswolfeoliver.pigeon.Server.Endpoints.Conversations.ConversationsEndpoint;
import com.jameswolfeoliver.pigeon.Server.Endpoints.Conversations.InboxEndpoint;
import com.jameswolfeoliver.pigeon.Server.Endpoints.Conversations.MessagesEndpoint;
import com.jameswolfeoliver.pigeon.Server.Endpoints.Login.InsecureLoginEndpoint;
import com.jameswolfeoliver.pigeon.Server.Endpoints.Login.SecureLoginEndpoint;
import com.jameswolfeoliver.pigeon.Server.Sessions.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Endpoints {
    public final static Pattern LOGIN_URI_PATTERN = Pattern.compile("/login");
    public final static Pattern INBOX_URI_PATTERN = Pattern.compile("/inbox");
    public final static Pattern JS_URI_PATTERN = Pattern.compile(".*/js/");
    public final static Pattern CSS_URI_PATTERN = Pattern.compile(".*/css/");
    public final static Pattern MESSAGES_URI_PATTERN = Pattern.compile("/messages");
    public final static Pattern CONVERSATIONS_URI_PATTERN = Pattern.compile("/conversations");
    public final static Pattern CONTACTS_URI_PATTERN = Pattern.compile("\\/contacts");
    public final static Pattern CONTACT_URI_PATTERN = Pattern.compile("\\/contacts\\/([0-9]{1,4})");
    public final static Pattern AVATAR_URI_PATTERN = Pattern.compile("\\/contacts\\/([0-9]{1,4})\\/avatar");

    public final static int LOGIN_ENDPOINT = 0;
    public final static int INBOX_ENDPOINT = 1;
    public final static int CONTACTS_ENDPOINT = 2;
    public final static int AVATAR_ENDPOINT = 3;
    public final static int MESSAGES_ENDPOINT = 4;
    public final static int CONVERSATIONS_ENDPOINT = 5;
    public final static int JS_ENDPOINT = 6;
    public final static int CSS_ENDPOINT = 7;
    
    private final List<Endpoint> endpoints;
    
    public Endpoints(boolean isSecure, SessionManager sessionManager) {
        endpoints = new ArrayList<>();
        endpoints.add(LOGIN_ENDPOINT, isSecure
                ? new SecureLoginEndpoint(sessionManager) : new InsecureLoginEndpoint(sessionManager));
        endpoints.add(INBOX_ENDPOINT, new InboxEndpoint(sessionManager));
        endpoints.add(CONTACTS_ENDPOINT, new ContactsEndpoint(sessionManager));
        endpoints.add(AVATAR_ENDPOINT, new AvatarEndpoint(sessionManager));
        endpoints.add(MESSAGES_ENDPOINT, new MessagesEndpoint(sessionManager));
        endpoints.add(CONVERSATIONS_ENDPOINT, new ConversationsEndpoint(sessionManager));
        endpoints.add(JS_ENDPOINT, new JsEndpoint(sessionManager));
        endpoints.add(CSS_ENDPOINT, new CssEndpoint(sessionManager));
    }

    public Endpoint getEndpoint(String uri) {
        if (LOGIN_URI_PATTERN.matcher(uri).find()) {
            return endpoints.get(LOGIN_ENDPOINT);
        } else if (INBOX_URI_PATTERN.matcher(uri).find()) {
            return endpoints.get(INBOX_ENDPOINT);
        } else if (AVATAR_URI_PATTERN.matcher(uri).find()) {
            return endpoints.get(AVATAR_ENDPOINT);
        } else if (CONTACTS_URI_PATTERN.matcher(uri).find()
                || CONTACT_URI_PATTERN.matcher(uri).find()) {
            return endpoints.get(CONTACTS_ENDPOINT);
        } else if (MESSAGES_URI_PATTERN.matcher(uri).find()) {
            return endpoints.get(MESSAGES_ENDPOINT);
        } else if (CONVERSATIONS_URI_PATTERN.matcher(uri).find()) {
            return endpoints.get(CONVERSATIONS_ENDPOINT);
        } else if (JS_URI_PATTERN.matcher(uri).find()) {
                return endpoints.get(JS_ENDPOINT);
        } else if (CSS_URI_PATTERN.matcher(uri).find()) {
            return endpoints.get(CSS_ENDPOINT);
        } else {
            return null;
        }
    }
}
