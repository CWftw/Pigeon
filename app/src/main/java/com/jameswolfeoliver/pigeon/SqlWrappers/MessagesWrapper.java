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

import Models.Message;
import com.jameswolfeoliver.pigeon.Utilities.PigeonApplication;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class MessagesWrapper implements Wrapper<Message>, LoaderManager.LoaderCallbacks<Cursor> {

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
    private WeakReference<Activity> activity;
    private long lastReceivedDate = -1L;
    private String threadId;

    public MessagesWrapper(Activity activity, String threadId) {
        super();
        this.activity = new WeakReference<>(activity);
        this.listeners = new SparseArray<>();
        this.threadId = threadId;
    }


    @Override
    public void get(int callerId, SqlCallback<Message> messageCallback, String... args) {
        getPaginatedMessages(callerId, messageCallback, lastReceivedDate);
    }

    @Override
    public void find(int callerId, SqlCallback<Message> messageCallback, String... args) {
        getPaginatedMessages(callerId, messageCallback, Long.parseLong(args[0]));
    }

    private void getPaginatedMessages(int callerId,
                                     SqlCallback<Message> messageCallback, long specifiedDate) {
        if (activity == null || activity.get() == null) {
            throw new IllegalStateException("Cannot user LoaderManager without activity reference");
        }
        this.listeners.put(callerId, messageCallback);
        Bundle bundle = new Bundle();
        bundle.putString(SELECTION_BUNDLE_KEY, getSelection(specifiedDate, threadId));
        if (activity.get().getLoaderManager()
                .getLoader(CursorIds.MESSAGES_WRAPPER_ID) != null) {
            activity.get().getLoaderManager().restartLoader(CursorIds.MESSAGES_WRAPPER_ID, bundle, this);
        } else {
            activity.get().getLoaderManager().initLoader(CursorIds.MESSAGES_WRAPPER_ID, bundle, this);
        }
    }

    private String getSelection(long lastReceivedDate, String threadId) {
        if (lastReceivedDate != -1) {
            return "date" + LESS_THAN + lastReceivedDate + AND + "thread_id" + EQUALS + threadId;
        } else {
            return "thread_id" + EQUALS + threadId;
        }
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
            int index = 0;
            while (cursor.moveToNext()) {
                Message.Builder builder = new Message.Builder(cursor.getInt(THREAD_ID_INDEX))
                        .setAddress(cursor.getLong(ADDRESS_INDEX))
                        .setDate(cursor.getLong(DATE_INDEX))
                        .setPerson(cursor.getInt(PERSON_INDEX))
                        .setType(cursor.getInt(TYPE_INDEX))
                        .setBody(cursor.getString(SNIPPET_INDEX))
                        .setStatus(cursor.getInt(STATUS_INDEX))
                        .setRead(cursor.getInt(READ_INDEX));
                messages.add(index, builder.build());
                if (cursor.getLong(DATE_INDEX) < lastReceivedDate
                        || lastReceivedDate == -1) {
                    lastReceivedDate = cursor.getLong(DATE_INDEX);
                }
                index++;
            }
            cursor.close();
            for (int i = 0; i <listeners.size(); i++) {
                SqlCallback<Message> listener = listeners.valueAt(i);
                listener.onQueryComplete(messages);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void unregisterCallback(int callerId) {
        if (listeners != null
                && listeners.size() != 0) {
            listeners.remove(callerId);
        }
    }
}
