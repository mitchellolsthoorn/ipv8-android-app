package org.ipv8.android;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.TextView;

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
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        TextView text=new TextView(context);
        text.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        text.setText("");

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(text);
        return viewHolder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int adapterPosition) {
        String content = "" + getObject(adapterPosition);

        // Set item views based on your views and data model
        TextView textView = (TextView) viewHolder.itemView;
        textView.setText(content);
    }

    public interface OnClickListener {
    }

    public interface OnSwipeListener {
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
