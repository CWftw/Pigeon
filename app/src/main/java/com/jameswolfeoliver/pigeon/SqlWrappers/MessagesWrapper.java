package com.jameswolfeoliver.pigeon.SqlWrappers;

import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.jameswolfeoliver.pigeon.Models.Message;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.annotations.Nullable;

public class MessagesWrapper extends Wrapper<List<Message>> {
    private static final int ADDRESS_INDEX = 0;
    private static final int PERSON_INDEX = 1;
    private static final int DATE_INDEX = 2;
    private static final int THREAD_ID_INDEX = 3;
    private static final int SNIPPET_INDEX = 4;
    private static final int TYPE_INDEX = 5;
    private static final int READ_INDEX = 6;
    private static final int STATUS_INDEX = 7;
    private static final String SORT_ORDER = "date desc limit 25";
    private static final String[] PROJECTION = new String[]{
            "address",
            "person",
            "date",
            "thread_id",
            "body",
            "type",
            "read",
            "status"};
    private final Uri MESSAGES_CONTENT_URI = Uri.parse("content://mms-sms/complete-conversations");
    private long lastReceivedDate = -1L;
    private String threadId;

    public MessagesWrapper(String threadId) {
        super();
        this.threadId = threadId;
    }

    public String selectByDateRangeAndThread(long lastReceivedDate, String threadId) {
        if (lastReceivedDate != -1) {
            return "date" + LESS_THAN + lastReceivedDate + AND + "thread_id" + EQUALS + threadId;
        } else {
            return "thread_id" + EQUALS + threadId;
        }
    }

    @Override
    public Observable<List<Message>> fetch() {
        return find(() -> selectByDateRangeAndThread(lastReceivedDate, threadId));
    }

    @Override
    public void go(@NonNull ObservableEmitter<List<Message>> subscriber, @Nullable Query query) {
        String selection = query != null ? query.getSelection() : null;
        final Cursor cursor = getCursor(MESSAGES_CONTENT_URI,
                PROJECTION,
                selection,
                null,
                SORT_ORDER);
        List<Message> messages = new ArrayList<>();
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
            Log.i("James", builder.build().toString());
            if (cursor.getLong(DATE_INDEX) < lastReceivedDate
                    || lastReceivedDate == -1) {
                lastReceivedDate = cursor.getLong(DATE_INDEX);
            }
            index++;
        }
        subscriber.onNext(messages);
        cursor.close();
        subscriber.onComplete();
    }
}
