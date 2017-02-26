package com.jameswolfeoliver.pigeon.SqlWrappers;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;

import com.jameswolfeoliver.pigeon.Utilities.PigeonApplication;

public class ContactsWrapper implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int CONTACT_ID_INDEX = 0;
    private static final int LOOKUP_KEY_INDEX = 1;
    private static final String[] PROJECTION = {
            Contacts._ID,
            Contacts.LOOKUP_KEY,
            Contacts.DISPLAY_NAME_PRIMARY};
    private static final String SELECTION = Contacts.DISPLAY_NAME_PRIMARY + " LIKE ?";
    private String mSearchString;
    private String[] mSelectionArgs = { mSearchString };

    public static getContacts(final Callback<>)

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(
                PigeonApplication.getAppContext(),
                ContactsContract.Data.CONTENT_URI,
                PROJECTION,
                SELECTION,
                mSelectionArgs,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


}
