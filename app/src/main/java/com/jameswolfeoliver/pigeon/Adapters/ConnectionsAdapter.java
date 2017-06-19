package com.jameswolfeoliver.pigeon.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jameswolfeoliver.pigeon.Activities.ConnectionActivity;
import com.jameswolfeoliver.pigeon.Models.Connection;
import com.jameswolfeoliver.pigeon.R;
import com.jameswolfeoliver.pigeon.Utilities.PigeonApplication;

import java.util.ArrayList;
import java.util.Date;


public class ConnectionsAdapter extends RecyclerView.Adapter<ConnectionsAdapter.ConnectionHolder> {
    private final Context context;
    private final ArrayList<Connection> connections;

    public ConnectionsAdapter(final Context context, final ArrayList<Connection> connections) {
        this.context = context;
        this.connections = connections;
    }

    @Override
    public ConnectionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ConnectionHolder.VIEW_TYPE_HEADER:
                return new ConnectionHeaderHolder(LayoutInflater.from(context).inflate(R.layout.view_connection_header, parent, false));
            case ConnectionHolder.VIEW_TYPE_ITEM:
                return new ConnectionItemHolder(LayoutInflater.from(context).inflate(R.layout.view_connection_item, parent, false));
            default:
                throw new IllegalArgumentException(String.format("%d is and invalid item type"));
        }
    }

    @Override
    public void onBindViewHolder(ConnectionHolder holder, int position) {
        holder.onBind(connections.get(position));
    }

    @Override
    public int getItemCount() {
        return connections.size();
    }

    @Override
    public int getItemViewType(int position) {
        return connections.get(position).getType() == ConnectionActivity.TYPE_HEADER
                ? ConnectionHolder.VIEW_TYPE_HEADER : ConnectionHolder.VIEW_TYPE_ITEM;
    }

    public abstract static class ConnectionHolder extends RecyclerView.ViewHolder {
        public static final int VIEW_TYPE_HEADER = 0;
        public static final int VIEW_TYPE_ITEM = 1;

        public ConnectionHolder(View itemView) {
            super(itemView);
        }

        abstract void onBind(Connection connection);
    }

    public static class ConnectionItemHolder extends ConnectionHolder {
        private TextView clientName;
        private TextView clientIp;
        private TextView clientLocation;
        private TextView clientDate;
        private ImageView connectionIcon;

        public ConnectionItemHolder(View itemView) {
            super(itemView);
            this.clientName = (TextView) itemView.findViewById(R.id.client_name);
            this.clientIp = (TextView) itemView.findViewById(R.id.client_ip);
            this.clientLocation = (TextView) itemView.findViewById(R.id.client_location);
            this.clientDate = (TextView) itemView.findViewById(R.id.client_date);
            this.connectionIcon = (ImageView) itemView.findViewById(R.id.connection_icon);
        }

        public void onBind(Connection connection) {
            this.clientName.setText(connection.getName());
            this.clientIp.setText(connection.getIp());
            String date = DateFormat.format("E, Ka", new Date(connection.getTimeStamp())).toString();
            this.clientDate.setText(date);
            this.clientLocation.setText(String.format("%s, %s", connection.getCity(), connection.getCountry()));
            this.connectionIcon.setImageResource(getIconFromType(connection.getType()));
        }

        private int getIconFromType(int clientType) {
            switch (clientType) {
                case Connection.TYPE_PHONE:
                    return R.drawable.ic_phone;
                case Connection.TYPE_TABLET:
                    return R.drawable.ic_tablet;
                case Connection.TYPE_COMPUTER:
                default:
                    return R.drawable.ic_computer;
            }
        }
    }

    public static class ConnectionHeaderHolder extends ConnectionHolder {
        private TextView title;
        private ImageView icon;

        public ConnectionHeaderHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.header_title);
            icon = (ImageView) itemView.findViewById(R.id.header_icon);
        }

        public void onBind(Connection connection) {
            title.setText(connection.getName());
            if (connection.getName().equals(
                    PigeonApplication.getAppContext().getString(R.string.pending_connections))) {
                icon.setImageResource(R.drawable.ic_pending);
            } else {
                icon.setImageResource(R.drawable.ic_history);
            }
        }
    }
}
