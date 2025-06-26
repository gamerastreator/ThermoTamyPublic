package com.tiodev.vegtummy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.tiodev.vegtummy.Adapter.SearchAdapter;
import com.tiodev.vegtummy.RoomDB.AppDatabase;
import com.tiodev.vegtummy.RoomDB.User;
import com.tiodev.vegtummy.RoomDB.UserDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Implement the click listener interface
public class SearchFragment extends Fragment implements SearchAdapter.OnRecipeClickListener {

    // Copied from SearchActivity
    EditText search;
    ImageView back_btn; // This might be removed or its functionality changed
    RecyclerView rcview;
    // dataPopular is initialized in filter() or by adapter, was empty initially in SearchActivity
    List<User> dataPopular = new ArrayList<>();
    SearchAdapter adapter;
    List<User> recipes; // Full list of recipes from DB
    TextView results;

    ChipGroup dificultad, tiempoPreparacion, porciones, version, tiempoTotal, rating;
    String filterText = ""; // Current text in EditText search

    // Lists to store selected chip values
    List<String> dificultyFilter = new ArrayList<>();
    List<String> tiempoPreparacionFilter = new ArrayList<>();
    List<String> porcionesFilter = new ArrayList<>();
    List<String> versionFilter = new ArrayList<>();
    List<String> tiempoTotalFilter = new ArrayList<>();
    List<String> ratingFilter = new ArrayList<>();

    // Argument key for collection name
    private static final String ARG_COLLECTION_NAME = "collectionName";
    private String initialCollectionName = null;

    private UserDao userDao; // To be initialized in onCreate or similar

    public SearchFragment() {
        // Required empty public constructor
    }

    // Static factory method for creating fragment with arguments (e.g., for collection pre-filter)
    public static SearchFragment newInstance(@Nullable String collectionName) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        if (collectionName != null) {
            args.putString(ARG_COLLECTION_NAME, collectionName);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            initialCollectionName = getArguments().getString(ARG_COLLECTION_NAME);
        }

        // Initialize Room DB and DAO
        // Consider moving DB access off the main thread in a real app
        AppDatabase db = Room.databaseBuilder(requireContext().getApplicationContext(),
                        AppDatabase.class, "db_name").allowMainThreadQueries()
                .createFromAsset("database/recipe.db")
                .fallbackToDestructiveMigration()
                .build();
        userDao = db.userDao();
        recipes = userDao.getAll(); // Fetch all recipes once
        if (recipes == null) {
            recipes = new ArrayList<>(); // Ensure recipes list is not null
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find views (IDs must match fragment_search.xml)
        search = view.findViewById(R.id.search);
        back_btn = view.findViewById(R.id.back_to_home);
        rcview = view.findViewById(R.id.rcview);
        results = view.findViewById(R.id.results);
        dificultad = view.findViewById(R.id.dificultad);
        tiempoPreparacion = view.findViewById(R.id.tiempoPreparacion);
        porciones = view.findViewById(R.id.porciones);
        version = view.findViewById(R.id.version);
        tiempoTotal = view.findViewById(R.id.tiempoTotal);
        rating = view.findViewById(R.id.rating);

        // Setup RecyclerView
        rcview.setLayoutManager(new LinearLayoutManager(getContext()));
        // dataPopular is used by adapter; it will be populated by the filter method.
        // Initialize with empty list for now.
        // Pass 'this' as the OnRecipeClickListener
        adapter = new SearchAdapter(dataPopular, requireContext().getApplicationContext(), this);
        rcview.setAdapter(adapter);

        // Initial filter call if initialCollectionName is set or to show all/default
        filter();

        // Setup listeners and other UI interactions
        setupUIListeners();

        // Show and focus the keyboard (optional, consider UX in fragment context)
        // search.requestFocus();
        // InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        // if (imm != null) {
        //     imm.showSoftInput(search, InputMethodManager.SHOW_IMPLICIT);
        // }
    }

    // Placeholder for UI listeners setup
    private void setupUIListeners() {
        // Back button functionality will change (e.g., pop back stack or handled by HomeActivity)
        back_btn.setOnClickListener(v -> {
            if (getActivity() != null) {
                // Hide keyboard
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null && search != null) {
                    imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
                }
                // Instead of finish(), pop the back stack if this fragment was added to it
                getParentFragmentManager().popBackStack();
            }
        });

