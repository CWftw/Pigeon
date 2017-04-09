package com.jameswolfeoliver.pigeon.SqlWrappers;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.util.SparseArray;
import com.jameswolfeoliver.pigeon.Server.Models.Contact;
import com.jameswolfeoliver.pigeon.Utilities.PigeonApplication;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class ContactsWrapper implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int CONTACT_ID_INDEX = 0;
    private static final int LOOKUP_KEY_INDEX = 1;
    private static final int CONTACT_DISPLAY_NAME_INDEX = 2;
    private static final int CONTACT_THUMBNAIL_INDEX = 3;
    private static final int CONTACT_HAS_PHONE_NUMBER_INDEX = 4;

    private static final String[] PROJECTION = new String[] {
            Contacts._ID,
            Contacts.LOOKUP_KEY,
            Contacts.DISPLAY_NAME_PRIMARY,
            Contacts.PHOTO_THUMBNAIL_URI,
            Contacts.HAS_PHONE_NUMBER};

    private static final String SELECTION_SUFFIX = " LIKE ?";

    private SparseArray<SqlCallback<Contact>> listeners;

    public ContactsWrapper() {
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

    public void getAllContacts(WeakReference<Activity> activity, int callerId,
                               SqlCallback<Contact> contactsCallback) {
        this.listeners.put(callerId, contactsCallback);
        activity.get().getLoaderManager().initLoader(CursorIds.CONTACTS_WRAPPER_ID, null, this);
    }

    public void queryContacts(String selectionDimension, String query, int callerId,
                              SqlCallback<Contact> contactsCallback, WeakReference<Activity> activity) {
        this.listeners.put(callerId, contactsCallback);
        activity.get().getLoaderManager().initLoader(CursorIds.CONTACTS_WRAPPER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String selection = null;
        String[] selectionArgs = null;

        return new CursorLoader(
                PigeonApplication.getAppContext(),
                Contacts.CONTENT_URI,
                PROJECTION,
                selection,
                selectionArgs,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (listeners != null && listeners.size() != 0) {
            populateContacts(cursor);
        } else {
            cursor.close();
        }
    }

    private void populateContacts(final Cursor cursor) {
        Handler rawContactThread = new Handler();
        rawContactThread.post(new Runnable() {
            @Override
            public void run() {
                ArrayList<Contact> contacts = new ArrayList<>();
                while (cursor.moveToNext()) {
                    if (cursor.getInt(CONTACT_HAS_PHONE_NUMBER_INDEX) == 1) {
                        Contact.Builder builder = new Contact.Builder(cursor.getString(CONTACT_DISPLAY_NAME_INDEX))
                                .setId(cursor.getInt(CONTACT_ID_INDEX))
                                .setLookupKey(cursor.getString(LOOKUP_KEY_INDEX))
                                .setThumbnailUri(cursor.getString(CONTACT_THUMBNAIL_INDEX));

                        Cursor phoneCursor = PigeonApplication
                                .getAppContext()
                                .getContentResolver()
                                .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                        null,
                                        ContactsContract.RawContacts.CONTACT_ID + "=?",
                                        new String[]{cursor.getString(CONTACT_ID_INDEX)},
                                        null);

                        ArrayList<Contact.PhoneNumber> phoneNumbers = new ArrayList<>();
                        while (phoneCursor.moveToNext()) {
                            phoneNumbers.add(new Contact.PhoneNumber(
                                    PigeonApplication
                                            .getAppContext()
                                            .getString(ContactsContract.CommonDataKinds.Phone.getTypeLabelResource(phoneCursor.getInt(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)))),
                                    phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))));
                        }

                        phoneCursor.close();
                        builder.addPhoneNumbers(phoneNumbers);
                        contacts.add(builder.build());
                    }
                }
                cursor.close();
                for (int i = 0; i <listeners.size(); i++) {
                    SqlCallback<Contact> listener = listeners.valueAt(i);
                    listener.onQueryComplete(contacts);
                }
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
