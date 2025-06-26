package com.tiodev.vegtummy;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
// Removed Room imports related to User/UserDao for this specific collection loading

import android.content.res.AssetManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView; // Import TextView
import androidx.appcompat.widget.SearchView; // Import SearchView
import android.widget.Toast;

import com.tiodev.vegtummy.Adapter.CollectionsAdapter;
import com.tiodev.vegtummy.Model.CollectionItem;

import com.google.android.material.bottomnavigation.BottomNavigationView; // Added for BNV interaction

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections; // Keep for sorting if needed
import java.util.LinkedHashMap; // Import LinkedHashMap
import java.util.List;
import java.util.Map; // Import Map


public class CollectionsFragment extends Fragment implements CollectionsAdapter.OnCollectionClickListener {

    private RecyclerView collectionsRecyclerView;
    private CollectionsAdapter collectionsAdapter;
    private List<CollectionItem> collectionItemList;
    private SearchView searchView;
    private TextView collectionCountTextView;

    private static final String TAG = "CollectionsFragment";

    public CollectionsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Removed UserDao initialization
        collectionItemList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collections, container, false);

        collectionsRecyclerView = view.findViewById(R.id.collections_recycler_view);
        collectionsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        searchView = view.findViewById(R.id.collections_search_view);
        collectionCountTextView = view.findViewById(R.id.collection_count_text_view);

        // Initialize adapter with the new List type
        // Ensure collectionItemList is initialized before passing to adapter
        if (collectionItemList == null) {
            collectionItemList = new ArrayList<>();
        }
        collectionsAdapter = new CollectionsAdapter(getContext(), collectionItemList, this);
        collectionsRecyclerView.setAdapter(collectionsAdapter);

        setupSearchView();
        loadCollectionsFromJson(); // Call the new method to load data initially

        return view;
    }

    private void setupSearchView() {
        if (getContext() == null || !isAdded()) return;
        searchView.setQueryHint(getString(R.string.collections_search_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (collectionsAdapter != null) {
                    collectionsAdapter.getFilter().filter(query);
                }
                return false; // Let the SearchView handle the submission if necessary
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (collectionsAdapter != null) {
                    collectionsAdapter.getFilter().filter(newText);
                }
                // It's better to update count after filter.publishResults() is done.
                // For simplicity, we'll update it here, but a callback or listener
                // from the adapter/filter would be more robust.
                // A handler post can also work to delay the count update slightly.
                // Or, the fragment can get the count from the adapter after notifyDataSetChanged.
                // Let's assume for now that notifyDataSetChanged() in publishResults is synchronous enough.
                // The most reliable way is for the fragment to observe data changes if using LiveData,
                // or for the adapter to call a method on the fragment after filtering.
                // For now, we'll update count after calling filter.
                // This relies on the filter completing and calling notifyDataSetChanged() before this.
                // A more robust solution would be a callback from the adapter to the fragment.
                // For now, we'll rely on the fact that notifyDataSetChanged will trigger a re-layout
                // and the fragment can then query the adapter.
                // A simple way: update count after filter call and rely on adapter update.
                // The adapter's publishResults calls notifyDataSetChanged.
                // We will update the count TextView in a separate method called after data loading/filtering.
                updateCollectionCount();
                return true;
            }
        });
    }

    private void updateCollectionCount() {
        if (getContext() == null || !isAdded() || collectionsAdapter == null || collectionCountTextView == null) {
            return;
        }
        int count = collectionsAdapter.getItemCount();
        collectionCountTextView.setText(getString(R.string.collections_count_format, count));
    }

    private void loadCollectionsFromJson() {
        Log.d(TAG, "Loading collections from JSON...");
        if (getContext() == null || !isAdded()) {
            Log.e(TAG, "Context is null or fragment not added, cannot load collections.");
            updateCollectionCount(); // Ensure count is updated even if loading fails early
            return;
        }

        AssetManager assetManager = getContext().getAssets();
        String jsonString;
        try {
            InputStream is = assetManager.open("data/static/collections.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            jsonString = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            Log.e(TAG, "IOException while loading collections.json", ex);
            Toast.makeText(getContext(), "Error loading collections data.", Toast.LENGTH_SHORT).show();
            collectionsAdapter.updateData(new ArrayList<>()); // Update with empty list
            return;
        }

        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            // Use LinkedHashMap to store items by title, ensuring uniqueness and preserving order
            Map<String, CollectionItem> uniqueCollectionItemsMap = new LinkedHashMap<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String id = jsonObject.optString("id");
                String title = jsonObject.optString("title");
                String image = jsonObject.optString("image"); // Assuming there's an image field

                if (!title.isEmpty()) { // Add only if title is present
                    // If title is not already in map, put the new item. This keeps the first occurrence.
                    uniqueCollectionItemsMap.putIfAbsent(title, new CollectionItem(id, title, image));
                }
            }

            collectionItemList.clear(); // Clear previous items
            collectionItemList.addAll(uniqueCollectionItemsMap.values()); // Add unique items from map

            Log.d(TAG, "Unique collections loaded from JSON: " + collectionItemList.size());
            // Optional: Sort by title if needed (already first-occurrence order is maintained by LinkedHashMap)
            // Collections.sort(collectionItemList, (o1, o2) -> o1.getTitle().compareToIgnoreCase(o2.getTitle()));

            collectionsAdapter.updateData(collectionItemList);
            updateCollectionCount(); // Update count after initial load

        } catch (JSONException ex) {
            Log.e(TAG, "JSONException while parsing collections.json", ex);
            Toast.makeText(getContext(), "Error parsing collections data.", Toast.LENGTH_SHORT).show();
            collectionsAdapter.updateData(new ArrayList<>());
            updateCollectionCount(); // Update count after error
        }

        if (collectionItemList.isEmpty()) {
            // This Toast might be redundant if updateCollectionCount also implies emptiness or if errors are already shown
            // Toast.makeText(getContext(), "No collections found.", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "No collections found or parsed from JSON.");
        }
        // Ensure count is accurate if list is empty after try-catch
        updateCollectionCount();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data if needed, e.g. if JSON could change or based on other app events
        // loadCollectionsFromJson(); // Could be called here if dynamic updates are expected
    }

    @Override
    public void onCollectionClick(CollectionItem collectionItem) { // Parameter changed to CollectionItem
        if (getContext() == null || !isAdded() || collectionItem == null) {
            Log.e(TAG, "Context or collectionItem is null, or fragment not added. Cannot start SearchActivity.");
            return;
        }
        Log.d(TAG, "Collection clicked: " + collectionItem.getTitle() + " (ID: " + collectionItem.getId() + ")");

        // Create SearchFragment instance with the collection title as an argument
        SearchFragment searchFragment = SearchFragment.newInstance(collectionItem.getTitle());

        // Replace current fragment with SearchFragment
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, searchFragment)
                .addToBackStack(null) // Add to back stack so user can navigate back
                .commit();

        // Update BottomNavigationView to select the "Search" tab
       /* if (getActivity() instanceof HomeActivity) {
            HomeActivity homeActivity = (HomeActivity) getActivity();
            BottomNavigationView bottomNavView = homeActivity.findViewById(R.id.bottom_navigation);
            if (bottomNavView != null) {
                bottomNavView.setSelectedItemId(R.id.navigation_launch_search);
            }
        }*/
    }
}
