package com.jameswolfeoliver.pigeon.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Switch;

import com.jameswolfeoliver.pigeon.Fragment.InboxFragment;
import com.jameswolfeoliver.pigeon.Fragment.SettingsFragment;
import com.jameswolfeoliver.pigeon.Presenters.InboxPresenter;
import com.jameswolfeoliver.pigeon.R;
import com.wang.avi.AVLoadingIndicatorView;

public class InboxActivity extends AppCompatActivity {

    private static final String LOG_TAG = InboxActivity.class.getSimpleName();

    private RelativeLayout root;
    private FloatingActionButton fab;
    private FrameLayout spinnerWrapper;
    private ImageButton settingsButton;
    private Switch serverSwitch;


    private InboxFragment inboxFragment;
    private SettingsFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        // Set root view and show spinner
        root = (RelativeLayout) findViewById(R.id.root_layout);
        spinnerWrapper = (FrameLayout) findViewById(R.id.spinner_wrapper);
        showSpinner();

        // Setup fragments and inflate default (inbox)
        this.settingsFragment = new SettingsFragment();
        this.inboxFragment = new InboxFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container,
                        this.inboxFragment)
                .commit();


        initViews();
    }

    private void initViews(){
        // Set Action Bar
        Toolbar inboxActionBar = (Toolbar) findViewById(R.id.inbox_action_bar);
        this.setSupportActionBar(inboxActionBar);

        this.serverSwitch = (Switch) findViewById(R.id.server_switch);
        this.settingsButton= (ImageButton) findViewById(R.id.settings_button);

        // Set Action Bar onClicks
        this.settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSettings();
            }
        });
        this.serverSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (serverSwitch.isChecked()) {
                    showSetupServerDialog();
                }
            }
        });

        // Set FAB
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        InboxPresenter.getInstance().startServer(this, new InboxPresenter.Callbacks() {
            @Override
            public void onServerStarted(String serverLoginUrl) {
                showConnectToPcDialog(serverLoginUrl);
            }
        });
        InboxPresenter.getInstance().startReceiver();
        hideSpinner();
    }

    @Override
    public void onDestroy() {
        InboxPresenter.getInstance().tearDownServer();
        InboxPresenter.getInstance().stopReceiver();
        super.onDestroy();
    }

    private void showSpinner() {
        if (spinnerWrapper != null && spinnerWrapper.getVisibility() != View.VISIBLE) {
            AVLoadingIndicatorView spinner = (AVLoadingIndicatorView) spinnerWrapper.findViewById(R.id.spinner);
            spinner.hide();
            spinnerWrapper.setVisibility(View.VISIBLE);
            spinner.smoothToShow();
        }
    }

    private void hideSpinner() {
        if (spinnerWrapper != null && spinnerWrapper.getVisibility() != View.GONE) {
            AVLoadingIndicatorView spinner = (AVLoadingIndicatorView) spinnerWrapper.findViewById(R.id.spinner);
            spinner.smoothToHide();
            spinnerWrapper.setVisibility(View.GONE);
        }
    }

    private void showConnectToPcDialog(String loginUrl){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(loginUrl)
                .setTitle("Pigeon")
                .setIcon(R.drawable.app_icon)
                .setPositiveButton("OK" , new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog connectToPcDialog = builder.create();
        connectToPcDialog.show();
    }

    private void showSetupServerDialog() {
        showConnectToPcDialog("");
    }

    private void goToSettings() {
        swapFragment(this.settingsFragment);
        this.settingsButton.setImageResource(R.drawable.ic_arrow_back);
        this.serverSwitch.setVisibility(View.GONE);
        this.settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToInbox();
            }
        });
    }

    private void backToInbox() {
        swapFragment(this.inboxFragment);
        this.settingsButton.setImageResource(R.drawable.ic_settings);
        this.serverSwitch.setVisibility(View.VISIBLE);
        this.settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSettings();
            }
        });
    }

    private void swapFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (fragment.equals(this.inboxFragment)) {
            fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
        } else {
            fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
        fragmentTransaction.replace(R.id.fragment_container, fragment).commit();
    }
}
