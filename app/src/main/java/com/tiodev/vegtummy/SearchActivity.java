package com.tiodev.vegtummy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.tiodev.vegtummy.Adapter.SearchAdapter;
import com.tiodev.vegtummy.RoomDB.AppDatabase;
import com.tiodev.vegtummy.RoomDB.User;
import com.tiodev.vegtummy.RoomDB.UserDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class SearchActivity extends AppCompatActivity {

    EditText search;
    ImageView back_btn;
    RecyclerView rcview;
    List<User> dataPopular = new ArrayList<>();
    SearchAdapter adapter;
    List<User> recipes;
    TextView results;

    ChipGroup dificultad, tiempoPreparacion, porciones, version, tiempoTotal, rating;
    String filterText = "";

    List<String> dificultyFilter = new ArrayList<String>();
    List<String> tiempoPreparacionFilter = new ArrayList<String>();
    List<String> porcionesFilter = new ArrayList<String>();
    List<String> versionFilter = new ArrayList<String>();
    List<String> tiempoTotalFilter = new ArrayList<String>();
    List<String> ratingFilter = new ArrayList<String>();
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Find views
        search = findViewById(R.id.search);
        back_btn = findViewById(R.id.back_to_home);
        rcview = findViewById(R.id.rcview);
        results = findViewById((R.id.results));
        dificultad = findViewById((R.id.dificultad));
        tiempoPreparacion = findViewById((R.id.tiempoPreparacion));
        porciones = findViewById((R.id.porciones));
        version= findViewById((R.id.version));
        tiempoTotal = findViewById((R.id.tiempoTotal));
        rating = findViewById((R.id.rating));

        // Show and focus the keyboard
        search.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);


        // Get database
        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, "db_name").allowMainThreadQueries()
                .createFromAsset("database/recipe.db")
                .fallbackToDestructiveMigration()
                .build();
        UserDao userDao = db.userDao();

        // Get all recipes from database
        recipes = userDao.getAll();

        // Filter the Popular category on activity start
        /*for(int i = 0; i<recipes.size(); i++){
            if(recipes.get(i).getCategory().contains("Popular")){
                dataPopular.add(recipes.get(i));
            }
        }*/

        // Set layout manager to recyclerView
        rcview.setLayoutManager(new LinearLayoutManager(this));

        // Hide keyboard when recyclerView item clicked
        rcview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
                return false;
            }
        });

        dificultad.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
              @Override
              public void onCheckedChanged(ChipGroup group, int checkedId) {
                  dificultyFilter = new ArrayList<>();
                  for( int i = 0; i< group.getCheckedChipIds().size(); i++) {
                      dificultyFilter.add("" + ((Chip) findViewById(group.getCheckedChipIds().get(i))).getText());
                  }
                  filter();

              }
          });
        tiempoPreparacion.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                tiempoPreparacionFilter = new ArrayList<>();
                for( int i = 0; i< group.getCheckedChipIds().size(); i++) {
                    tiempoPreparacionFilter.add("" + ((Chip) findViewById(group.getCheckedChipIds().get(i))).getText());
                }
                filter();

            }
        });
        tiempoTotal.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                tiempoTotalFilter = new ArrayList<>();
                for( int i = 0; i< group.getCheckedChipIds().size(); i++) {
                    tiempoTotalFilter.add("" + ((Chip) findViewById(group.getCheckedChipIds().get(i))).getText());
                }
                filter();

            }
        });
        porciones.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                porcionesFilter = new ArrayList<>();
                for( int i = 0; i< group.getCheckedChipIds().size(); i++) {
                    porcionesFilter.add("" + ((Chip) findViewById(group.getCheckedChipIds().get(i))).getText());
                }
                filter();

            }
        });
        version.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                versionFilter = new ArrayList<>();
                for( int i = 0; i< group.getCheckedChipIds().size(); i++) {
                    versionFilter.add("" + ((Chip) findViewById(group.getCheckedChipIds().get(i))).getText());
                }
                filter();

            }
        });
        rating.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                ratingFilter = new ArrayList<>();
                for( int i = 0; i< group.getCheckedChipIds().size(); i++) {
                    ratingFilter.add("" + ((Chip) findViewById(group.getCheckedChipIds().get(i))).getText());
                }
                filter();

            }
        });

        // Set adapter to search recyclerView
        adapter = new SearchAdapter(dataPopular, getApplicationContext());
        rcview.setAdapter(adapter);


        // Search from all recipes when Edittext data changed
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Method required*
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Method required*
            }

            @Override
            public void afterTextChanged(Editable s) {

                if(!s.toString().equals("")){ // Search if new alphabet added
                    filterText = s.toString();
                    filter();
                }


            }
        });


        // Exit activity
        back_btn.setOnClickListener(v -> {
            imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
            finish();
        });
        String collectionName =  getIntent().getStringExtra("collection");
        if (collectionName != null) {
            filter();
        }
    }

    // Filter the searched item from all recipes
    public void filter() {
        String collectionName =  getIntent().getStringExtra("collection");
        List<User> filterList = new ArrayList<>();

        for(int i = 0; i<recipes.size(); i++){ // Loop for check searched item in recipe list
            boolean title = false;
            boolean dificulty = true;
            boolean preparationTime = true;
            boolean totalTime = true;
            boolean yield = true;
            boolean tmversion = true;
            boolean rate = true;
            boolean pais = false;
            boolean collection = false;

            if (collectionName == null) {
                collection = true;
            } else if(!collectionName.isEmpty() && recipes.get(i).getCollections().toLowerCase(Locale.ROOT).contains(collectionName.toLowerCase(Locale.ROOT))) {
              collection = true;
            }

            boolean mexico = recipes.get(i).getCollections().toLowerCase(Locale.ROOT).contains("xico");
            if((mexico && recipes.get(i).getCollections().toLowerCase(Locale.ROOT).contains("españa")) || !mexico) {
                pais = true;
            }
            if(recipes.get(i).getTitle().toLowerCase(Locale.ROOT).contains(filterText.toLowerCase(Locale.ROOT))) {
                title = true;
            }
            if (!dificultyFilter.isEmpty() &&
                    !recipes.get(i).getDifficulty().equals(dificultyFilter.get(0))){
                dificulty = false;
            }
            if (!tiempoPreparacionFilter.isEmpty() &&
                    recipes.get(i).getPrepTime()>Integer.parseInt(tiempoPreparacionFilter.get(0))){
                preparationTime = false;
            }
            if (!tiempoTotalFilter.isEmpty() &&
                    recipes.get(i).getTotalTime()>Integer.parseInt(tiempoTotalFilter.get(0))){
                totalTime = false;
            }
            if (!porcionesFilter.isEmpty() &&
                    recipes.get(i).getRecipeYield()!=Integer.parseInt(porcionesFilter.get(0))){
                yield = false;
            }
            if (!ratingFilter.isEmpty() &&
                    recipes.get(i).getRating()>Integer.parseInt(ratingFilter.get(0))){
                rate = false;
            }
            if (!versionFilter.isEmpty() &&
                   !recipes.get(i).getDevices().contains(versionFilter.get(0))){
                tmversion = false;
            }
            if (collection && pais && title && dificulty && preparationTime && totalTime && yield && rate && tmversion) {
                filterList.add(recipes.get(i));
            }
        }

        // Update search recyclerView with new item
        adapter.filterList(filterList);
        results.setText("Mostrando: " + filterList.size() + " recetas");
        if (collectionName != null) {
            results.setText("Mostrando: " + filterList.size() + " recetas de la colección " + collectionName);

        }
    }
}