        // Hide keyboard when recyclerView item clicked
        rcview.setOnTouchListener((v, event) -> {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null && search != null) {
                imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
            }
            return false;
        });

        // TextWatcher for search EditText
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                filterText = s.toString();
                filter();
            }
        });

        // ChipGroup Listeners
        setupChipGroupListener(dificultad, dificultyFilter);
        setupChipGroupListener(tiempoPreparacion, tiempoPreparacionFilter);
        setupChipGroupListener(porciones, porcionesFilter);
        setupChipGroupListener(version, versionFilter);
        setupChipGroupListener(tiempoTotal, tiempoTotalFilter);
        setupChipGroupListener(rating, ratingFilter);
    }

    private void setupChipGroupListener(ChipGroup chipGroup, List<String> filterList) {
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            filterList.clear();
            for (int id : checkedIds) {
                Chip chip = group.findViewById(id);
                if (chip != null) {
                    filterList.add(chip.getText().toString());
                }
            }
            filter();
        });
    }

    // Filter method (copied and adapted from SearchActivity)
    // Needs access to 'recipes' list (all recipes) and 'initialCollectionName'
    public void filter() {
        if (recipes == null || adapter == null || results == null || getContext() == null) {
             // Ensure components are initialized, especially if filter is called very early
            return;
        }

        List<User> filterList = new ArrayList<>();
        String currentCollectionName = initialCollectionName; // Use the argument passed to the fragment

        for(User recipe : recipes){
            boolean titleMatch = false;
            boolean dificultyMatch = true;
            boolean prepTimeMatch = true;
            boolean totalTimeMatch = true;
            boolean yieldMatch = true;
            boolean versionMatch = true;
            boolean ratingMatch = true;
            boolean collectionMatch = false;

            // Collection filter
            if (currentCollectionName == null || currentCollectionName.isEmpty()) {
                collectionMatch = true; // No collection filter means all collections match
            } else if (recipe.getCollections() != null && recipe.getCollections().toLowerCase(Locale.ROOT).contains(currentCollectionName.toLowerCase(Locale.ROOT))) {
                collectionMatch = true;
            }

            // Title filter (text search)
            if (filterText.isEmpty() || (recipe.getTitle() != null && recipe.getTitle().toLowerCase(Locale.ROOT).contains(filterText.toLowerCase(Locale.ROOT)))) {
                titleMatch = true;
            }

            // Chip filters
            if (!dificultyFilter.isEmpty() && (recipe.getDifficulty() == null || !dificultyFilter.contains(recipe.getDifficulty()))) {
                dificultyMatch = false;
            }
            if (!tiempoPreparacionFilter.isEmpty() && (recipe.getPrepTime() == null || recipe.getPrepTime() > Integer.parseInt(tiempoPreparacionFilter.get(0)))) {
                // Assuming single selection for time filters for simplicity of porting, original logic was >
                 prepTimeMatch = false;
            }
             if (!tiempoTotalFilter.isEmpty() && (recipe.getTotalTime() == null || recipe.getTotalTime() > Integer.parseInt(tiempoTotalFilter.get(0)))) {
                totalTimeMatch = false;
            }
            if (!porcionesFilter.isEmpty() && (recipe.getRecipeYield() == null || !porcionesFilter.contains(String.valueOf(recipe.getRecipeYield())))) {
                yieldMatch = false;
            }
            if (!ratingFilter.isEmpty() && (recipe.getRating() == null || !ratingFilter.contains(String.valueOf(recipe.getRating())))) {
                 // Original logic was > Integer.parseInt(ratingFilter.get(0)), now check if it's in the list of selected ratings
                ratingMatch = false;
            }
            if (!versionFilter.isEmpty() && (recipe.getDevices() == null || !versionFilter.stream().anyMatch(ver -> recipe.getDevices().contains(ver)))) {
                versionMatch = false;
            }

            if (collectionMatch && titleMatch && dificultyMatch && prepTimeMatch && totalTimeMatch && yieldMatch && ratingMatch && versionMatch) {
                filterList.add(recipe);
            }
        }

        adapter.filterList(filterList); // Update adapter
        String resultText = "Mostrando: " + filterList.size() + " recetas";
        if (currentCollectionName != null && !currentCollectionName.isEmpty()) {
            resultText += " de la colección " + currentCollectionName;
        }
        results.setText(resultText);
    }

    @Override
    public void onRecipeClicked(User recipe) {
        if (getActivity() == null || !isAdded() || recipe == null) {
            return;
        }

        // Construct the htmlPath for WebviewRecipeFragment
        // This assumes recipe.getIdentifier() gives the base name for the .html file
        String htmlPath = "data/" + recipe.getIdentifier() + ".html";
        String recipeIdStr = String.valueOf(recipe.getUid());

        WebviewRecipeFragment webviewFragment = WebviewRecipeFragment.newInstance(
                recipeIdStr,
                htmlPath,
                recipe.getTitle()
        );

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, webviewFragment) // Assumes R.id.fragment_container is in HomeActivity
                .addToBackStack(null) // Allows user to navigate back to search results
                .commit();

        // Optional: Hide keyboard if it was open
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && getView() != null) {
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
    }
}
