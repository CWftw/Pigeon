package com.jameswolfeoliver.pigeon.Listeners;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.jameswolfeoliver.pigeon.SqlWrappers.Wrapper;

import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.schedulers.Schedulers;

public abstract class PaginatedScrollListener<T> extends RecyclerView.OnScrollListener {

    protected final AtomicBoolean loading;
    private final int LOADING_THRESHOLD;
    private final Wrapper<T> loaderManager;
    private LinearLayoutManager layoutManager;

    public PaginatedScrollListener(LinearLayoutManager layoutManager, int LOADING_THRESHOLD, Wrapper<T> loaderManager) {
        super();
        this.layoutManager = layoutManager;
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

    protected abstract void paginated(T object);

    protected abstract void paginating();

    protected void paginate() {
        paginating();
        loaderManager.fetch().subscribeOn(Schedulers.io())
                .subscribe(PaginatedScrollListener.this::paginated);
    }
}
