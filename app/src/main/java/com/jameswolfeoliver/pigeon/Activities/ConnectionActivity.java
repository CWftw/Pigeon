package com.jameswolfeoliver.pigeon.Activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.jameswolfeoliver.pigeon.Adapters.ConnectionsAdapter;
import com.jameswolfeoliver.pigeon.Managers.NotificationsManager;
import com.jameswolfeoliver.pigeon.R;

import java.util.ArrayList;

import com.jameswolfeoliver.pigeon.Models.Connection;

public class ConnectionActivity extends AppCompatActivity {

    public static final int TYPE_HEADER = -1;

    private RecyclerView connectionsRecyclerView;
    private ArrayList<Connection> connections;
    private ConnectionsAdapter connectionsAdapter;

    @Override
    protected void onStart() {
        super.onStart();
        NotificationsManager.removeAllNotificationsForRemoteClient(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        this.connectionsRecyclerView = (RecyclerView) findViewById(R.id.action_view_connections);

        // Setup toolbar
        getSupportActionBar().setTitle(R.string.connections);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Setup data
        this.connections = new ArrayList<>();
        this.connections.add(createConnectionHeader(getString(R.string.pending_connections)));
        this.connections.add(new Connection.Builder("192.168.1.0")
                .setCity("Guelph")
                .setType(Connection.TYPE_COMPUTER)
                .setName("James' Laptop")
                .setCountry("CA")
                .setTimeStamp(System.currentTimeMillis())
                .build());
        this.connections.add(createConnectionHeader(getString(R.string.active_connections)));
        this.connections.add(new Connection.Builder("192.168.0.132")
                .setCity("Dundalk")
                .setType(Connection.TYPE_TABLET)
                .setName("James' Tablet")
                .setCountry("CA")
                .setTimeStamp(System.currentTimeMillis())
                .build());
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        this.connectionsRecyclerView.setLayoutManager(linearLayoutManager);
        this.connectionsAdapter = new ConnectionsAdapter(this, connections);
        this.connectionsRecyclerView.setAdapter(connectionsAdapter);
    }

    private Connection createConnectionHeader(String headerTitle) {
        return new Connection.Builder("")
                .setType(TYPE_HEADER)
                .setName(headerTitle)
                .build();
    }
}
