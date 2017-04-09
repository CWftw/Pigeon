package com.jameswolfeoliver.pigeon.Managers;


import android.app.Activity;
import android.os.Handler;
import android.util.LongSparseArray;

import com.jameswolfeoliver.pigeon.Server.Models.Contact;
import com.jameswolfeoliver.pigeon.SqlWrappers.ContactsWrapper;
import com.jameswolfeoliver.pigeon.SqlWrappers.SqlCallback;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class UserCacheManager {
    private static UserCacheManager instance;
    private static final int ID = 44;
    private ContactsWrapper contactsWrapper;
    private final LongSparseArray<Contact> contacts;
    private Handler handler;

    private UserCacheManager() {
        contactsWrapper = new ContactsWrapper();
        handler = new Handler();
        handler.getLooper().getThread().setName(getClass().getSimpleName());
        contacts = new LongSparseArray<Contact>();
    }

    public static UserCacheManager getInstance() {
        if (instance == null) {
            instance = new UserCacheManager();
        }
        return instance;
    }

    public void update(WeakReference<Activity> activity) {
        contactsWrapper.getAllContacts(activity, ID, new SqlCallback<Contact>() {
            @Override
            public void onQueryComplete(ArrayList<Contact> results) {
                handler.post(new ContactsSorter(results));
            }
        });
    }

    public Contact getContact(long phoneNumber) {
        return contacts.get(phoneNumber, contacts.get(prependOne(phoneNumber), null));
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
