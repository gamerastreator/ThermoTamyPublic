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
import com.tiodev.vegtummy.RecipeActivity;
import com.tiodev.vegtummy.RoomDB.User;

import java.util.List;

public class Adaptar extends  RecyclerView.Adapter<Adaptar.myviewHolder>{

//    List<ResModel> data;
//    Context context;

    List<User> data;
    Context context;
//    ArrayList<String> data = new ArrayList<>();
//
//    public Adaptar(ArrayList<String> data) {
//        this.data = data;
//    }

    public Adaptar(List<User> data , Context context) {

        this.data = data;
        this.context = context;
    }

    @NonNull
    @Override
    public myviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
         View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_design,parent,false);
         return new myviewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull myviewHolder holder, int position) {

        final User temp = data.get(position);

        holder.txt1.setText(data.get(position).getTitle());
        Glide.with(holder.txt1.getContext()).load(data.get(position).getIdentifier()).into(holder.img);
        Glide.with(holder.img.getContext()).load("file:///android_asset/data/" +data.get(position).getIdentifier() +".jpg").into(holder.img);

        holder.img2.setOnClickListener(v -> {

            Intent intent = new Intent(context, RecipeActivity.class);
            intent.putExtra("id", String.valueOf(temp.getUid())); // Pass the recipe ID
            intent.putExtra("img", temp.getIdentifier()); // Assuming 'identifier' is the image URL
            intent.putExtra("tittle", temp.getTitle());
            // See comments in AdapterPopular.java regarding "des" and "ing"
            // Using placeholders for now.
            intent.putExtra("des", temp.getTitle()); // Placeholder
            intent.putExtra("ing", temp.getCategory()); // Placeholder
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);


        });



    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    static class myviewHolder extends RecyclerView.ViewHolder{
        ImageView img , img2;
        TextView txt1;

        public myviewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.img);
            img2 = itemView.findViewById(R.id.card_btn);
            txt1 = itemView.findViewById(R.id.txt1);
        }
    }



}
