package com.jameswolfeoliver.pigeon.Utilities;

import android.Manifest;

import com.jameswolfeoliver.pigeon.R;

import java.util.HashMap;
import java.util.Map;

import io.github.jameswolfeoliver.library.Permission.Permission;

public class PermissionsManager {
    public static Map<String, Permission> permissions = null;

    public static synchronized Map<String, Permission> getPermissionMap() {
        if (permissions == null) {
            permissions = new HashMap<>();

            // Will give permission to all permission in SMS permission group
            permissions.put(Manifest.permission.SEND_SMS, new Permission.Builder()
                    .setIconResourceId(R.drawable.ic_sms)
                    .setBackgroundColorResourceId(R.color.colorPrimary)
                    .setRational(getString(R.string.permission_sms_rational))
                    .setSimpleName(getString(R.string.permission_sms_title))
                    .setSystemName(Manifest.permission.SEND_SMS)
                    .build());

            // Will give permission to all permission in Contact permission group
            permissions.put(Manifest.permission.READ_CONTACTS, new Permission.Builder()
                    .setIconResourceId(R.drawable.ic_people)
                    .setBackgroundColorResourceId(R.color.colorPrimary)
                    .setRational(getString(R.string.permission_contacts_rational))
                    .setSimpleName(getString(R.string.permission_contacts_title))
                    .setSystemName(Manifest.permission.READ_CONTACTS)
                    .build());
        }
        return permissions;
    }

    private static String getString(int id) {
        return PigeonApplication.getAppContext().getString(id);
    }
}
