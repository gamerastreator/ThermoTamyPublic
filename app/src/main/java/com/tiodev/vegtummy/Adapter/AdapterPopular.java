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

public class AdapterPopular extends RecyclerView.Adapter<AdapterPopular.myviewholder>{

    List<User> data;
    Context context;

    public AdapterPopular(List<User> data, Context context) {
        this.data = data;
        this.context = context;
    }

    @NonNull
    @Override
    public myviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.popular_list,parent,false);
        return new myviewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull myviewholder holder, int position) {
        final User temp = data.get(holder.getAdapterPosition());


        // Set time
        holder.txt2.setText("\uD83D\uDD50 "+data.get(holder.getAdapterPosition()).getTotalTime());
        // Load image from link
        Glide.with(holder.txt2.getContext()).load(data.get(holder.getAdapterPosition()).getIdentifier()).into(holder.img);
        // Set title
        holder.txt.setText(data.get(holder.getAdapterPosition()).getTitle());

        holder.img.setOnClickListener(v ->{
            Intent intent = new Intent(context, RecipeActivity.class);
            //intent.putExtra("img", temp.getImg());
            intent.putExtra("tittle", temp.getTitle());
            //intent.putExtra("des", temp.getDes());
            //intent.putExtra("ing", temp.getIng());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
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
