package io.github.jameswolfeoliver.library.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.lang.ref.WeakReference;
import java.util.List;

import io.github.jameswolfeoliver.library.Activities.PermissionActivity;
import io.github.jameswolfeoliver.library.Adapters.RationalAdapter;
import io.github.jameswolfeoliver.library.Permission.Permission;
import io.github.jameswolfeoliver.library.R;
import io.github.jameswolfeoliver.library.Views.CircleCutout;
import io.github.jameswolfeoliver.library.Views.CutoutModel;

public class RationalFragment extends DialogFragment implements ViewPager.OnPageChangeListener {

    private ViewPager permissionsPager;
    private TabLayout tabLayout;
    private List<Permission> permissions;
    private RationalAdapter rationalAdapter;
    private WeakReference<PermissionActivity> permissionActivity;
    private Button actionButton;
    private String actionButtonText;
    private CutoutModel circleCutoutModel;
    private CircleCutout circleCutout;
    private int tabOriginX;
    private View.OnClickListener actionButtonListener;

    public RationalFragment(PermissionActivity activity, List<Permission> permissions, View.OnClickListener listener) {
        super();
        permissionActivity = new WeakReference<PermissionActivity>(activity);
        this.permissions = permissions;
        this.setCancelable(false);
        this.actionButtonListener = listener;
        this.actionButtonText = "";
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_rational, container);

        this.actionButton = (Button) rootView.findViewById(R.id.action_button);
        this.actionButton.setText(actionButtonText);
        this.rationalAdapter = new RationalAdapter(getChildFragmentManager(), permissions);
        this.permissionsPager = (ViewPager) rootView.findViewById(R.id.rational_pager);
        this.permissionsPager.addOnPageChangeListener(this);
        this.permissionsPager.setAdapter(rationalAdapter);

        this.tabLayout = (TabLayout) rootView.findViewById(R.id.tab_layout);
        this.tabLayout.setupWithViewPager(permissionsPager, true);
        tabOriginX = (int) tabLayout.getX();

        this.circleCutoutModel = (CutoutModel) rootView.findViewById(R.id.permission_icon_cutout);
        this.circleCutout = (CircleCutout) rootView.findViewById(R.id.arc_background);
        this.circleCutoutModel.setCutoutView(circleCutout);

        return rootView;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (position + 1 == permissions.size() - 1) {
            actionButton.setScaleY(positionOffset);
            actionButton.setScaleX(positionOffset);
            tabLayout.setX(tabOriginX - positionOffsetPixels);
        }
        if (position == permissions.size() - 1) {
            this.actionButton.setOnClickListener(actionButtonListener);
        } else {
            this.actionButton.setOnClickListener(null);
        }
    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public void setActionButtonText(String buttonText) {
        this.actionButtonText = buttonText;
    }

    @Override
    public void onStop() {
        super.onStop();
        permissionActivity.clear();
    }
}
