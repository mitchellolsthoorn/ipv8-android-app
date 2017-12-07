package org.ipv8.android;

import android.text.TextUtils;
import android.widget.Filter;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Filter bank accounts
 */
public class IOMViewAdapterFilter extends Filter {

    private final FilterableRecyclerViewAdapter _adapter;

    public IOMViewAdapterFilter(FilterableRecyclerViewAdapter adapter) {
        super();
        _adapter = adapter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        // Get a copy of the data to avoid concurrency issues while iterating over it
        HashSet<Object> data = _adapter.getData();
        Collection<Object> filtered = new LinkedList<>();
        // Sanitize query
        String query = constraint.toString().trim().toLowerCase();
        if (TextUtils.isEmpty(query)) {
            // Show all
            filtered = data;
        }
        // Filter by name and description
        else {
            for (Object object : data) {
                // TODO
            }
        }
        CollectionFilterResults results = new CollectionFilterResults();
        results.collection = filtered;
        return results;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void publishResults(CharSequence constraint, FilterResults filterResults) {
        if (filterResults instanceof CollectionFilterResults) {
            CollectionFilterResults results = (CollectionFilterResults) filterResults;
            _adapter.onFilterResults(results.collection);
        }
    }

    protected static class CollectionFilterResults extends FilterResults {

        public Collection<Object> collection;
    }
}
