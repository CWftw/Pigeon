package com.jameswolfeoliver.pigeon.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.jameswolfeoliver.pigeon.Models.Contact;
import com.jameswolfeoliver.pigeon.Models.Conversation;
import com.jameswolfeoliver.pigeon.Models.Message;
import com.jameswolfeoliver.pigeon.R;
import com.jameswolfeoliver.pigeon.Utilities.PigeonApplication;
import com.jameswolfeoliver.pigeon.Utilities.Utils;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.BaseHolder> {

    private static final int TYPE_MESSAGE = 0;
    private static final int TYPE_LOADING = 1;

    private ArrayList<Message> dataSet;
    private Context context;
    private Contact contact;

    public MessageAdapter(Context context, ArrayList<Message> dataSet, Contact contact) {
        this.context = context;
        this.dataSet = dataSet;
        this.contact = contact;
    }

    public void append(List<Message> messages) {
        int rangeStart = this.dataSet.size();
        this.dataSet.addAll(messages);
        int rangeEnd = this.dataSet.size() - 1;
        if (rangeStart != rangeEnd) notifyItemRangeInserted(rangeStart, rangeEnd);
    }

    public int appendLoading() {
        this.dataSet.add(null);
        int loadingIndex = dataSet.size() - 1;
        notifyItemInserted(loadingIndex);
        return loadingIndex;
    }

    public void removeLoading(int index) {
        this.dataSet.remove(index);
        notifyItemRemoved(index);
    }

    public void filteredPrepend(List<Message> messages) {
        int rangeStart = 0;
        int rangeEnd = 0;
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).equals(dataSet.get(0))) {
                break;
            } else {
                rangeEnd++;
                dataSet.add(i, messages.get(i));
            }
        }
        if (rangeStart != rangeEnd) notifyItemRangeInserted(rangeStart, rangeEnd);
    }

    public void onNewMessage(Message newMessage) {
        this.dataSet.add(0, newMessage);
        notifyItemInserted(0);
    }

    @Override
    public BaseHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            default:
            case TYPE_MESSAGE:
                return new MessageHolder(LayoutInflater.from(context).inflate(R.layout.view_message_item, parent, false), viewType);
            case TYPE_LOADING:
                return new BaseHolder(LayoutInflater.from(context).inflate(R.layout.view_loading_item, parent, false), viewType);
        }
    }

    @Override
    public void onBindViewHolder(BaseHolder baseHolder, int position) {
        if (baseHolder.getType() == TYPE_MESSAGE && baseHolder instanceof MessageHolder) {
            MessageHolder holder = ((MessageHolder) baseHolder);
            if (dataSet.get(position + 1) != null) {
                holder.setMessage(dataSet.get(position), contact, dataSet.get(position + 1).getType());
            } else {
                holder.setMessage(dataSet.get(position), contact, -2);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return dataSet.get(position) != null ? TYPE_MESSAGE : TYPE_LOADING;
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public static class BaseHolder extends RecyclerView.ViewHolder {
        protected final int type;

        public BaseHolder(View rootView, int type) {
            super(rootView);
            this.type = type;
        }

        public int getType() {
            return type;
        }
    }

    public static class MessageHolder extends BaseHolder {
        private Message message;
        private TextView messageTextView;
        private LinearLayout messageWrapper;
        private SimpleDraweeView avatar;

        public MessageHolder(View rootView, int type) {
            super(rootView, type);
            this.messageTextView = (TextView) rootView.findViewById(R.id.view_message);
            this.messageWrapper = (LinearLayout) rootView.findViewById(R.id.view_message_wrapper);
            this.avatar = (SimpleDraweeView) rootView.findViewById(R.id.view_avatar);
        }


        private void setMessage(Message message, Contact contact, int NEXT_MESSAGE_TYPE) {
            this.message = message;
            setViews(contact, NEXT_MESSAGE_TYPE);
        }

        private void setViews(Contact contact, int NEXT_MESSAGE_TYPE) {
            this.messageTextView.setText(message.getBody());
            if (message.getType() == Conversation.TYPE_USER) {
                messageTextView.setBackgroundResource(R.drawable.bg_message_bubble_sent);
                ((FrameLayout.LayoutParams) this.messageWrapper.getLayoutParams()).gravity = Gravity.END;
                messageTextView.setTextColor(PigeonApplication.getAppContext().getResources().getColor(R.color.textColour));
                avatar.setVisibility(View.GONE);
            } else if (message.getType() == Conversation.TYPE_SENDER) {
                messageTextView.setBackgroundResource(R.drawable.bg_message_bubble);
                ((FrameLayout.LayoutParams) this.messageWrapper.getLayoutParams()).gravity = Gravity.START;
                messageTextView.setTextColor(PigeonApplication.getAppContext().getResources().getColor(R.color.colorWhite));
                if (message.getType() == NEXT_MESSAGE_TYPE) {
                    avatar.setVisibility(View.GONE);
                } else {
                    avatar.setImageURI(contact.getThumbnailUri());
                    avatar.setVisibility(View.VISIBLE);
                }
            }
            if (message.getType() == NEXT_MESSAGE_TYPE) {
                ((FrameLayout.LayoutParams) messageWrapper.getLayoutParams()).topMargin = Utils.convertToPixels(2);
                if (message.getType() == Conversation.TYPE_SENDER) {
                    ((LinearLayout.LayoutParams) messageTextView.getLayoutParams()).leftMargin = Utils.convertToPixels(56);
                } else {
                    ((LinearLayout.LayoutParams) messageTextView.getLayoutParams()).leftMargin = Utils.convertToPixels(8);
                }
            } else {
                ((LinearLayout.LayoutParams) messageTextView.getLayoutParams()).leftMargin = Utils.convertToPixels(8);
                ((FrameLayout.LayoutParams) messageWrapper.getLayoutParams()).topMargin = Utils.convertToPixels(10);
            }
        }
    }
}
