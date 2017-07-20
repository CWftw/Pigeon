package com.jameswolfeoliver.pigeon.Managers;

import android.util.LongSparseArray;

import com.jameswolfeoliver.pigeon.Models.Contact;
import com.jameswolfeoliver.pigeon.SqlWrappers.ContactsWrapper;
import com.jameswolfeoliver.pigeon.Utilities.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import io.reactivex.schedulers.Schedulers;

public class ContactCacheManager {
    private static final String LOG_TAG = ContactCacheManager.class.getSimpleName();
    private static ContactCacheManager instance;
    private final LongSparseArray<Contact> contacts;
    private final ContactsWrapper contactsWrapper;

    private ContactCacheManager() {
        contactsWrapper = new ContactsWrapper();
        contacts = new LongSparseArray<Contact>();
    }

    public static ContactCacheManager getInstance() {
        if (instance == null) {
            instance = new ContactCacheManager();
        }
        return instance;
    }

    public void update() {
        contactsWrapper.fetch()
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe((contact) -> {
                    for (Contact.PhoneNumber phoneNumber : contact.getPhoneNumbers())
                        contacts.put(phoneNumber.getNumber(), contact);
                });
    }

    public ContactCacheManager updateNow() {
        synchronized (contactsWrapper) {
            final Iterable<Contact> conversations = contactsWrapper.fetch()
                    .blockingIterable();
            final Iterator<Contact> conversationIterator = conversations.iterator();
            while (conversationIterator.hasNext()) {
                Contact contact = conversationIterator.next();
                for (Contact.PhoneNumber phoneNumber : contact.getPhoneNumbers())
                    contacts.put(phoneNumber.getNumber(), contact);
            }
        }
        return this;
    }

    public List<Contact> getContacts() {
        List<Contact> arrayList = new ArrayList<Contact>(contacts.size());
        for (int i = 0; i < contacts.size(); i++)
            arrayList.add(contacts.valueAt(i));
        return arrayList;
    }

    public List<Contact> getContacts(List<Long> phoneNumbers) {
        final List<Contact> contactList = new ArrayList<>();
        for (Long phoneNumber : phoneNumbers) {
            contactList.add(getContact(phoneNumber));
        }
        return contactList;
    }

    public Contact getContact(long phoneNumber) {
        return contacts.get(phoneNumber, contacts.get(prependOne(phoneNumber), contacts.get(dePrependOne(phoneNumber), makeAnonContact(phoneNumber))));
    }

    private Contact makeAnonContact(long number) {
        Contact.PhoneNumber phoneNumber = new Contact.PhoneNumber("mobile", Long.toString(number));
        return new Contact.Builder(phoneNumber.getPrettyNumber())
                .addPhoneNumbers(Collections.singletonList(phoneNumber))
                .setId(0)
                .setThumbnailUri("")
                .build();
    }

    private Long prependOne(long phoneNumber) {
        return phoneNumber + 10000000000L;
    }

    private Long dePrependOne(long phoneNumber) {
        return phoneNumber - 10000000000L > 0L ? phoneNumber - 10000000000L : phoneNumber;
    }
}
