package com.tiodev.vegtummy.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.tiodev.vegtummy.R;
// Import WebviewRecipeActivity instead of RecipeActivity
import com.tiodev.vegtummy.WebviewRecipeActivity;
import com.tiodev.vegtummy.RoomDB.User;

import java.util.List;

// Changed class name to FavoritesAdapter
public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.myviewholder>{

    List<User> data;
    Context context;

    // Changed constructor name
    public FavoritesAdapter(List<User> data, Context context) {
        this.data = data;
        this.context = context;
    }

    @NonNull
    @Override
    public myviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Assuming FavoritesFragment uses the same item layout as Popular: popular_list.xml
        // If a different layout is needed for favorites items, this should change.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.popular_list,parent,false);
        return new myviewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull myviewholder holder, int position) {
        final User temp = data.get(holder.getAdapterPosition());

        // Set time - Assuming popular_list.xml has popular_time (txt2)
        holder.txt2.setText("\uD83D\uDD50 "+data.get(holder.getAdapterPosition()).getTotalTime());

        // Load image from link - Assuming popular_list.xml has popular_img (img)
        // And User.getIdentifier() is the correct image source (URL or asset identifier stem)
        Glide.with(holder.txt2.getContext()).load(data.get(holder.getAdapterPosition()).getIdentifier()).into(holder.img);
        Glide.with(holder.img.getContext()).load("file:///android_asset/data/" +data.get(position).getIdentifier() +".jpg").into(holder.img);

        // Set title - Assuming popular_list.xml has popular_txt (txt)
        holder.txt.setText(data.get(holder.getAdapterPosition()).getTitle());

        // Item click listener
        holder.itemView.setOnClickListener(v -> { // Changed from holder.img.setOnClickListener for broader click area
            Intent intent = new Intent(context, WebviewRecipeActivity.class);
            intent.putExtra("id", String.valueOf(temp.getUid()));
            intent.putExtra("tittle", temp.getTitle());
            // Construct the path for WebviewRecipeActivity
            // This matches how SearchAdapter constructs the path.
            String htmlPath = "data/" + temp.getIdentifier() + ".html";
            intent.putExtra("path", htmlPath);

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        if (data == null) {
            return 0;
        }
        return data.size();
    }

    // ViewHolder class - ensure R.id references match popular_list.xml
    static class myviewholder extends RecyclerView.ViewHolder{
        ImageView img;
        TextView txt, txt2; // txt for title, txt2 for time

        public myviewholder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.popular_img);
            txt = itemView.findViewById(R.id.popular_txt);
            txt2 = itemView.findViewById(R.id.popular_time);
        }
    }
}
