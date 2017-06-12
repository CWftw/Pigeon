package com.jameswolfeoliver.pigeon.Listeners;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.jameswolfeoliver.pigeon.SqlWrappers.SqlCallback;
import com.jameswolfeoliver.pigeon.SqlWrappers.Wrapper;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class PaginatedScrollListener<T> extends RecyclerView.OnScrollListener {

    protected final AtomicBoolean loading;
    private final int LOADING_THRESHOLD;
    private final Wrapper<T> loaderManager;
    private int callerId;
    private LinearLayoutManager layoutManager;

    public PaginatedScrollListener(LinearLayoutManager layoutManager,
                                   int callerId,
                                   int LOADING_THRESHOLD,
                                   Wrapper<T> loaderManager) {
        super();
        this.layoutManager = layoutManager;
        this.callerId = callerId;
        this.LOADING_THRESHOLD = LOADING_THRESHOLD;
        this.loaderManager = loaderManager;
        this.loading = new AtomicBoolean(false);
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        synchronized (loading) {
            if (dy < 0 && !loading.get()) {
                int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                int totalItemCount = layoutManager.getItemCount();

                if ((lastVisibleItemPosition + LOADING_THRESHOLD) > totalItemCount) {
                    loading.set(true);
                    paginate();
                }
            }
        }
    }

    public void forcePaginate() {
        loading.set(true);
        paginate();
    }

    protected abstract void paginated(ArrayList<T> object);

    protected abstract void paginating();

    protected void paginate() {
        paginating();
        loaderManager.get(callerId, new SqlCallback<T>() {
            @Override
            public void onQueryComplete(ArrayList<T> results) {
                paginated(results);
            }
        });
    }
}
