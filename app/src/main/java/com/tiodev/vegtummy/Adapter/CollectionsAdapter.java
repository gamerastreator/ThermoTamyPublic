package com.tiodev.vegtummy.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tiodev.vegtummy.R;
import com.tiodev.vegtummy.Model.CollectionItem; // Import CollectionItem
import java.util.ArrayList;
import java.util.List;

public class CollectionsAdapter extends RecyclerView.Adapter<CollectionsAdapter.ViewHolder> {

    private List<CollectionItem> collectionItems; // Changed from List<String>
    private final OnCollectionClickListener listener;
    private Context context;

    public interface OnCollectionClickListener {
        void onCollectionClick(CollectionItem collectionItem); // Changed parameter
    }

    public CollectionsAdapter(Context context, List<CollectionItem> collectionItems, OnCollectionClickListener listener) {
        this.context = context;
        this.collectionItems = collectionItems != null ? collectionItems : new ArrayList<>();
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
        CollectionItem collectionItem = collectionItems.get(position); // Get CollectionItem
        holder.collectionNameTextView.setText(collectionItem.getTitle()); // Display title
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCollectionClick(collectionItem); // Pass CollectionItem
            }
        });
    }

    @Override
    public int getItemCount() {
        return collectionItems.size(); // Use collectionItems
    }

    public void updateData(List<CollectionItem> newCollectionItems) { // Changed parameter type
        this.collectionItems.clear();
        if (newCollectionItems != null) {
            this.collectionItems.addAll(newCollectionItems);
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
