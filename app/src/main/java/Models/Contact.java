package Models;

import android.annotation.SuppressLint;

import com.google.gson.annotations.SerializedName;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class Contact {
    @SerializedName("name")
    private String name;

    @SerializedName("id")
    private int id;

    @SerializedName("lookupKey")
    private String lookupKey;

    @SerializedName("thumbnailUri")
    private String thumbnailUri;

    @SerializedName("phoneNumbers")
    private HashSet<PhoneNumber> phoneNumbers;

    private Contact(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLookupKey() {
        return lookupKey;
    }

    public String getThumbnailUri() {
        return thumbnailUri;
    }

    public HashSet<PhoneNumber> getPhoneNumbers() {
        return phoneNumbers;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public String toString() {
        String contactPhoneNumbers = "";
        for(PhoneNumber phoneNumber : phoneNumbers) {
            contactPhoneNumbers += phoneNumber.toString() + "\n\t\t";
        }
        return String.format("id: %d; name: %s; lookupKey: %s;" +
                "\n\tthumbnailUri: %s; " +
                "\n\tphoneNumbers: " +
                "\n\t\t%s", id, name, lookupKey, thumbnailUri, contactPhoneNumbers);
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof Conversation)) {
            return false;
        }
        Contact contact = (Contact) object;
        return Objects.equals(this.id, contact.id)
                && Objects.equals(this.name, contact.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    public static class Builder {
        private Contact contact;

        public Builder(String name) {
            this.contact = new Contact(name);
            this.contact.id = -1;
            this.contact.lookupKey = "";
            this.contact.thumbnailUri = "";
            this.contact.phoneNumbers = new HashSet<>();
        }

        public Builder setId(int id) {
            contact.id = id;
            return this;
        }

        public Builder setLookupKey(String lookupKey) {
            contact.lookupKey = lookupKey;
            return this;
        }

        public Builder setName(String name) {
            if (name != null)
                contact.name = name;
            return this;
        }

        public Builder setThumbnailUri(String thumbnailUri) {
            if (thumbnailUri != null)
                contact.thumbnailUri = thumbnailUri;
            return this;
        }

        public Builder addPhoneNumbers(List<PhoneNumber> phoneNumbers) {
            this.contact.phoneNumbers.addAll(phoneNumbers);
            return this;
        }

        public Contact build() {
            return contact;
        }
    }

    public static class PhoneNumber {
        @SerializedName("type")
        private final String type;

        @SerializedName("number")
        private final long number;

        public PhoneNumber(String type, String number){
            this.type = type;
            this.number = Long.parseLong(sanitizeNumber(number));

        }

        private String sanitizeNumber(String dirtyNumber) {
            return dirtyNumber
                    .replace("+", "")
                    .replace("-", "")
                    .replace("(", "")
                    .replace(")", "")
                    .replace(" ", "")
                    .trim();
        }

        public String getPrettyNumber() {
            return Long.toString(number);
        }

        @Override
        public String toString() {
            return getType() +": "+ getNumber();
        }

        @Override
        public boolean equals(Object object) {
            if (object == this) {
                return true;
            }
            if (!(object instanceof PhoneNumber)) {
                return false;
            }
            PhoneNumber number = (PhoneNumber) object;
            return Objects.equals(this.type, number.type)
                    && Objects.equals(this.number, number.number);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.type, this.number);
        }

        public String getType() {
            return type;
        }

        public long getNumber() {
            return number;
        }
    }
}
