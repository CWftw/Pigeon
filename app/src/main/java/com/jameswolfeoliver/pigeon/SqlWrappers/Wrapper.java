package com.jameswolfeoliver.pigeon.SqlWrappers;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.jameswolfeoliver.pigeon.Utilities.PigeonApplication;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.annotations.Nullable;

public abstract class Wrapper<T> {
    private final Context context;
    protected static final String LESS_THAN = " < ";
    protected static final String AND = " AND ";
    protected static final String EQUALS = " = ";
    protected static final String IN = " in ";

    public Wrapper() {
        this(PigeonApplication.getAppContext());
    }

    public Wrapper(Context context) {
        this.context = context;
    }

    public Observable<T> fetch() {
        return fetchInternal(null);
    }

    public Observable<T> find(Query query) {
        return fetchInternal(query);
    }

    private synchronized Observable<T> fetchInternal(Query query) {
        return Observable.create(e -> go(e, query));
    }

    abstract void go(@NonNull ObservableEmitter<T> subscriber, @Nullable Query query);

    protected static Cursor getCursor(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
    }

    protected static ContentResolver getContentResolver() {
        return PigeonApplication.getAppContext().getContentResolver();
    }
}
