package com.tiodev.vegtummy.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tiodev.vegtummy.FavoriteList;
import com.tiodev.vegtummy.R;
import java.util.List;
import java.util.Locale;

public class FavoriteListNameAdapter extends RecyclerView.Adapter<FavoriteListNameAdapter.ViewHolder> {

    private List<FavoriteList> favoriteLists;
    private final Context context;
    private final OnFavoriteListClickListener clickListener;

    public interface OnFavoriteListClickListener {
        void onFavoriteListClick(FavoriteList favoriteList);
        void onFavoriteListLongClick(FavoriteList favoriteList, View view); // For context menu
    }

    public FavoriteListNameAdapter(Context context, List<FavoriteList> favoriteLists, OnFavoriteListClickListener listener) {
        this.context = context;
        this.favoriteLists = favoriteLists;
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorite_list_name, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FavoriteList favoriteList = favoriteLists.get(position);
        holder.listNameTextView.setText(favoriteList.getName());
        int recipeCount = favoriteList.getRecipeIds().size();
        holder.recipeCountTextView.setText(String.format(Locale.getDefault(), "(%d %s)", recipeCount, recipeCount == 1 ? "recipe" : "recipes"));

        holder.itemView.setOnClickListener(v -> clickListener.onFavoriteListClick(favoriteList));
        holder.itemView.setOnLongClickListener(v -> {
            clickListener.onFavoriteListLongClick(favoriteList, v);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return favoriteLists == null ? 0 : favoriteLists.size();
    }

    public void updateData(List<FavoriteList> newFavoriteLists) {
        this.favoriteLists.clear();
        if (newFavoriteLists != null) {
            this.favoriteLists.addAll(newFavoriteLists);
        }
        notifyDataSetChanged();
    }

    public FavoriteList getItem(int position) {
        if (favoriteLists != null && position >= 0 && position < favoriteLists.size()) {
            return favoriteLists.get(position);
        }
        return null;
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView listNameTextView;
        TextView recipeCountTextView;

        ViewHolder(View itemView) {
            super(itemView);
            listNameTextView = itemView.findViewById(R.id.list_name_text_view);
            recipeCountTextView = itemView.findViewById(R.id.list_recipe_count_text_view);
        }
    }
}
