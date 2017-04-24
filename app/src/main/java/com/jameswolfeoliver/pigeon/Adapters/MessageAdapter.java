package com.jameswolfeoliver.pigeon.Adapters;

import android.content.Context;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.jameswolfeoliver.pigeon.R;
import com.jameswolfeoliver.pigeon.Server.Models.Contact;
import com.jameswolfeoliver.pigeon.Server.Models.Conversation;
import com.jameswolfeoliver.pigeon.Server.Models.Message;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageHolder> {

    private ArrayList<Message> dataSet;
    private Context context;
    private Contact contact;

    public MessageAdapter(Context context, ArrayList<Message> dataSet, Contact contact) {
        this.context = context;
        this.dataSet = dataSet;
        this.contact = contact;
    }

    public void update(ArrayList<Message> messages) {
        int rangeStart = this.dataSet.size();
        this.dataSet.addAll(messages);
        int rangeEnd = this.dataSet.size() - 1;
        notifyItemRangeInserted(rangeStart, rangeEnd);
    }

    @Override
    public MessageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MessageHolder(LayoutInflater.from(context).inflate(R.layout.view_message_item, parent, false));
    }

    @Override
    public void onBindViewHolder(MessageHolder holder, int position) {
        holder.setMessage(dataSet.get(position));
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public class MessageHolder extends RecyclerView.ViewHolder {
        private Message message;
        private View rootView;
        private TextView messageTextView;
        private LinearLayout messageWrapper;
        private SimpleDraweeView avatar;

        public MessageHolder(View rootView) {
            super(rootView);
            this.rootView = rootView;
            this.messageTextView = (TextView) rootView.findViewById(R.id.view_message);
            this.messageWrapper = (LinearLayout) rootView.findViewById(R.id.view_message_wrapper);
            this.avatar = (SimpleDraweeView) rootView.findViewById(R.id.view_avatar);
        }

        private void setMessage(Message message) {
            this.message = message;
            setViews();
        }

        private void setViews() {
            this.messageTextView.setText(message.getBody());
            setBackground();
        }

        private void setBackground() {
            if (message.getType() == Conversation.TYPE_USER) {
                ((RelativeLayout.LayoutParams) this.messageWrapper.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_END);
                avatar.setVisibility(View.GONE);
            } else if (message.getType() == Conversation.TYPE_SENDER) {
                ((RelativeLayout.LayoutParams) this.messageWrapper.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_START);
                avatar.setImageURI(contact.getThumbnailUri());
                avatar.setVisibility(View.VISIBLE);
            }
        }
    }
}
