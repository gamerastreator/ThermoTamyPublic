package com.tiodev.vegtummy.Adapter;

import android.annotation.SuppressLint;
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
import com.tiodev.vegtummy.RecipeActivity;
import com.tiodev.vegtummy.RoomDB.User;
import com.tiodev.vegtummy.WebviewRecipeFragment;

import java.util.List;

public class AdapterPopular extends RecyclerView.Adapter<AdapterPopular.myviewholder>{

    List<User> data;
    Context context;
    private final OnRecipeClickListener clickListener;

    // Interface for click events
    public interface OnRecipeClickListener {
        void onRecipeClicked(User recipe);
    }
    public AdapterPopular(List<User> data, Context context, OnRecipeClickListener listener) {
        this.data = data;
        this.context = context;
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public myviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.popular_list,parent,false);
        return new myviewholder(view);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filterList(List<User> filterList) {
        data = filterList;
        notifyDataSetChanged();
    }
    @Override
    public void onBindViewHolder(@NonNull myviewholder holder, int position) {
        final User temp = data.get(holder.getAdapterPosition());


        // Set time
        holder.txt2.setText("\uD83D\uDD50 "+data.get(holder.getAdapterPosition()).getTotalTime());
        // Load image from link
        Glide.with(holder.txt2.getContext()).load(data.get(holder.getAdapterPosition()).getIdentifier()).into(holder.img);
        Glide.with(holder.img.getContext()).load("file:///android_asset/data/" +data.get(position).getIdentifier() +".jpg").into(holder.img);

        // Set title
        holder.txt.setText(data.get(holder.getAdapterPosition()).getTitle());

        holder.img.setOnClickListener(v ->{
            if (clickListener != null) {
                clickListener.onRecipeClicked(temp);
            }
           /*Intent intent = new Intent(context, RecipeActivity.class);
            intent.putExtra("id", String.valueOf(temp.getUid())); // Pass the recipe ID
            intent.putExtra("img", temp.getIdentifier()); // Assuming 'identifier' is the image URL
            intent.putExtra("tittle", temp.getTitle());
            intent.putExtra("des", temp.getRecipeYieldText()); // Assuming 'des' could be recipeYieldText or similar
            intent.putExtra("ing", temp.getKeywords());*/ // Assuming 'ing' could be keywords or similar, this needs clarification
                                                       // Or it might be a field that is not directly in User object but fetched separately.
                                                       // For now, using keywords as a placeholder.
                                                       // The original RecipeActivity splits "ing" by "\n"
            // It seems 'des' (description) and 'ing' (ingredients) were not fully passed before.
            // Need to ensure RecipeActivity gets all necessary data.
            // For 'ing', RecipeActivity expects a newline-separated string.
            // For 'des', RecipeActivity expects the steps.
            // These might not be directly available in the User object as single strings.
            // This part needs careful review of how RecipeActivity consumes 'des' and 'ing'.
            // For now, I'm passing placeholders. This might be an issue later.
            // The User object has fields like totalTime, cookTime, prepTime, difficulty, rating, category etc.
            // but not a direct multi-line 'ingredients' string or 'steps' string.
            // This is a significant gap. RecipeActivity seems to expect these.

            // Let's assume for now that the existing RecipeActivity's data loading for ing and des
            // from intent extras like getIntent().getStringExtra("ing") and getIntent().getStringExtra("des")
            // will need to be reconciled with what the User object actually provides.
            // The original code in RecipeActivity:
            // ingList = getIntent().getStringExtra("ing").split("\n");
            // steps.setText(getIntent().getStringExtra("des"));
            // This implies "ing" and "des" were passed as strings from somewhere.
            // The User object doesn't seem to have these directly.
            // This is a pre-existing issue or a misunderstanding of data flow.

            // For the favorite functionality, "id" is the most critical part.
            // I will use placeholder values for "des" and "ing" for now if not directly available.
            // It appears the database schema (User entity) and RecipeActivity's expectations for "ing" and "des" are not aligned.
            // I will pass title for des and category for ing as temporary placeholders to avoid nulls,
            // this will likely not show correct data in RecipeActivity for these fields.
            /*intent.putExtra("des", temp.getTitle()); // Placeholder for description/steps
            intent.putExtra("ing", temp.getCategory()); // Placeholder for ingredients, split by \n in RecipeActivity

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);*/
        });



    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    static class myviewholder extends RecyclerView.ViewHolder{

        ImageView img;
        TextView txt, txt2;
        public myviewholder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.popular_img);
            txt = itemView.findViewById(R.id.popular_txt);
            txt2 = itemView.findViewById(R.id.popular_time);
        }
    }
}
