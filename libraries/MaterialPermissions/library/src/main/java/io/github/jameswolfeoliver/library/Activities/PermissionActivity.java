package io.github.jameswolfeoliver.library.Activities;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;

import io.github.jameswolfeoliver.library.Managers.PermissionCallbacks;
import io.github.jameswolfeoliver.library.Managers.PermissionRequestManager;
import io.github.jameswolfeoliver.library.Permission.Permission;

public abstract class PermissionActivity extends AppCompatActivity implements PermissionCallbacks.Callbacks {

    private PermissionRequestManager permissionManager;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionManager != null
                && requestCode == PermissionRequestManager.REQUEST_PERMISSION_CODE) {
            permissionManager.onPermissionResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * @param callback callback for when
     *  {@link PermissionCallbacks.Callbacks}
     * @param permissions String array of permissions to be used
     * @return true if permission is already given, return false if permission will be requested
     */
    public boolean usePermissions(final PermissionCallbacks.Callbacks callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissionManager = new PermissionRequestManager(this, callback);
            return !permissionManager.requestPermission();
        }
        return true;
    }

    public boolean usePermissions() {
        return usePermissions(this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (permissionManager != null) {
            permissionManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    abstract public ArrayList<Permission> buildRequiredPermissions();

    @Override
    abstract public void onPermissionDenied(String[] permissions);

    @Override
    abstract public void onPermissionAlwaysDenied(String[] permissions);

    @Override
    abstract public void onPermissionGranted(String[] permissions);
}
