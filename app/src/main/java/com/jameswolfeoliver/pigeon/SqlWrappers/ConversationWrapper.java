package com.jameswolfeoliver.pigeon.SqlWrappers;

import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.jameswolfeoliver.pigeon.Models.Conversation;

import io.reactivex.ObservableEmitter;
import io.reactivex.annotations.Nullable;

public class ConversationWrapper extends Wrapper<Conversation> {
    private static final int ADDRESS_INDEX = 0;
    private static final int PERSON_INDEX = 1;
    private static final int DATE_INDEX = 2;
    private static final int THREAD_ID_INDEX = 3;
    private static final int SNIPPET_INDEX = 4;
    private static final int TYPE_INDEX = 5;
    private static final int READ_INDEX = 6;
    private static final int STATUS_INDEX = 7;
    private static final String SORT_ORDER = "date desc";
    private static final String[] PROJECTION = new String[]{
            "address",
            "person",
            "date",
            "thread_id",
            "body",
            "type",
            "read",
            "status"};
    private final Uri CONVERSATIONS_CONTENT_URI = Uri.parse("content://mms-sms/conversations");

    @Override
    void go(@NonNull ObservableEmitter<Conversation> subscriber, @Nullable Query query) {
        String selection = query != null ? query.getSelection() : null;
        final Cursor cursor = getCursor(CONVERSATIONS_CONTENT_URI,
                PROJECTION,
                selection,
                null,
                SORT_ORDER);
        while (cursor.moveToNext()) {
            Conversation.Builder builder = new Conversation.Builder(cursor.getInt(THREAD_ID_INDEX))
                    .setAddress(cursor.getLong(ADDRESS_INDEX))
                    .setDate(cursor.getLong(DATE_INDEX))
                    .setPerson(cursor.getInt(PERSON_INDEX))
                    .setType(cursor.getInt(TYPE_INDEX))
                    .setSnippet(cursor.getString(SNIPPET_INDEX))
                    .setStatus(cursor.getInt(STATUS_INDEX))
                    .setRead(cursor.getInt(READ_INDEX));
            subscriber.onNext(builder.build());
        }
        cursor.close();
        subscriber.onComplete();
    }
}
