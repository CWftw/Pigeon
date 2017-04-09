package io.github.jameswolfeoliver.library.Managers;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.jameswolfeoliver.library.Activities.PermissionActivity;
import io.github.jameswolfeoliver.library.Fragments.RationalFragment;
import io.github.jameswolfeoliver.library.Permission.Permission;
import io.github.jameswolfeoliver.library.R;

@TargetApi(23)
public class PermissionRequestManager implements View.OnClickListener {
    private static final int PERMISSION_GRANTED = PackageManager.PERMISSION_GRANTED;
    private static final int PERMISSION_ALWAYS_DENIED = 1;
    private static final int PERMISSION_DENIED = PackageManager.PERMISSION_DENIED;

    // permission_pref_key = "has_permission_been_requested"
    private static final String PERMISSIONS_PREF_PREFIX = "has_";
    private static final String PERMISSION_PREF_SUFFIX = "_been_requested";
    private static final String PERMISSIONS_PREF = "permissions";

    public static final int REQUEST_PERMISSION_SETTING = 10;
    public static final int REQUEST_PERMISSION_CODE = 22;

    private WeakReference<PermissionActivity> permissionActivityWeakReference;
    private PermissionCallbacks.Callbacks callback;
    private String[] permissions;
    private ArrayList<Permission> requiredPermissions;
    private Map<String, Integer> classifiedPermissions;

    private RationalFragment fragment = null;

    public PermissionRequestManager(@NonNull PermissionActivity activity, @NonNull PermissionCallbacks.Callbacks callback) {
        this.permissionActivityWeakReference = new WeakReference<PermissionActivity>(activity);
        this.callback = callback;
        this.classifiedPermissions = null;
    }

    /**
     *
     * @return false if all permissions have already been granted, return
     *         true if requesting user permission
     */
    public boolean requestPermission() throws IllegalArgumentException {
        this.requiredPermissions = permissionActivityWeakReference.get().buildRequiredPermissions();
        if (this.requiredPermissions.size() == 0) {
            return false;
        }

        this.permissions = new String[requiredPermissions.size()];
        for (int i = 0; i < requiredPermissions.size(); i++) {
            permissions[i] = requiredPermissions.get(i).getSystemName();
        }

        this.classifiedPermissions = classifyPermissions(permissions);
        if (!classifiedPermissions.containsValue(PERMISSION_ALWAYS_DENIED)
                && !classifiedPermissions.containsValue(PERMISSION_DENIED)) {
            return false;
        }

        SharedPreferences.Editor sharedPrefs = permissionActivityWeakReference.get().getSharedPreferences(PERMISSIONS_PREF, Context.MODE_PRIVATE).edit();
        for (String permission : classifiedPermissions.keySet()) {
            if (classifiedPermissions.get(permission) != PERMISSION_GRANTED) {
                sharedPrefs.putBoolean(PERMISSIONS_PREF_PREFIX + permission + PERMISSION_PREF_SUFFIX, true);
            }
        }
        sharedPrefs.apply();

        showRationalDialog();

        return true;
    }

