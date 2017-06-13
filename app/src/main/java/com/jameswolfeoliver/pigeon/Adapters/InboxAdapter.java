package com.jameswolfeoliver.pigeon.Adapters;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.jameswolfeoliver.pigeon.Managers.ContactCacheManager;
import com.jameswolfeoliver.pigeon.R;
import com.jameswolfeoliver.pigeon.Models.Contact;
import com.jameswolfeoliver.pigeon.Models.Conversation;

import java.util.ArrayList;
import java.util.Date;

public class InboxAdapter extends RecyclerView.Adapter<InboxAdapter.ConversationHolder> {

    private Context context;
    private ArrayList<Conversation> conversations;

    public InboxAdapter(Context context, ArrayList<Conversation> conversations) {
        this.context = context;
        this.conversations = conversations;
    }

    public void update(ArrayList<Conversation> conversations) {
        this.conversations.clear();
        this.conversations.addAll(conversations);
        notifyDataSetChanged();
    }

    public Conversation getConversation(int position) {
        return conversations.get(position);
    }

    @Override
    public ConversationHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ConversationHolder(LayoutInflater.from(context).inflate(R.layout.view_inbox_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ConversationHolder holder, int position) {
        Conversation conversation = conversations.get(position);
        Contact contact = ContactCacheManager.getInstance().getContact(conversation.getAddress());
        if (contact != null) {
            holder.setViews(conversation, contact);
        } else {
            holder.setViews(conversation);
        }
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    public class ConversationHolder extends RecyclerView.ViewHolder {
        private Conversation conversation;
        private SimpleDraweeView avatar;
        private TextView address;
        private TextView snippet;
        private TextView date;

        private ConversationHolder(View itemView) {
            super(itemView);
            this.avatar = (SimpleDraweeView) itemView.findViewById(R.id.avatar);
            this.address = (TextView) itemView.findViewById(R.id.address);
            this.snippet = (TextView) itemView.findViewById(R.id.snippet);
            this.date = (TextView) itemView.findViewById(R.id.date);
        }

        private void setViews(Conversation conversation) {
            this.conversation = conversation;
            this.address.setText(String.format("%d", this.conversation.getAddress()));
            String snippet = ((this.conversation.getType() == Conversation.TYPE_USER)
                    ? "You: " : "") + this.conversation.getSnippet();
            this.snippet.setText(snippet);
            String date = DateFormat.format("E, Ka",
                    new Date(this.conversation.getDate())).toString();
            this.date.setText(date);

            if (!this.conversation.getRead()) {
                this.address.setTextColor(context.getResources().getColor(android.R.color.secondary_text_light));
                this.snippet.setTextColor(context.getResources().getColor(android.R.color.secondary_text_light));
            }
        }

        private void setViews(Conversation conversation, Contact contact) {
            this.conversation = conversation;
            this.address.setText(contact.getName());
            this.avatar.setImageURI(contact.getThumbnailUri());
            String snippet = ((this.conversation.getType() == Conversation.TYPE_USER)
                    ? "You: " : "") + this.conversation.getSnippet();
            this.snippet.setText(snippet);
            String date = DateFormat.format("E, Ka",
                    new Date(this.conversation.getDate())).toString();
            this.date.setText(date);

            if (!this.conversation.getRead()) {
                this.address.setTextColor(context.getResources().getColor(android.R.color.secondary_text_light));
                this.snippet.setTextColor(context.getResources().getColor(android.R.color.secondary_text_light));
            }
        }
    }
}
