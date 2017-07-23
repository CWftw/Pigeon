package com.jameswolfeoliver.pigeon.Server.Endpoints.Contacts;

import com.jameswolfeoliver.pigeon.Managers.ContactCacheManager;
import com.jameswolfeoliver.pigeon.Models.ClientResponses.Error;
import com.jameswolfeoliver.pigeon.Models.Contact;
import com.jameswolfeoliver.pigeon.Server.Endpoint;
import com.jameswolfeoliver.pigeon.Server.Endpoints.Endpoints;
import com.jameswolfeoliver.pigeon.Server.PigeonServer;
import com.jameswolfeoliver.pigeon.Server.Sessions.SessionManager;
import com.jameswolfeoliver.pigeon.SqlWrappers.ContactsWrapper;
import com.jameswolfeoliver.pigeon.Utilities.PigeonApplication;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class ContactsEndpoint extends Endpoint {

    public ContactsEndpoint(SessionManager sessionManager) {
        super(sessionManager);
    }

    @Override
    protected Response onGet(IHTTPSession session) {
        final Matcher idMatcher = Endpoints.CONTACT_URI_PATTERN.matcher(session.getUri());
        final Map<String, List<String>> params = session.getParameters();
        if (idMatcher.find()) {
            return buildJsonResponse(onGetByIds(Collections.singletonList(Integer.parseInt(idMatcher.group(1)))), Status.OK);
        } else if (params != null && params.containsKey("id")) {
            final List<Integer> contactIds = new ArrayList<>();
            for (String id : params.get("id")) {
                try {
                    contactIds.add(Integer.parseInt(id));
                } catch (NumberFormatException e) {
                    return buildJsonError(Error.Codes.YOUR_FAULT, "Bad query", Status.BAD_REQUEST);
                }
            }
            return buildJsonResponse(onGetByIds(contactIds), Status.OK);
        } else if (params != null && params.containsKey("phoneNumber")) {
            final List<Long> contactNumbers = new ArrayList<>();
            for (String id : params.get("phoneNumber")) {
                try {
                    contactNumbers.add(Long.parseLong(id));
                } catch (NumberFormatException e) {
                    return buildJsonError(Error.Codes.YOUR_FAULT, "Bad query", Status.BAD_REQUEST);
                }
            }
            return buildJsonResponse(onGetNumbers(contactNumbers), Status.OK);
        } else if (params == null || params.isEmpty()) {
            return buildJsonResponse(PigeonApplication.getGson().toJson(ContactCacheManager
                    .getInstance().updateNow().getContacts()), Status.OK);
        }
        return buildJsonError(Error.Codes.YOUR_FAULT, "Unrecognized", Status.BAD_REQUEST);
    }

    private String onGetByIds(final List<Integer> contactIds) {
        final ContactsWrapper contactsWrapper = new ContactsWrapper();
        final Iterable<Contact> conversations = contactsWrapper.find(() -> contactsWrapper.selectByIds(contactIds))
                .blockingIterable();
        final List<Contact> contactList = new ArrayList<>();
        final Iterator<Contact> conversationIterator = conversations.iterator();
        while (conversationIterator.hasNext()) contactList.add(conversationIterator.next());
        return PigeonApplication.getGson().toJson(contactList);
    }

    private String onGetNumbers(final List<Long> phoneNumbers) {
        return PigeonApplication.getGson().toJson(ContactCacheManager.getInstance().updateNow().getContacts(phoneNumbers));
    }

    @Override
    protected Response onPost(IHTTPSession session) {
        // TODO implement create contact
        return buildJsonError(Error.Codes.MY_FAULT, "Not implemented", Status.NOT_IMPLEMENTED);
    }
}