    public void onPermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != REQUEST_PERMISSION_CODE) {
            throw new IllegalStateException("PermissionRequestManager.onPermissionResult called without reason");
        }
        if (grantResults.length > 0) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PERMISSION_GRANTED) {
                    classifiedPermissions.put(permissions[i], PERMISSION_GRANTED);
                } else {
                    if (isAlwaysDenied(permissions[i])) {
                        classifiedPermissions.put(permissions[i], PERMISSION_ALWAYS_DENIED);
                    } else {
                        classifiedPermissions.put(permissions[i], PERMISSION_DENIED);
                    }
                }
            }
        } else {
            if (classifiedPermissions != null) {
                String[] lastKnownPermissionSet = classifiedPermissions.keySet().toArray(new String[0]);
                classifiedPermissions = classifyPermissions(lastKnownPermissionSet);
            }
        }
        returnPermissionResults();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PermissionRequestManager.REQUEST_PERMISSION_SETTING) {
            onReturnFromSettings();
        }
    }


    private void onReturnFromSettings() {
        if (classifiedPermissions != null) {
            String[] permissions = classifiedPermissions.keySet().toArray(new String[0]);
            classifiedPermissions = classifyPermissions(permissions);
            returnPermissionResults();
        }
    }

    private boolean isGranted(String permission) {
        return permissionActivityWeakReference.get()
                .checkSelfPermission(permission) == PERMISSION_GRANTED;
    }

    private boolean isAlwaysDenied(String permission) {
        SharedPreferences sharedPrefs = permissionActivityWeakReference.get().getSharedPreferences(PERMISSIONS_PREF, Context.MODE_PRIVATE);
        return sharedPrefs.getBoolean(PERMISSIONS_PREF_PREFIX + permission + PERMISSION_PREF_SUFFIX, false)
                && !permissionActivityWeakReference.get().shouldShowRequestPermissionRationale(permission);
    }

    private Map<String, Integer> classifyPermissions(String[] permissions) {
        Map<String, Integer> classifiedPermissions = new HashMap<String, Integer>();

        for (String permission : permissions) {
            if (isGranted(permission)) {
                classifiedPermissions.put(permission, PERMISSION_GRANTED);
            } else {
                if (isAlwaysDenied(permission)) {
                    classifiedPermissions.put(permission, PERMISSION_ALWAYS_DENIED);
                } else {
                    classifiedPermissions.put(permission, PERMISSION_DENIED);
                }
            }
        }

        return classifiedPermissions;
    }

    private void showRationalDialog() {
        if (this.fragment == null) {
            this.fragment = new RationalFragment(
                    permissionActivityWeakReference.get(),
                    this.requiredPermissions,
                    this);
        }

        fragment.setActionButtonText(
                permissionActivityWeakReference.get().getString(
                        classifiedPermissions.containsValue(PERMISSION_ALWAYS_DENIED)
                                ? R.string.go
                                : android.R.string.ok));

        fragment.show(permissionActivityWeakReference.get().getSupportFragmentManager(), "Rational");
    }

    private void goToSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", permissionActivityWeakReference.get().getPackageName(), null);
        intent.setData(uri);
        permissionActivityWeakReference.get().startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
    }

    private void returnPermissionResults() {
        List<String> grantedList = new ArrayList<String>();
        List<String> deniedList = new ArrayList<String>();
        List<String> alwaysDeniedList = new ArrayList<String>();

        for (String permission : classifiedPermissions.keySet()) {
            if (classifiedPermissions.get(permission) == PERMISSION_GRANTED) {
                grantedList.add(permission);
            } else if (classifiedPermissions.get(permission) == PERMISSION_DENIED) {
                deniedList.add(permission);
            } else if (classifiedPermissions.get(permission) == PERMISSION_ALWAYS_DENIED) {
                alwaysDeniedList.add(permission);
            }
        }

        // Remove permission from shared prefs if granted
        SharedPreferences sharedPrefs = permissionActivityWeakReference.get().getSharedPreferences(PERMISSIONS_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor sharedPrefsEditor = sharedPrefs.edit();
        for (String permission : grantedList) {
            String sharePrefKey = PERMISSIONS_PREF_PREFIX + permission + PERMISSION_PREF_SUFFIX;
            if (sharedPrefs.contains(sharePrefKey)) {
                sharedPrefsEditor.remove(sharePrefKey);
            }
        }
        sharedPrefsEditor.apply();

        callback.onPermissionGranted(grantedList.toArray(new String[0]));
        callback.onPermissionDenied(deniedList.toArray(new String[0]));
        callback.onPermissionAlwaysDenied(alwaysDeniedList.toArray(new String[0]));

        // De-ref
        callback = null;
        classifiedPermissions.clear();
        permissionActivityWeakReference.clear();
    }

    @Override
    public void onClick(View v) {
        fragment.dismissAllowingStateLoss();

        if (classifiedPermissions.containsValue(PERMISSION_ALWAYS_DENIED)) {
            goToSettings();
        } else {
            permissionActivityWeakReference.get().requestPermissions(permissions, REQUEST_PERMISSION_CODE);
        }
    }
}
