package com.jameswolfeoliver.pigeon.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jameswolfeoliver.pigeon.Activities.ConversationActivity;
import com.jameswolfeoliver.pigeon.Adapters.InboxAdapter;
import com.jameswolfeoliver.pigeon.Listeners.RecyclerItemClickListener;
import com.jameswolfeoliver.pigeon.Models.Conversation;
import com.jameswolfeoliver.pigeon.R;
import com.jameswolfeoliver.pigeon.SqlWrappers.ConversationWrapper;

import java.util.ArrayList;

import io.reactivex.schedulers.Schedulers;

public class InboxFragment extends Fragment {

    private RecyclerView inbox;
    private InboxAdapter inboxAdapter;
    private ConversationWrapper conversationWrapper;
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.rootView = inflater.inflate(R.layout.fragment_inbox, null);

        this.conversationWrapper = new ConversationWrapper();
        this.inboxAdapter = new InboxAdapter(getContext(), new ArrayList<Conversation>());

        this.inbox = (RecyclerView) rootView.findViewById(R.id.inbox_recycler_view);
        this.inbox.setLayoutManager(new LinearLayoutManager(getContext()));
        this.inbox.setAdapter(inboxAdapter);
        this.inbox.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), inbox, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                goToConversation(inboxAdapter.getConversation(position));
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }
        }));
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        conversationWrapper.fetch()
                .subscribeOn(Schedulers.io())
                .subscribe(conversation -> inbox.post(() -> inboxAdapter.singleUpdate(conversation)));
    }

    public void goToConversation(Conversation conversation) {
        Intent conversationIntent = new Intent(getActivity(), ConversationActivity.class);
        conversationIntent.putExtra(ConversationActivity.CONVERSATION_EXTRA, conversation);
        getActivity().startActivity(conversationIntent);
    }
}
