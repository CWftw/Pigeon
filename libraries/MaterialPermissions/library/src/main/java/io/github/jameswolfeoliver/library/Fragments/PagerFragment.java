package io.github.jameswolfeoliver.library.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import io.github.jameswolfeoliver.library.Permission.Permission;
import io.github.jameswolfeoliver.library.R;

public class PagerFragment extends Fragment {

    private Permission permission;
    private ImageView permissionIcon;
    private TextView permissionRational;
    private TextView permissionTitle;

    public PagerFragment(Permission permission) {
        super();
        this.permission = permission;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_pager, container, false);
        init(rootView);
        return rootView;
    }

    private void init(View rootView) {
        this.permissionIcon = (ImageView) rootView.findViewById(R.id.permission_icon);
        this.permissionRational = (TextView) rootView.findViewById(R.id.permission_rational);
        this.permissionTitle = (TextView) rootView.findViewById(R.id.permission_title);
        populatePermission(permission);
    }

    // region setters
    private void populatePermission(Permission permission) {
        if (permission != null) {
            setPermissionIcon(permission.getIconResourceId());
            setPermissionTitle(permission.getSimpleName());
            setPermissionRational(permission.getRational());
        }
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }

    public void setPermissionIcon(int resourceId) {
        this.permissionIcon.setImageResource(resourceId);
    }

    public void setPermissionTitle(int resourceId) {
        this.permissionTitle.setText(resourceId);
    }

    public void setPermissionTitle(String title) {
        this.permissionTitle.setText(title);
    }

    public void setPermissionRational(int resourceId) {
        this.permissionRational.setText(resourceId);
    }

    public void setPermissionRational(String rational) {
        this.permissionRational.setText(rational);
    }}
