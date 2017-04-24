package com.jameswolfeoliver.pigeon.Activities;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.jameswolfeoliver.pigeon.Adapters.MessageAdapter;
import com.jameswolfeoliver.pigeon.Listeners.PaginatedScrollListener;
import com.jameswolfeoliver.pigeon.Managers.UserCacheManager;
import com.jameswolfeoliver.pigeon.R;
import com.jameswolfeoliver.pigeon.Server.Models.Contact;
import com.jameswolfeoliver.pigeon.Server.Models.Conversation;
import com.jameswolfeoliver.pigeon.Server.Models.Message;
import com.jameswolfeoliver.pigeon.SqlWrappers.MessagesWrapper;
import com.jameswolfeoliver.pigeon.Utilities.PigeonApplication;

import java.util.ArrayList;

public class ConversationActivity extends AppCompatActivity
        implements View.OnFocusChangeListener {
    public static final String CONVERSATION_EXTRA = "extra_thread_id";

    private AppCompatEditText chatEditText;
    private RecyclerView messageRecyclerView;
    private PaginatedScrollListener<Message> paginatedScrollListener;
    private MessagesWrapper messagesWrapper;
    private MessageAdapter messageAdapter;
    private Conversation conversation;
    private Contact contact;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        conversation = (getIntent().getExtras() != null) ? (Conversation) getIntent().getSerializableExtra(CONVERSATION_EXTRA) : null;
        if (conversation == null) {
            Toast.makeText(PigeonApplication.getAppContext(), "An error occurred", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_conversation);
        contact = UserCacheManager.getInstance().getContact(conversation.getAddress());

        // Setup views
        this.chatEditText = (AppCompatEditText) findViewById(R.id.action_message);
        this.chatEditText.setOnFocusChangeListener(this);
        this.messageRecyclerView = (RecyclerView) findViewById(R.id.action_view_messages);

        getSupportActionBar().setTitle(contact.getName());
        getSupportActionBar().setHomeButtonEnabled(true);

        // Setup data
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true);
        this.messageRecyclerView.setLayoutManager(linearLayoutManager);
        this.messagesWrapper = new MessagesWrapper(this, Integer.toString(conversation.getThreadId()));
        this.messageAdapter = new MessageAdapter(ConversationActivity.this, new ArrayList<Message>(), contact);
        this.messageRecyclerView.setAdapter(messageAdapter);

        this.paginatedScrollListener = new PaginatedScrollListener<Message>(linearLayoutManager, 5, messagesWrapper) {
            @Override
            protected void paginated(final ArrayList<Message> messages) {
                if (!messages.isEmpty()) {
                    messageRecyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            messageAdapter.update(messages);
                            loading.set(false);
                        }
                    });
                }
                Log.d("James", "Paginated");
            }

            @Override
            protected void paginating() {
                Log.d("James", "Paginating...");
            }
        };
        messageRecyclerView.addOnScrollListener(paginatedScrollListener);

        // Manually do first call
        paginatedScrollListener.forcePaginate();
    }

    private void showSoftKeyboard() {
        if (chatEditText.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(chatEditText, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        switch (view.getId()) {
            case R.id.action_message:
                Log.d("James", "Edit text focused? " + Boolean.toString(b));
        }
    }
}
