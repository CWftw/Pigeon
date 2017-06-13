package com.jameswolfeoliver.pigeon.Activities;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jameswolfeoliver.pigeon.Managers.ContactCacheManager;
import com.jameswolfeoliver.pigeon.Managers.PageCacheManager;
import com.jameswolfeoliver.pigeon.R;
import com.jameswolfeoliver.pigeon.Server.Rest.RestServer;
import com.jameswolfeoliver.pigeon.SqlWrappers.ContactsWrapper;
import com.jameswolfeoliver.pigeon.Utilities.PermissionsManager;

import java.util.ArrayList;

import io.github.jameswolfeoliver.library.Activities.PermissionActivity;
import io.github.jameswolfeoliver.library.Permission.Permission;

public class SplashActivity extends PermissionActivity {

    private ImageView logo;
    private ProgressBar spinner;
    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        logo = (ImageView) findViewById(R.id.app_logo);
        spinner = (ProgressBar) findViewById(R.id.spinner);
        status = (TextView) findViewById(R.id.status);

        logo.setImageResource(R.drawable.app_icon);
        logo.setVisibility(View.VISIBLE);
        status.setText(R.string.status_updating);
        status.setVisibility(View.VISIBLE);
        spinner.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        PageCacheManager.maybeUpdateLocalPageCache(new RestServer.RestCallback<Boolean>() {
            @Override
            public void onResult(Boolean success) {
                if (usePermissions()) {
                    ContactsWrapper contactsWrapper = new ContactsWrapper(SplashActivity.this);
                    ContactCacheManager.getInstance().update(contactsWrapper);
                    transitionToInbox();
                }
            }
        });
    }

    private void transitionToInbox() {
        status.setText(R.string.status_loading);
        spinner.setVisibility(View.INVISIBLE);
        Intent inboxIntent = new Intent(SplashActivity.this, InboxActivity.class);
        startActivity(inboxIntent);
        finish();
        SplashActivity.this.overridePendingTransition(R.anim.fade_grow_in, R.anim.fade_shrink_out);
    }

    @Override
    public ArrayList<Permission> buildRequiredPermissions() {
        final ArrayList<Permission> permissions = new ArrayList<>();
        permissions.add(PermissionsManager.getPermissionMap().get(Manifest.permission.SEND_SMS));
        permissions.add(PermissionsManager.getPermissionMap().get(Manifest.permission.READ_CONTACTS));
        return permissions;
    }

    @Override
    public void onPermissionDenied(String[] permissions) {
        // Todo handle rejection
    }

    @Override
    public void onPermissionAlwaysDenied(String[] permissions) {
        // Todo handle rejection
    }

    @Override
    public void onPermissionGranted(String[] permissions) {
        ContactsWrapper contactsWrapper = new ContactsWrapper(this);
        ContactCacheManager.getInstance().update(contactsWrapper);
        transitionToInbox();
    }
}
