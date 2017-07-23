package com.jameswolfeoliver.pigeon.SqlWrappers;

import android.database.Cursor;
import android.provider.ContactsContract;

import com.jameswolfeoliver.pigeon.Models.Contact;
import com.jameswolfeoliver.pigeon.Utilities.PigeonApplication;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.ObservableEmitter;

public class ContactsWrapper extends Wrapper<Contact> {
    private static final String LOG_TAG = ContactsWrapper.class.getSimpleName();
    private static final int CONTACT_ID_INDEX = 0;
    private static final int LOOKUP_KEY_INDEX = 1;
    private static final int CONTACT_DISPLAY_NAME_INDEX = 2;
    private static final int CONTACT_THUMBNAIL_INDEX = 3;
    private static final int CONTACT_HAS_PHONE_NUMBER_INDEX = 4;

    private static final String[] PROJECTION = new String[]{
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.LOOKUP_KEY,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
            ContactsContract.Contacts.HAS_PHONE_NUMBER};

    public String selectById(int contactId) {
        return "_id" + EQUALS + contactId;
    }

    public String selectByIds(List<Integer> contactIds) {
        StringBuilder idGroup = new StringBuilder();
        idGroup.append("(");
        for(int i = 0; i < contactIds.size(); i++) {
            idGroup.append("' ");
            idGroup.append(contactIds.get(i));
            idGroup.append("' ");
            if (i + 1 == contactIds.size()) {
                idGroup.append(")");
            } else {
                idGroup.append(", ");
            }
        }
        return "_id" + IN + idGroup.toString();
    }

    @Override
    public void go(ObservableEmitter<Contact> subscriber, Query query) {
        String selection = query != null ? query.getSelection() : null;
        final Cursor cursor = getCursor(ContactsContract.Contacts.CONTENT_URI, PROJECTION, selection, null, null);
        while (cursor.moveToNext()) {
            if (cursor.getInt(CONTACT_HAS_PHONE_NUMBER_INDEX) == 1) {
                Contact.Builder builder = new Contact.Builder(cursor.getString(CONTACT_DISPLAY_NAME_INDEX))
                        .setId(cursor.getInt(CONTACT_ID_INDEX))
                        .setLookupKey(cursor.getString(LOOKUP_KEY_INDEX))
                        .setThumbnailUri(cursor.getString(CONTACT_THUMBNAIL_INDEX));

                Cursor phoneCursor = getCursor(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
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
                Contact contact = builder.build();
                subscriber.onNext(contact);
            }
        }
        cursor.close();
        subscriber.onComplete();
    }
}
