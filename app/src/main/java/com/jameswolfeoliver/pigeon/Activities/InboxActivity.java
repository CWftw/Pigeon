package com.jameswolfeoliver.pigeon.Activities;

import android.app.AlertDialog;
import android.content.Context;
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
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Switch;

import com.jameswolfeoliver.pigeon.Fragment.InboxFragment;
import com.jameswolfeoliver.pigeon.Fragment.SettingsFragment;
import com.jameswolfeoliver.pigeon.Presenters.InboxPresenter;
import com.jameswolfeoliver.pigeon.R;
import com.jameswolfeoliver.pigeon.Server.TextServer;
import com.wang.avi.AVLoadingIndicatorView;

public class InboxActivity extends AppCompatActivity {

    private static final String LOG_TAG = InboxActivity.class.getSimpleName();

    private RelativeLayout root;
    private FloatingActionButton fab;
    private FrameLayout spinnerWrapper;
    private ImageButton settingsButton;
    private Switch serverSwitch;
    public static Context context;
    
    private InboxPresenter inboxPresenter;

    private InboxFragment inboxFragment;
    private SettingsFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);
        inboxPresenter = new InboxPresenter();
        context = this;

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
        this.serverSwitch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    showServerSetupDialog();
                } else {
                    inboxPresenter.tearDownServer();
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
        inboxPresenter.startReceiver();
        hideSpinner();
    }

    @Override
    public void onDestroy() {
        inboxPresenter.tearDownServer();
        inboxPresenter.stopReceiver();
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
        if (loginUrl.contains("https")) {
            builder.setIcon(R.drawable.ic_https);
        } else {
            builder.setIcon(R.drawable.ic_http);
        }
        builder.setMessage(loginUrl)
                .setTitle(R.string.app_name)
                .setIcon(R.drawable.app_icon)
                .setPositiveButton(android.R.string.ok , new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog connectToPcDialog = builder.create();
        connectToPcDialog.show();
    }

    private void showServerSetupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(InboxActivity.this);
        builder.setIcon(R.drawable.ic_devices_black);
        builder.setTitle(getString(R.string.setup_connection));
        builder.setMessage(getString(R.string.setup_connection_message));


        String positiveText = getString(R.string.setup_encrypted);
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showSpinner();
                        inboxPresenter.startServer(true, new TextServer.ServerCallback() {
                            @Override
                            public void onComplete() {
                                hideSpinner();
                                showConnectToPcDialog(TextServer.getServerUri() + "/login");
                            }
                        });
                    }
                });

        String negativeText = getString(R.string.setup_normal);
        builder.setNegativeButton(negativeText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showSpinner();
                        inboxPresenter.startServer(false, new TextServer.ServerCallback() {
                            @Override
                            public void onComplete() {
                                hideSpinner();
                                showConnectToPcDialog(TextServer.getServerUri() + "/login");
                            }
                        });
                    }
                });

        AlertDialog securityDialog = builder.create();
        securityDialog.show();
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
