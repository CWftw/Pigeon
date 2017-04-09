package io.github.jameswolfeoliver.materialpermissions.Activities;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

import io.github.jameswolfeoliver.library.Activities.PermissionActivity;
import io.github.jameswolfeoliver.library.Permission.Permission;
import io.github.jameswolfeoliver.materialpermissions.R;

public class ExampleActivity extends PermissionActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExampleActivity.this.usePermissions();
            }
        });
    }

    @Override
    public void onPermissionGranted(String[] permissions) {
        for (String s : permissions) {
            Log.d(ExampleActivity.class.getSimpleName(), String.format("%s was granted", s));
        }
    }

    @Override
    public void onPermissionAlwaysDenied(String[] permissions) {
        for (String s : permissions) {
            Log.d(ExampleActivity.class.getSimpleName(), String.format("%s is always denied", s));
        }
    }

    @Override
    public void onPermissionDenied(String[] permissions) {
        for (String s : permissions) {
            Log.d(ExampleActivity.class.getSimpleName(), String.format("%s was denied", s));
        }
    }

    @Override
    public ArrayList<Permission> buildRequiredPermissions() {
        final ArrayList<Permission> permissionList = new ArrayList<Permission>();
        permissionList.add(new Permission.Builder()
                .setIconResourceId(R.drawable.ic_storage_dark)
                .setBackgroundColorResourceId(R.color.colorPrimary)
                .setRational(getString(R.string.permission_rational_text))
                .setSimpleName("Storage")
                .setSystemName(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .build());
        permissionList.add(new Permission.Builder()
                .setIconResourceId(R.drawable.ic_account_dark)
                .setBackgroundColorResourceId(R.color.colorPrimary)
                .setRational(getString(R.string.permission_rational_text))
                .setSimpleName("Contacts")
                .setSystemName(Manifest.permission.READ_CONTACTS)
                .build());
        permissionList.add(new Permission.Builder()
                .setIconResourceId(R.drawable.ic_location_dark)
                .setBackgroundColorResourceId(R.color.colorPrimary)
                .setRational(getString(R.string.permission_rational_text))
                .setSimpleName("Location")
                .setSystemName(Manifest.permission.ACCESS_FINE_LOCATION)
                .build());
        permissionList.add(new Permission.Builder()
                .setIconResourceId(R.drawable.ic_sms_dark)
                .setBackgroundColorResourceId(R.color.colorPrimary)
                .setRational(getString(R.string.permission_rational_text))
                .setSimpleName("SMS")
                .setSystemName(Manifest.permission.READ_SMS)
                .build());
        return permissionList;
    }
}
