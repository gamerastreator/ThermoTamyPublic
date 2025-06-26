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
import android.widget.Toast;

import com.tiodev.vegtummy.Adapter.CollectionsAdapter;
import com.tiodev.vegtummy.Model.CollectionItem; // Import CollectionItem

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections; // Keep for sorting if needed
import java.util.List;


public class CollectionsFragment extends Fragment implements CollectionsAdapter.OnCollectionClickListener {

    private RecyclerView collectionsRecyclerView;
    private CollectionsAdapter collectionsAdapter;
    private List<CollectionItem> collectionItemList; // Changed from List<String>

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

        // Initialize adapter with the new List type
        collectionsAdapter = new CollectionsAdapter(getContext(), collectionItemList, this);
        collectionsRecyclerView.setAdapter(collectionsAdapter);

        loadCollectionsFromJson(); // Call the new method

        return view;
    }

    private void loadCollectionsFromJson() {
        Log.d(TAG, "Loading collections from JSON...");
        if (getContext() == null || !isAdded()) {
            Log.e(TAG, "Context is null or fragment not added, cannot load collections.");
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
            collectionItemList.clear(); // Clear previous items

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String id = jsonObject.optString("id");
                String title = jsonObject.optString("title");
                String image = jsonObject.optString("image"); // Assuming there's an image field

                if (!title.isEmpty()) { // Add only if title is present
                    collectionItemList.add(new CollectionItem(id, title, image));
                }
            }

            Log.d(TAG, "Collections loaded from JSON: " + collectionItemList.size());
            // Optional: Sort by title
            // Collections.sort(collectionItemList, (o1, o2) -> o1.getTitle().compareToIgnoreCase(o2.getTitle()));

            collectionsAdapter.updateData(collectionItemList);

        } catch (JSONException ex) {
            Log.e(TAG, "JSONException while parsing collections.json", ex);
            Toast.makeText(getContext(), "Error parsing collections data.", Toast.LENGTH_SHORT).show();
            collectionsAdapter.updateData(new ArrayList<>());
        }

        if (collectionItemList.isEmpty()) {
            Toast.makeText(getContext(), "No collections found.", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "No collections found or parsed from JSON.");
        }
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
        Intent intent = new Intent(getContext(), SearchActivity.class);

        // Pass the collection title to SearchActivity.
        // This assumes SearchActivity uses the collection title for filtering.
        intent.putExtra("collection", collectionItem.getTitle());

        startActivity(intent);
    }
}
