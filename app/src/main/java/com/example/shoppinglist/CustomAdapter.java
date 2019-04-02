package com.example.shoppinglist;

import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.textservice.TextInfo;
import android.widget.TextView;

import java.util.Comparator;
import java.util.List;

import static java.util.Collections.sort;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CustomViewHolder> {
    private int selectedItemsCount;
    private List<ListItem> mDataset;
    private MainActivity mActivity;
    private ActionMode mActionMode;


    public class CustomViewHolder extends RecyclerView.ViewHolder {
        View view;
        TextView mTextView;
        View mainView;
        View swipeView;
        CustomViewHolder(View v) {
            super(v);
            view = v;
            mTextView = v.findViewById(R.id.item_text_view);
            mainView = v.findViewById(R.id.view_main);
            swipeView = v.findViewById(R.id.view_swipe);
        }
    }

    public CustomAdapter(MainActivity activity, List<ListItem> dataset) {
        mDataset = dataset;
        mActivity = activity;
    }

    public void removeItem(int position) {
        mDataset.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(ListItem item, int position) {
        mDataset.add(position, item);
        notifyItemInserted(position);
    }

    public List<ListItem> getDataset() {
        return mDataset;
    }

    public void setDataset(List<ListItem> dataset) {
        mDataset = dataset;
    }

    public void sortItems() {
        sort(mDataset, new Comparator<ListItem>() {
            @Override
            public int compare(ListItem o1, ListItem o2) {
                return o1.getText().compareTo(o2.getText());
            }
        });
        notifyDataSetChanged();
    }

    @Override
    public CustomAdapter.CustomViewHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_recycler_view_item, parent, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CustomViewHolder holder, int position) {
        final ListItem item = mDataset.get(position);
        holder.mTextView.setText(item.getText());
        holder.mTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                holder.mTextView.setCursorVisible(true);
                holder.mTextView.setFocusableInTouchMode(true);
                holder.mTextView.setInputType(InputType.TYPE_CLASS_TEXT);
                holder.mTextView.requestFocus();
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}