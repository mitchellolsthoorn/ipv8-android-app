package org.ipv8.android;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.Filter;

import java.util.Collection;

/**
 * {@link RecyclerView.Adapter} that can display
 */
public class IOMViewAdapter extends FilterableRecyclerViewAdapter {
    public static final int VIEW_TYPE_UNKNOWN = 0;

    private OnClickListener _clickListener;
    private final IOMViewAdapterFilter _filter;
    private final ListFragment _listFragment;

    public IOMViewAdapter(Collection<Object> objects, ListFragment listFragment) {
        super(objects);
        _filter = new IOMViewAdapterFilter(this);
        _listFragment = listFragment;
    }

    @Nullable
    public OnClickListener getClickListener() {
        return _clickListener;
    }

    /**
     * @param listener OnClickListener that will listen to the view items being clicked
     */
    public void setClickListener(@Nullable OnClickListener listener) {
        _clickListener = listener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Filter getFilter() {
        return _filter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemViewType(int adapterPosition) {
        Object item = getObject(adapterPosition);
        return VIEW_TYPE_UNKNOWN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // TODO
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int adapterPosition) {
        // TODO
    }

    public interface OnClickListener {
    }

    public interface OnSwipeListener {
    }
}
