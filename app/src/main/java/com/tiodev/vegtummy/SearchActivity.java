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
import java.util.stream.Collectors; // For converting String UIDs to Int

public class SearchActivity extends AppCompatActivity {

    // Intent Extras for displaying a favorite list
    public static final String EXTRA_FAVORITE_LIST_NAME = "com.tiodev.vegtummy.EXTRA_FAVORITE_LIST_NAME";
    public static final String EXTRA_FAVORITE_RECIPE_UID_LIST = "com.tiodev.vegtummy.EXTRA_FAVORITE_RECIPE_UID_LIST";

    EditText search;
    ImageView back_btn;
    RecyclerView rcview;
    List<User> dataPopular = new ArrayList<>(); // Used by adapter in normal search mode, populated by filter()
    SearchAdapter adapter;
    List<User> recipes; // Holds all recipes in normal mode, or specific list in favorite mode
    TextView results;

    ChipGroup dificultad, tiempoPreparacion, porciones, version, tiempoTotal, rating;
    String filterText = "";
    boolean isFavoriteListMode = false; // Flag for mode

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

        ArrayList<String> favoriteRecipeUidsStr = getIntent().getStringArrayListExtra(EXTRA_FAVORITE_RECIPE_UID_LIST);
        String favoriteListName = getIntent().getStringExtra(EXTRA_FAVORITE_LIST_NAME);

        if (favoriteRecipeUidsStr != null && !favoriteRecipeUidsStr.isEmpty()) {
            // === MODE: Displaying a specific favorite list ===
            if (favoriteListName != null) {
                setTitle(favoriteListName); // Set activity title
                results.setText("Showing recipes from: " + favoriteListName);
            } else {
                results.setText("Showing favorite recipes");
            }

            // Convert String UIDs to List<Integer> for DAO query
            List<Integer> recipeUidsInt = new ArrayList<>();
            for (String uidStr : favoriteRecipeUidsStr) {
                try {
                    recipeUidsInt.add(Integer.parseInt(uidStr));
                } catch (NumberFormatException e) {
                    // Log error or handle invalid UID string if necessary
                }
            }

            if (!recipeUidsInt.isEmpty()) {
                recipes = userDao.getUsersByIds(recipeUidsInt);
            } else {
                recipes = new ArrayList<>(); // No valid UIDs found
            }

            // Hide search and filter UI elements
            if (search != null) search.setVisibility(View.GONE);
            if (dificultad != null) dificultad.setVisibility(View.GONE);
            if (tiempoPreparacion != null) tiempoPreparacion.setVisibility(View.GONE);
            if (porciones != null) porciones.setVisibility(View.GONE);
            if (version != null) version.setVisibility(View.GONE);
            if (tiempoTotal != null) tiempoTotal.setVisibility(View.GONE);
            if (rating != null) rating.setVisibility(View.GONE);

            // Initialize adapter with the fetched favorite recipes
            adapter = new SearchAdapter(recipes, getApplicationContext()); // Pass recipes directly
            rcview.setLayoutManager(new LinearLayoutManager(this));
            rcview.setAdapter(adapter);
            // Update results text for favorite list mode
            String listTitle = favoriteListName != null ? favoriteListName : "Selected Favorites";
            results.setText(recipes.size() + " recipes in '" + listTitle + "'");

            isFavoriteListMode = true; // Set mode flag

        } else {
            // === MODE: Normal Search ===
            isFavoriteListMode = false; // Set mode flag
            if (search != null) search.setVisibility(View.VISIBLE); // Ensure search is visible

            recipes = userDao.getAll(); // This is the full list for searching
            dataPopular.clear(); // dataPopular is the list displayed by adapter, populated by filter()

            rcview.setLayoutManager(new LinearLayoutManager(this));
            adapter = new SearchAdapter(dataPopular, getApplicationContext());
            rcview.setAdapter(adapter);

            filter(); // Initial filter call for normal search mode (might show all or based on default filters if any)
            // results text will be updated within filter() method in normal mode.

            // Setup listeners only for normal search mode
            setupSearchAndFilterListeners(); // Call to the new method

            // Request focus and show keyboard for normal search mode
            if (search != null) {
                search.requestFocus();
                InputMethodManager immForSearch = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (immForSearch != null) {
                    immForSearch.showSoftInput(search, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        } // End of else for normal search mode

        // Common rcview touch listener and back button listener (moved outside the if/else)
        final InputMethodManager immRef = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (rcview != null && search != null) {
             rcview.setOnTouchListener((v, event) -> {
                if (immRef != null) {
                    immRef.hideSoftInputFromWindow(search.getWindowToken(), 0);
                }
                return false;
            });
        }

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

    }

    private void setupSearchAndFilterListeners() {
        if (search != null) {
            search.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) {
                    filterText = s.toString();
                    filter();
                }
            });
        }

