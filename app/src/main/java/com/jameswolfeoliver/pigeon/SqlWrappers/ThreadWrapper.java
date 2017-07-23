package com.jameswolfeoliver.pigeon.SqlWrappers;

import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;

import io.reactivex.ObservableEmitter;
import io.reactivex.annotations.Nullable;

public class ThreadWrapper extends Wrapper<Integer> {
    private final static Uri THREAD_ID_URI = Uri.parse("content://mms-sms/canonical-addresses");
    private final static String[] PROJECTION = new String[]{"_id"};

    @Override
    public void go(@NonNull ObservableEmitter<Integer> subscriber, @Nullable Query query) {
        String selection = query != null ? query.getSelection() : null;
        final Cursor cursor = getCursor(THREAD_ID_URI,
                    PROJECTION,
                    "address" + " = ?",
                    new String[]{selection},
                    null);
        String threadId;
        while (cursor.moveToNext()) {
            threadId = cursor.getString(cursor.getColumnIndex("_id"));
            subscriber.onNext(Integer.parseInt(threadId));
        }
        cursor.close();
        subscriber.onComplete();
    }
}
