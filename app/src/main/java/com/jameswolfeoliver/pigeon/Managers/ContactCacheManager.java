package com.jameswolfeoliver.pigeon.Managers;

import android.os.Handler;
import android.util.LongSparseArray;

import com.jameswolfeoliver.pigeon.SqlWrappers.ContactsWrapper;
import com.jameswolfeoliver.pigeon.SqlWrappers.SqlCallback;

import java.util.ArrayList;
import java.util.Collections;

import com.jameswolfeoliver.pigeon.Models.Contact;

public class ContactCacheManager {
    private static ContactCacheManager instance;
    private static final int ID = 44;
    private final LongSparseArray<Contact> contacts;
    private Handler handler;

    private ContactCacheManager() {
        handler = new Handler();
        // handler.getLooper().getThread().setName(getClass().getSimpleName());
        contacts = new LongSparseArray<Contact>();
    }

    public static ContactCacheManager getInstance() {
        if (instance == null) {
            instance = new ContactCacheManager();
        }
        return instance;
    }

    public void update(ContactsWrapper contactsWrapper) {
        contactsWrapper.get(ID, new SqlCallback<Contact>() {
            @Override
            public void onQueryComplete(ArrayList<Contact> results) {
                handler.post(new ContactsSorter(results));

            }
        });
    }

    public Contact getContact(long phoneNumber) {
        return contacts.get(phoneNumber, contacts.get(prependOne(phoneNumber), makeAnonContact(phoneNumber)));
    }

    private Contact makeAnonContact(long number) {
        Contact.PhoneNumber phoneNumber = new Contact.PhoneNumber("mobile", Long.toString(number));
        return new Contact.Builder(phoneNumber.getPrettyNumber())
                .addPhoneNumbers(Collections.singletonList(phoneNumber))
                .setThumbnailUri("")
                .build();
    }

    private Long prependOne(long phoneNumber) {
        return phoneNumber + 10000000000L;
    }

    private class ContactsSorter implements Runnable {
        private final ArrayList<Contact> contactsToSort;

        private ContactsSorter(ArrayList<Contact> contactsToSort) {
            this.contactsToSort = contactsToSort;
        }

        @Override
        public void run() {
            synchronized (contacts) {
                contacts.clear();
                for (Contact contact : contactsToSort) {
                    for (Contact.PhoneNumber phoneNumber : contact.getPhoneNumbers()) {
                        contacts.put(phoneNumber.getNumber(), contact);
                    }
                }
            }
        }
    }
}
