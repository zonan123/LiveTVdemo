package com.android.tv.license;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.mediatek.wwtv.tvcenter.R;

public class LicenseRecyclerAdapter extends PagedListAdapter<String, LicenseRecyclerAdapter.RecyclerViewHolder> {


    public LicenseRecyclerAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.web_notice, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
        holder.mTitleTextView.setText(getItem(position));
    }

    public static class RecyclerViewHolder extends RecyclerView.ViewHolder {

        TextView mTitleTextView;
        public RecyclerViewHolder(View itemView) {
            super(itemView);
            mTitleTextView = itemView.findViewById(R.id.content_item);
        }
    }

    private static DiffUtil.ItemCallback<String> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<String>() {
                @Override
                public boolean areItemsTheSame(String oldConcert, String newConcert) {
                    return oldConcert.equals(newConcert);
                }

                @Override
                public boolean areContentsTheSame(String oldConcert,
                                                  String newConcert) {
                    return oldConcert.equals(newConcert);
                }
            };
}
