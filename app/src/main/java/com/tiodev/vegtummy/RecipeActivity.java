package com.tiodev.vegtummy;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

public class RecipeActivity extends AppCompatActivity {

    ImageView img, backBtn, overlay, scroll, zoomImage;
    ImageButton favoriteBtn; // Added favorite button
    TextView txt, ing, time, steps;
    String [] ingList;
    String recipeId; // Added to store recipe ID
    Button stepBtn, ing_btn;
    boolean isImgCrop = false;
    ScrollView scrollView, scrollView_step;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        // Find views
        img = findViewById(R.id.recipe_img);
        txt = findViewById(R.id.tittle);
        ing = findViewById(R.id.ing);
        time = findViewById(R.id.time);
        stepBtn = findViewById(R.id.steps_btn);
        ing_btn = findViewById(R.id.ing_btn);
        backBtn = findViewById(R.id.back_btn);
        steps = findViewById(R.id.steps_txt);
        scrollView = findViewById(R.id.ing_scroll);
        scrollView_step = findViewById(R.id.steps);
        overlay = findViewById(R.id.image_gradient);
        scroll = findViewById(R.id.scroll);
        zoomImage = findViewById(R.id.zoom_image);
        favoriteBtn = findViewById(R.id.favorite_btn); // Initialize favorite button

        // Get Recipe ID - Assuming it's passed as "id"
        // IMPORTANT: This needs to be confirmed. If the ID is passed with a different key,
        // or if it's part of a serialized object, this line needs to change.
        // For now, let's assume "id" is a string. If it's an int from Room, it might be passed as int or long.
        recipeId = getIntent().getStringExtra("id");
        if (recipeId == null || recipeId.isEmpty()) {
            // Fallback or error handling if ID is not passed correctly
            // For now, let's try to use title as a fallback, though this is not ideal if titles aren't unique
            recipeId = getIntent().getStringExtra("tittle");
            if (recipeId == null || recipeId.isEmpty()) {
                Toast.makeText(this, "Recipe ID not found, favoriting may not work correctly.", Toast.LENGTH_LONG).show();
                // Disable favorite button if no usable ID
                favoriteBtn.setEnabled(false);
            }
        }


        // Load recipe image from link
        Glide.with(getApplicationContext()).load(getIntent().getStringExtra("img"))
                                                    .into(img);
        // Set recipe title
        txt.setText(getIntent().getStringExtra("tittle"));

        // Set recipe ingredients
        ingList = getIntent().getStringExtra("ing").split("\n");
        // Set time
        time.setText(ingList[0]);


        for (int i = 1; i<ingList.length; i++){
            ing.setText(ing.getText()+"\uD83D\uDFE2  "+ingList[i]+"\n");
        }
        // Set recipe steps
        steps.setText(getIntent().getStringExtra("des"));

        // Setup Favorite Button
        if (recipeId != null && !recipeId.isEmpty()) {
            setFavoriteButtonState();
            favoriteBtn.setOnClickListener(v -> toggleFavoriteStatus());
        }

        stepBtn.setBackground(null);

        stepBtn.setOnClickListener(v -> {
            stepBtn.setBackgroundResource(R.drawable.btn_ing);
            stepBtn.setTextColor(getColor(R.color.white));
            ing_btn.setBackground(null);
            ing_btn.setTextColor(getColor(R.color.black));


            scrollView.setVisibility(View.GONE);
            scrollView_step.setVisibility(View.VISIBLE);



//            ing.setText(getIntent().getStringExtra("des"));


        });

        ing_btn.setOnClickListener(v -> {
            ing_btn.setBackgroundResource(R.drawable.btn_ing);
            ing_btn.setTextColor(getColor(R.color.white));
            stepBtn.setBackground(null);
            stepBtn.setTextColor(getColor(R.color.black));

            scrollView.setVisibility(View.VISIBLE);
            scrollView_step.setVisibility(View.GONE);

        });


        // Full recipe image button
        zoomImage.setOnClickListener(view ->{

            if(!isImgCrop){
                img.setScaleType(ImageView.ScaleType.FIT_CENTER);
                overlay.setImageAlpha(0);
                Glide.with(getApplicationContext()).load(getIntent().getStringExtra("img"))
                        .into(img);
                isImgCrop = true;

            }else{
                img.setScaleType(ImageView.ScaleType.CENTER_CROP);
                overlay.setImageAlpha(255);
                Glide.with(getApplicationContext()).load(getIntent().getStringExtra("img"))
                        .into(img);
                isImgCrop = false;

            }

        });


        // Exit activity
        backBtn.setOnClickListener(v -> finish());
    }

    private void setFavoriteButtonState() {
        if (FavoriteManager.isFavorite(this, recipeId)) {
            favoriteBtn.setImageResource(R.drawable.ic_favorite_filled); // Assuming filled heart drawable
        } else {
            favoriteBtn.setImageResource(R.drawable.ic_favorite_border); // Assuming empty heart drawable
        }
    }

    private void toggleFavoriteStatus() {
        if (FavoriteManager.isFavorite(this, recipeId)) {
            FavoriteManager.removeFavorite(this, recipeId);
            favoriteBtn.setImageResource(R.drawable.ic_favorite_border);
            Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
        } else {
            FavoriteManager.addFavorite(this, recipeId);
            favoriteBtn.setImageResource(R.drawable.ic_favorite_filled);
            Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show();
        }
    }
}