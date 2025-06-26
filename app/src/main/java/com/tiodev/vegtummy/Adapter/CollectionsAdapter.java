package com.tiodev.vegtummy.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Filter; // Import Filter
import android.widget.Filterable; // Import Filterable
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tiodev.vegtummy.R;
import com.tiodev.vegtummy.Model.CollectionItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CollectionsAdapter extends RecyclerView.Adapter<CollectionsAdapter.ViewHolder> implements Filterable {

    private List<CollectionItem> originalCollectionItems; // Full list
    private List<CollectionItem> filteredCollectionItems; // List to display
    private final OnCollectionClickListener listener;
    private Context context;

    public interface OnCollectionClickListener {
        void onCollectionClick(CollectionItem collectionItem);
    }

    public CollectionsAdapter(Context context, List<CollectionItem> collectionItems, OnCollectionClickListener listener) {
        this.context = context;
        this.originalCollectionItems = new ArrayList<>(collectionItems); // Initialize original list
        this.filteredCollectionItems = new ArrayList<>(collectionItems); // Initialize filtered list
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
        CollectionItem collectionItem = filteredCollectionItems.get(position); // Use filtered list
        holder.collectionNameTextView.setText(collectionItem.getTitle());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCollectionClick(collectionItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredCollectionItems.size(); // Return size of filtered list
    }

    public void updateData(List<CollectionItem> newCollectionItems) {
        this.originalCollectionItems.clear();
        this.originalCollectionItems.addAll(newCollectionItems);
        this.filteredCollectionItems.clear();
        this.filteredCollectionItems.addAll(newCollectionItems);
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString().toLowerCase(Locale.getDefault()).trim();
                List<CollectionItem> tempList = new ArrayList<>();
                if (charString.isEmpty()) {
                    tempList.addAll(originalCollectionItems);
                } else {
                    for (CollectionItem item : originalCollectionItems) {
                        if (item.getTitle().toLowerCase(Locale.getDefault()).contains(charString)) {
                            tempList.add(item);
                        }
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = tempList;
                filterResults.count = tempList.size(); // Important for the fragment to get the count
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredCollectionItems.clear();
                if (results.values != null) {
                    filteredCollectionItems.addAll((List<CollectionItem>) results.values);
                }
                notifyDataSetChanged();
                // The fragment will call getItemCount() on the adapter to update its own count TextView
            }
        };
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView collectionNameTextView;

        ViewHolder(View itemView) {
            super(itemView);
            collectionNameTextView = itemView.findViewById(R.id.collection_name_text_view);
        }
    }
}
