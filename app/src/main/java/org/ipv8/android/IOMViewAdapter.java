package org.ipv8.android;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import mehdi.sakout.fancybuttons.FancyButton;

/**
 * {@link RecyclerView.Adapter} that can display
 */
public class IOMViewAdapter extends FilterableRecyclerViewAdapter {
    public static final int VIEW_TYPE_UNKNOWN = 0;
    public static final int VIEW_TYPE_STRING = 1;   // String[] starting with "1"
    public static final int VIEW_TYPE_INPUT = 2;    // String[] starting with "2"
    public static final int VIEW_TYPE_BUTTONS = 3;  // String[] starting with "3"
    public static final int VIEW_TYPE_CLICKABLE_ITEM = 4; // String[] starting with "4"
    public static final int VIEW_TYPE_UNCLICKABLE_ITEM = 5; // String[] starting with "4"

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
        if ((item instanceof String[]) && (((String[]) item)[0].length() == 1)){
            char c = ((String[]) item)[0].charAt(0);
            switch (c){
                case '1':
                    return VIEW_TYPE_STRING;
                case '2':
                    return VIEW_TYPE_INPUT;
                case '3':
                    return VIEW_TYPE_BUTTONS;
                case '4':
                    return VIEW_TYPE_CLICKABLE_ITEM;
                case '5':
                    return VIEW_TYPE_UNCLICKABLE_ITEM;
            }
        }
        return VIEW_TYPE_UNKNOWN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = null;

        if (viewType == VIEW_TYPE_INPUT){
            //view = new EditText(context);
            view = inflater.inflate(R.layout.fragment_input_description, null);
        } else if (viewType == VIEW_TYPE_BUTTONS) {
            view = inflater.inflate(R.layout.fragment_input_button, null);
        } else if (viewType == VIEW_TYPE_CLICKABLE_ITEM) {
            view = inflater.inflate(R.layout.fragment_input_generic, null);
        } else if (viewType == VIEW_TYPE_UNCLICKABLE_ITEM){
            view = inflater.inflate(R.layout.fragment_input_generic_unclickable, null);
        } else {
            TextView text = new TextView(context);
            text.setPadding((int) (parent.getResources().getDisplayMetrics().density * 16), 0, 0, 0);
            text.setText("");
            view = text;
        }

        view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int adapterPosition) {
        Object content = getObject(adapterPosition);
        boolean raw = true;

        if ((content instanceof String[]) && (((String[]) content)[0].length() == 1)){
            raw = false;
            char c = ((String[]) content)[0].charAt(0);
            switch (c){
                case '1':
                    //VIEW_TYPE_STRING;
                    String out = "";
                    for (String s : ((String[]) content)){
                        if (!raw){
                            raw = true;
                        } else {
                            if ("".equals(out)){
                                out += s;
                            } else {
                                out += " " + s;
                            }
                        }
                    }
                    content = out;
                    break;
                case '2':
                    //VIEW_TYPE_INPUT;
                    RelativeLayout topLevelLayout = (RelativeLayout) viewHolder.itemView;
                    ScrollView scrollView = (ScrollView) topLevelLayout.getChildAt(0);
                    GridLayout gridLayout = (GridLayout) scrollView.getChildAt(0);
                    TextView label1 = (TextView) gridLayout.getChildAt(0);
                    TextView label2 = (TextView) gridLayout.getChildAt(1);
                    if (((String[]) content).length > 2){
                        label1.setText(((String[]) content)[1]);
                        label2.setText(((String[]) content)[2]);
                    } else {
                        label1.setVisibility(View.GONE);
                        label2.setText(((String[]) content)[1]);
                    }
                    break;
                case '3':
                    //VIEW_TYPE_BUTTONS;
                    RelativeLayout topLevel = (RelativeLayout) viewHolder.itemView;
                    FancyButton button1 = (FancyButton) topLevel.getChildAt(0);
                    FancyButton button2 = (FancyButton) topLevel.getChildAt(1);
                    button1.setText(((String[]) content)[1]);
                    if (((String[]) content).length > 2){
                        button2.setText(((String[]) content)[2]);
                    } else {
                        button2.setVisibility(View.GONE);
                    }
                    break;
                case '4':
                    // VIEW_TYPE_CLICKABLE_ITEM
                    CardView cardView = (CardView) viewHolder.itemView;
                    RelativeLayout relLayout = (RelativeLayout) cardView.getChildAt(0);
                    TextView title = (TextView) relLayout.getChildAt(0);
                    TextView description = (TextView) relLayout.getChildAt(1);
                    title.setText(((String[]) content)[1]);
                    description.setText(((String[]) content)[2]);
                    List<String> tag = new ArrayList<String>();
                    if (((String[]) content).length > 3) {
                        for (int i = 3; i < ((String[]) content).length; i++) {
                            tag.add(((String[]) content)[i]);
                        }
                    }
                    cardView.setTag(tag);
                    break;
                case '5':
                    // VIEW_TYPE_UNCLICKABLE_ITEM
                    CardView ucardView = (CardView) viewHolder.itemView;
                    RelativeLayout urelLayout = (RelativeLayout) ucardView.getChildAt(0);
                    TextView utitle = (TextView) urelLayout.getChildAt(0);
                    TextView udescription = (TextView) urelLayout.getChildAt(1);
                    utitle.setText(((String[]) content)[1]);
                    udescription.setText(((String[]) content)[2]);
                    break;
                default:
                    raw = true;
            }
        }

        if (raw) {
            TextView textView = (TextView) viewHolder.itemView;
            textView.setText("" + content);
        }
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
