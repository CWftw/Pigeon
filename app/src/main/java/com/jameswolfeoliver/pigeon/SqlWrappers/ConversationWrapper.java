package com.jameswolfeoliver.pigeon.SqlWrappers;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseArray;
import com.jameswolfeoliver.pigeon.Server.Models.Conversation;
import com.jameswolfeoliver.pigeon.Utilities.PigeonApplication;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class ConversationWrapper implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int ADDRESS_INDEX = 0;
    private static final int PERSON_INDEX = 1;
    private static final int DATE_INDEX = 2;
    private static final int THREAD_ID_INDEX = 3;
    private static final int SNIPPET_INDEX = 4;
    private static final int TYPE_INDEX = 5;
    private static final int READ_INDEX = 6;
    private static final int STATUS_INDEX = 7;
    private static final String SELECTION_SUFFIX = " LIKE ?";
    private static final String SORT_ORDER = "date desc";
    private final Uri CONVERSATIONS_CONTENT_URI = Uri.parse("content://mms-sms/conversations");
    private static final String[] PROJECTION = new String[] {
            "address",
            "person",
            "date",
            "thread_id",
            "body",
            "type",
            "read",
            "status"};



    private SparseArray<SqlCallback<Conversation>> listeners;

    public ConversationWrapper() {
        this.listeners = new SparseArray<>();
    }

    private String getSelection(String selectionDimension) {
        return selectionDimension + SELECTION_SUFFIX;
    }

    private String[] getSelectionArgs(String query) {
        return new String[] {"%" + query + "%"};
    }

    public void unregisterCallback(int callerId) {
        if (listeners != null
                && listeners.size() != 0
                && listeners.indexOfKey(callerId) > 0) {
            listeners.remove(callerId);
        }
    }

    public void getAllConversations(WeakReference<Activity> activity, int callerId,
                                    SqlCallback<Conversation> conversationCallback) {
        this.listeners.put(callerId, conversationCallback);
        activity.get().getLoaderManager().initLoader(CursorIds.CONVERSATIONS_WRAPPER_ID, null, this);
    }

    public void queryConversations(String selectionDimension, String query, int callerId,
                              WeakReference<Activity> activity, SqlCallback<Conversation> conversationCallback) {
        this.listeners.put(callerId, conversationCallback);
        activity.get().getLoaderManager().initLoader(CursorIds.CONVERSATIONS_WRAPPER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String selection = null;
        String[] selectionArgs = null;

        return new CursorLoader(
                PigeonApplication.getAppContext(),
                CONVERSATIONS_CONTENT_URI,
                PROJECTION,
                selection,
                selectionArgs,
                SORT_ORDER
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (listeners != null && listeners.size() != 0) {
            ArrayList<Conversation> conversations = new ArrayList<>();
            while (cursor.moveToNext()) {
                Conversation.Builder builder = new Conversation.Builder(cursor.getInt(THREAD_ID_INDEX))
                        .setAddress(cursor.getLong(ADDRESS_INDEX))
                        .setDate(cursor.getLong(DATE_INDEX))
                        .setPerson(cursor.getInt(PERSON_INDEX))
                        .setType(cursor.getInt(TYPE_INDEX))
                        .setSnippet(cursor.getString(SNIPPET_INDEX))
                        .setStatus(cursor.getInt(STATUS_INDEX))
                        .setRead(cursor.getInt(READ_INDEX));
                conversations.add(builder.build());
            }
            for (int i = 0; i <listeners.size(); i++) {
                SqlCallback<Conversation> listener = listeners.valueAt(i);
                listener.onQueryComplete(conversations);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
