package com.tiodev.vegtummy.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tiodev.vegtummy.R;
import java.util.ArrayList;
import java.util.List;

public class CollectionsAdapter extends RecyclerView.Adapter<CollectionsAdapter.ViewHolder> {

    private List<String> collectionNames;
    private final OnCollectionClickListener listener;
    private Context context;

    public interface OnCollectionClickListener {
        void onCollectionClick(String collectionName);
    }

    public CollectionsAdapter(Context context, List<String> collectionNames, OnCollectionClickListener listener) {
        this.context = context;
        this.collectionNames = collectionNames != null ? collectionNames : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_collection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String collectionName = collectionNames.get(position);
        holder.collectionNameTextView.setText(collectionName);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCollectionClick(collectionName);
            }
        });
    }

    @Override
    public int getItemCount() {
        return collectionNames.size();
    }

    public void updateData(List<String> newCollectionNames) {
        this.collectionNames.clear();
        if (newCollectionNames != null) {
            this.collectionNames.addAll(newCollectionNames);
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView collectionNameTextView;

        ViewHolder(View itemView) {
            super(itemView);
            collectionNameTextView = itemView.findViewById(R.id.collection_name_text_view);
        }
    }
}
