package com.tiodev.vegtummy;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.tiodev.vegtummy.Adapter.CollectionsAdapter;
import com.tiodev.vegtummy.RoomDB.AppDatabase;
import com.tiodev.vegtummy.RoomDB.User;
import com.tiodev.vegtummy.RoomDB.UserDao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CollectionsFragment extends Fragment implements CollectionsAdapter.OnCollectionClickListener {

    private RecyclerView collectionsRecyclerView;
    private CollectionsAdapter collectionsAdapter;
    private List<String> collectionNamesList;
    private UserDao userDao;

    private static final String TAG = "CollectionsFragment";

    public CollectionsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppDatabase db = Room.databaseBuilder(requireContext().getApplicationContext(),
                        AppDatabase.class, "db_name")
                .allowMainThreadQueries() // For simplicity in this example; consider background thread for DB operations
                .createFromAsset("database/recipe.db")
                .fallbackToDestructiveMigration()
                .build();
        userDao = db.userDao();
        collectionNamesList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collections, container, false);

        collectionsRecyclerView = view.findViewById(R.id.collections_recycler_view);
        collectionsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        collectionsAdapter = new CollectionsAdapter(getContext(), collectionNamesList, this);
        collectionsRecyclerView.setAdapter(collectionsAdapter);

        loadCollections();

        return view;
    }

    private void loadCollections() {
        Log.d(TAG, "Loading collections from database...");
        List<User> allRecipes = userDao.getAll();
        if (allRecipes == null || allRecipes.isEmpty()) {
            Log.d(TAG, "No recipes found in the database.");
            if (isAdded() && getContext() != null) { // Check if fragment is added and context is available
                 Toast.makeText(getContext(), "No recipes found to derive collections.", Toast.LENGTH_SHORT).show();
            }
            collectionsAdapter.updateData(new ArrayList<>()); // Update with empty list
            return;
        }

        Log.d(TAG, "Total recipes fetched: " + allRecipes.size());
        Set<String> uniqueCollectionNames = new HashSet<>();
        for (User recipe : allRecipes) {
            String collectionsStr = recipe.getCollections();
            if (!TextUtils.isEmpty(collectionsStr)) {
                // Assuming collections are comma-separated, possibly with spaces
                String[] names = collectionsStr.split("\\s*,\\s*"); // Split by comma, trimming whitespace
                for (String name : names) {
                    if (!TextUtils.isEmpty(name)) {
                        uniqueCollectionNames.add(name.trim());
                    }
                }
            }
        }

        Log.d(TAG, "Unique collections found: " + uniqueCollectionNames.size());

        collectionNamesList.clear();
        collectionNamesList.addAll(new ArrayList<>(uniqueCollectionNames));
        Collections.sort(collectionNamesList); // Optional: sort alphabetically

        if (isAdded() && collectionsAdapter != null) { // Check if fragment is added
             collectionsAdapter.updateData(collectionNamesList);
        }

        if (collectionNamesList.isEmpty() && isAdded() && getContext() != null) {
            Toast.makeText(getContext(), "No collections found.", Toast.LENGTH_SHORT).show();
             Log.d(TAG, "No collections derived from recipes.");
        }
    }

    @Override
    public void onCollectionClick(String collectionName) {
        if (getContext() == null || !isAdded()) {
            Log.e(TAG, "Context is null or fragment not added, cannot start SearchActivity.");
            return;
        }
        Log.d(TAG, "Collection clicked: " + collectionName);
        Intent intent = new Intent(getContext(), SearchActivity.class);
        // SearchActivity expects the "collection" extra to be the title of the collection.
        // This title is then used to filter recipes.
        // The WebviewRecipeActivity's loadJSONFromAsset was previously used to get this title
        // from a collection ID. Here, the collectionName IS the title.
        intent.putExtra("collection", collectionName);
        // We also need to tell SearchActivity that it's filtering by a collection name,
        // not a general search query. SearchActivity's current logic might need adjustment
        // if it only expects a search query string.
        // For now, we assume SearchActivity can handle a collection title passed this way.
        // It might internally search for recipes where the `recipe.getCollections()` string
        // contains the passed `collectionName`.
        startActivity(intent);
    }
}
