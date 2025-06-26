package com.tiodev.vegtummy.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
// Intent and File are no longer needed here for launching WebviewRecipeActivity directly
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.tiodev.vegtummy.R;
// RecipeActivity and WebviewRecipeActivity are no longer directly launched from here
import com.tiodev.vegtummy.RoomDB.User;

import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.Searchviewholder> {

    List<User> data;
    Context context; // Retain context for Glide
    private final OnRecipeClickListener clickListener;

    // Interface for click events
    public interface OnRecipeClickListener {
        void onRecipeClicked(User recipe);
    }

    public SearchAdapter(List<User> data, Context context, OnRecipeClickListener listener) {
        this.data = data;
        this.context = context;
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public Searchviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_list, parent, false);
        return new Searchviewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Searchviewholder holder, int position) {
        final User temp = data.get(position);

        // Load image using Glide
        Glide.with(holder.img.getContext())
                .load("file:///android_asset/data/" + temp.getIdentifier() + ".jpg")
                .into(holder.img);
        holder.txt.setText(temp.getTitle());

        // Set click listener on the item view
        holder.item.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onRecipeClicked(temp);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filterList(List<User> filterList) {
        data = filterList;
        notifyDataSetChanged();
    }

    static class Searchviewholder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView txt;
        ConstraintLayout item;

        public Searchviewholder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.search_img);
            txt = itemView.findViewById(R.id.search_txt);
            item = itemView.findViewById(R.id.search_item);
        }
    }
}