        ChipGroup[] chipGroups = {dificultad, tiempoPreparacion, porciones, version, tiempoTotal, rating};
        for (ChipGroup group : chipGroups) {
            if (group != null) {
                group.setOnCheckedChangeListener((chipGroup, checkedId) -> {
                    List<String> targetFilterList = null;
                    int currentChipGroupId = chipGroup.getId();

                    if (currentChipGroupId == R.id.dificultad) targetFilterList = dificultyFilter;
                    else if (currentChipGroupId == R.id.tiempoPreparacion) targetFilterList = tiempoPreparacionFilter;
                    else if (currentChipGroupId == R.id.tiempoTotal) targetFilterList = tiempoTotalFilter;
                    else if (currentChipGroupId == R.id.porciones) targetFilterList = porcionesFilter;
                    else if (currentChipGroupId == R.id.version) targetFilterList = versionFilter;
                    else if (currentChipGroupId == R.id.rating) targetFilterList = ratingFilter;

                    if (targetFilterList != null) {
                        targetFilterList.clear();
                        for(int selectedChipId : chipGroup.getCheckedChipIds()){ // Iterate over all checked chips
                            Chip chip = chipGroup.findViewById(selectedChipId);
                            if (chip != null) targetFilterList.add(chip.getText().toString());
                        }
                    }
                    filter();
                });
            }
        }
    }

    // Filter the searched item from all recipes
    public void filter() {
        if (isFavoriteListMode) { // If displaying a specific favorite list, don't apply text/chip filters
            // The 'recipes' list is already set and adapter updated.
            // Results text is also set in onCreate for this mode.
            return;
        }

        String collectionNameFromIntent =  getIntent().getStringExtra("collection");
        List<User> tempList = new ArrayList<>();

        if (allRecipesFromDb == null) { // Guard against null allRecipesFromDb if called before init in normal mode
             if (results != null) results.setText("Mostrando: 0 recetas");
            dataForAdapter.clear(); // dataForAdapter is the list the adapter uses
            if (adapter != null) adapter.notifyDataSetChanged();
            return;
        }

        for(User recipe : allRecipesFromDb){
            boolean titleMatches = filterText.isEmpty() || (recipe.getTitle() != null && recipe.getTitle().toLowerCase(Locale.ROOT).contains(filterText.toLowerCase(Locale.ROOT)));

            // Apply collection filter from intent only if no other text filter is active, or consider how they interact
            boolean collectionMatches = (collectionNameFromIntent == null || collectionNameFromIntent.isEmpty() || filterText.isEmpty()) ? // only apply collection if no search text
                                        ( (collectionNameFromIntent == null || collectionNameFromIntent.isEmpty()) || (recipe.getCollections() != null && recipe.getCollections().toLowerCase(Locale.ROOT).contains(collectionNameFromIntent.toLowerCase(Locale.ROOT)))) :
                                        true; // If there's search text, ignore initial collection filter for this logic pass

            boolean difficultyMatches = dificultyFilter.isEmpty() || (recipe.getDifficulty() != null && dificultyFilter.contains(recipe.getDifficulty()));

            // Assuming time filters mean "less than or equal to selected value"
            // And that filter lists for time contain single string like "30" (minutes)
            boolean prepTimeMatches = tiempoPreparacionFilter.isEmpty() ||
                                      (recipe.getPrepTime() != null && !tiempoPreparacionFilter.stream().anyMatch(t -> recipe.getPrepTime() > Integer.parseInt(t.replaceAll("[^0-9]", "")) ));

            boolean totalTimeMatches = tiempoTotalFilter.isEmpty() ||
                                       (recipe.getTotalTime() != null && !tiempoTotalFilter.stream().anyMatch(t -> recipe.getTotalTime() > Integer.parseInt(t.replaceAll("[^0-9]", "")) ));

            boolean yieldMatches = porcionesFilter.isEmpty() ||
                                   (recipe.getRecipeYield() != null && porcionesFilter.contains(String.valueOf(recipe.getRecipeYield())));

            // Assuming rating filter means "greater than or equal to selected value"
            boolean ratingMatches = ratingFilter.isEmpty() ||
                                    (recipe.getRating() != null && !ratingFilter.stream().anyMatch(r -> recipe.getRating() < Integer.parseInt(r.replaceAll("[^0-9]", "")) ));

            boolean versionMatches = versionFilter.isEmpty() ||
                                     (recipe.getDevices() != null && versionFilter.stream().anyMatch(v -> recipe.getDevices().contains(v)));

            // Simplified "pais" logic from original, effectively making it always true unless specific conditions are re-added
            boolean paisMatches = true;
            // Example of original complex pais logic (needs careful review if re-enabled):
            // if (recipe.getCollections() != null) {
            //    boolean mexico = recipe.getCollections().toLowerCase(Locale.ROOT).contains("xico");
            //    boolean espana = recipe.getCollections().toLowerCase(Locale.ROOT).contains("españa");
            //    if (mexico && espana) paisMatches = true; // Or some other logic
            //    else if (mexico) paisMatches = true; // Or false depending on intent
            //    else if (espana) paisMatches = true; // Or false
            // }


            if (titleMatches && collectionMatches && difficultyMatches && prepTimeMatches && totalTimeMatches && yieldMatches && ratingMatches && versionMatches && paisMatches) {
                tempList.add(recipe);
            }
        }

        dataForAdapter.clear();
        dataForAdapter.addAll(tempList);
        if (adapter != null) { // Ensure adapter is not null
            adapter.notifyDataSetChanged();
        }

        if (results != null) {
            // Use the active collectionNameFromIntent for results text if relevant
            String currentCollectionNameForDisplay = getIntent().getStringExtra("collection");
            if (currentCollectionNameForDisplay != null && !currentCollectionNameForDisplay.isEmpty() && (filterText.isEmpty() && allFiltersEmpty())) {
                 results.setText("Mostrando: " + dataForAdapter.size() + " recetas de la colección " + currentCollectionNameForDisplay);
            } else {
                 results.setText("Mostrando: " + dataForAdapter.size() + " recetas");
            }
        }
    }

    // Helper to check if all chip filters are empty
    private boolean allFiltersEmpty() {
        return dificultyFilter.isEmpty() && tiempoPreparacionFilter.isEmpty() &&
               porcionesFilter.isEmpty() && versionFilter.isEmpty() &&
               tiempoTotalFilter.isEmpty() && ratingFilter.isEmpty();
    }
}