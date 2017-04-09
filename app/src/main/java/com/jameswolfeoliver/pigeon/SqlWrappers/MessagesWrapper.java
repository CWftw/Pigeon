package com.jameswolfeoliver.pigeon.SqlWrappers;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;

import com.jameswolfeoliver.pigeon.Server.Models.Conversation;
import com.jameswolfeoliver.pigeon.Server.Models.Message;
import com.jameswolfeoliver.pigeon.Utilities.PigeonApplication;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class MessagesWrapper implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int ADDRESS_INDEX = 0;
    private static final int PERSON_INDEX = 1;
    private static final int DATE_INDEX = 2;
    private static final int THREAD_ID_INDEX = 3;
    private static final int SNIPPET_INDEX = 4;
    private static final int TYPE_INDEX = 5;
    private static final int READ_INDEX = 6;
    private static final int STATUS_INDEX = 7;
    private static final String LESS_THAN = " < ";
    private static final String AND = " AND ";
    private static final String EQUALS = " = ";
    private static final String SORT_ORDER = "date desc limit 25";
    private static final String SELECTION_BUNDLE_KEY = "selection";
    private final Uri MESSAGES_CONTENT_URI = Uri.parse("content://mms-sms/complete-conversations");
    private static final String[] PROJECTION = new String[] {
            "address",
            "person",
            "date",
            "thread_id",
            "body",
            "type",
            "read",
            "status"};

    private SparseArray<SqlCallback<Message>> listeners;

    public MessagesWrapper() {
        this.listeners = new SparseArray<>();
    }

    private String getSelection(String lastReceivedDate, String threadId) {
        if (lastReceivedDate != null) {
            return "date" + LESS_THAN + lastReceivedDate + AND + "thread_id" + EQUALS + threadId;
        } else {
            return "thread_id" + EQUALS + threadId;
        }
    }

    public void unregisterCallback(int callerId) {
        if (listeners != null
                && listeners.size() != 0
                && listeners.indexOfKey(callerId) > 0) {
            listeners.remove(callerId);
        }
    }

    /**
     *
     * @param lastReceivedDate null if initial call
     * @param threadId conversation thread
     * @param callerId identity of caller
     * @param activity activity with available loader manager
     * @param conversationCallback a callback for after messages are loaded
     */
    public void getPaginatedMessages(String lastReceivedDate, String threadId, int callerId,
                                   WeakReference<Activity> activity, SqlCallback<Message> conversationCallback) {
        this.listeners.put(callerId, conversationCallback);
        Bundle bundle = new Bundle();
        bundle.putString(SELECTION_BUNDLE_KEY, getSelection(lastReceivedDate, threadId));
        activity.get().getLoaderManager().initLoader(CursorIds.MESSAGES_WRAPPER_ID, bundle, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String selection = bundle.getString(SELECTION_BUNDLE_KEY);

        return new CursorLoader(
                PigeonApplication.getAppContext(),
                MESSAGES_CONTENT_URI,
                PROJECTION,
                selection,
                null,
                SORT_ORDER
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (listeners != null && listeners.size() != 0) {
            ArrayList<Message> messages = new ArrayList<>();
            while (cursor.moveToNext()) {
                Message.Builder builder = new Message.Builder(cursor.getInt(THREAD_ID_INDEX))
                        .setAddress(cursor.getLong(ADDRESS_INDEX))
                        .setDate(cursor.getLong(DATE_INDEX))
                        .setPerson(cursor.getInt(PERSON_INDEX))
                        .setType(cursor.getInt(TYPE_INDEX))
                        .setBody(cursor.getString(SNIPPET_INDEX))
                        .setStatus(cursor.getInt(STATUS_INDEX))
                        .setRead(cursor.getInt(READ_INDEX));
                messages.add(builder.build());
            }
            for (int i = 0; i <listeners.size(); i++) {
                SqlCallback<Message> listener = listeners.valueAt(i);
                listener.onQueryComplete(messages);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
