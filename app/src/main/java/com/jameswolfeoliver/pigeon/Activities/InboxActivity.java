package com.jameswolfeoliver.pigeon.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.jameswolfeoliver.pigeon.Fragment.InboxFragment;
import com.jameswolfeoliver.pigeon.Fragment.SettingsFragment;
import com.jameswolfeoliver.pigeon.Managers.ContactCacheManager;
import com.jameswolfeoliver.pigeon.Managers.NotificationsManager;
import com.jameswolfeoliver.pigeon.Presenters.BasePresenter;
import com.jameswolfeoliver.pigeon.R;
import com.jameswolfeoliver.pigeon.Services.TextService;
import com.jameswolfeoliver.pigeon.SqlWrappers.ContactsWrapper;
import com.jameswolfeoliver.pigeon.Utilities.PigeonApplication;

public class InboxActivity extends BaseActivity {

    private static final String LOG_TAG = InboxActivity.class.getSimpleName();

    private FloatingActionButton fab;
    private FrameLayout spinnerWrapper;
    private ContactsWrapper contactsWrapper;

    private InboxFragment inboxFragment;
    private SettingsFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        // Set root view and show spinner
        spinnerWrapper = (FrameLayout) findViewById(R.id.spinner_wrapper);
        showSpinner();

        // Setup fragments and inflate default (inbox)
        this.settingsFragment = new SettingsFragment();
        this.inboxFragment = new InboxFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, inboxFragment)
                .commit();

        initViews();

        contactsWrapper = new ContactsWrapper(this);
    }

    private void initViews() {
        // Set Action Bar
        getSupportActionBar().setTitle(getString(R.string.inbox));
        getSupportActionBar().setHomeButtonEnabled(false);


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
        hideSpinner();
        ContactCacheManager.getInstance().update(contactsWrapper);
        NotificationsManager.removeAllNotifications(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_settings:
                goToSettings();
                return true;
            case R.id.action_view_connections:
                Intent confirmConnectionIntent = new Intent(PigeonApplication.getAppContext(), ConnectionActivity.class);
                PigeonApplication.getAppContext().startActivity(confirmConnectionIntent);
                return true;
            case R.id.action_connect_to_pc:
                showConnectionStatus();
                return true;
            default:
                return (super.onOptionsItemSelected(menuItem));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_inbox, menu);
        return true;
    }

    private void showSpinner() {
        if (spinnerWrapper != null && spinnerWrapper.getVisibility() != View.VISIBLE) {
            ProgressBar spinner = (ProgressBar) spinnerWrapper.findViewById(R.id.spinner);
            spinnerWrapper.setVisibility(View.VISIBLE);
        }
    }

    private void hideSpinner() {
        if (spinnerWrapper != null && spinnerWrapper.getVisibility() != View.GONE) {
            ProgressBar spinner = (ProgressBar) spinnerWrapper.findViewById(R.id.spinner);
            spinnerWrapper.setVisibility(View.GONE);
        }
    }

    private void showConnectionStatus() {
        basePresenter.isServerRunning(new BasePresenter.ServerStatusCallback() {
            @Override
            public void onInfoReceived(final String uri, final boolean secure, final int status) {
                if (status == TextService.Status.RUNNING) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(InboxActivity.this);
                    builder.setMessage(String.format(getString(R.string.connected_message), uri + "/inbox"))
                            .setTitle(R.string.connect_to_pc)
                            .setIcon(R.drawable.ic_phonelink_dark)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton(R.string.disconnect, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    basePresenter.tearDownServer(null);
                                }
                            });

                    AlertDialog connectToPcDialog = builder.create();
                    connectToPcDialog.show();
                } else {
                    showServerSetupDialog();
                }
            }
        });
    }

    private void showServerSetupDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            builder.setIcon(R.drawable.ic_phonelink_dark);
            builder.setTitle(getString(R.string.setup_connection));
            builder.setMessage(getString(R.string.setup_connection_message));

            String positiveText = getString(R.string.setup_encrypted);
            builder.setPositiveButton(positiveText,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            showSpinner();
                            basePresenter.startServer(true, new BasePresenter.ServerStatusCallback() {
                                @Override
                                public void onInfoReceived(String uri, boolean secure, int status) {
                                    hideSpinner();
                                    if (status == TextService.Status.RUNNING) {
                                        showConnectionSuccess(uri);
                                    } else {
                                        Toast.makeText(getApplicationContext(), R.string.connection_failed, Toast.LENGTH_SHORT).show();
                                    }
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
                            basePresenter.startServer(true, new BasePresenter.ServerStatusCallback() {
                                @Override
                                public void onInfoReceived(String uri, boolean secure, int status) {
                                    hideSpinner();
                                    if (status == TextService.Status.RUNNING) {
                                        showConnectionSuccess(uri);
                                    } else {
                                        Toast.makeText(getApplicationContext(), R.string.connection_failed, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });

            final AlertDialog securityDialog = builder.create();
            securityDialog.show();
        } else {
            // Connect to wifi
            builder.setIcon(R.drawable.ic_signal_wifi_off);
            builder.setTitle(getString(R.string.setup_connection));
            builder.setMessage(getString(R.string.setup_connection_wifi_error_message));

            String positiveText = getString(android.R.string.ok);
            builder.setPositiveButton(positiveText,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
                        }
                    });

            final AlertDialog networkDialog = builder.create();
            networkDialog.show();
        }
    }

    private void goToSettings() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.addToBackStack(getString(R.string.settings))
                .add(R.id.fragment_container, settingsFragment)
                .commit();
    }

    @Override
    public void onNetworkStateChange(boolean isAvailable) {

    }
}
