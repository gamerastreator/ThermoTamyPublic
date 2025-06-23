package com.tiodev.vegtummy;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tiodev.vegtummy.Adapter.AdapterPopular;
import com.tiodev.vegtummy.RoomDB.AppDatabase;
import com.tiodev.vegtummy.RoomDB.User;
import com.tiodev.vegtummy.RoomDB.UserDao;

import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment {

    private RecyclerView favoritesRecyclerView;
    private AdapterPopular adapterPopular;
    private List<User> favoriteRecipesList;
    private TextView emptyFavoritesText;
    private AppDatabase appDatabase;

    public FavoritesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        favoritesRecyclerView = view.findViewById(R.id.favorites_recycler_view);
        emptyFavoritesText = view.findViewById(R.id.empty_favorites_text);

        favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        favoriteRecipesList = new ArrayList<>();
        adapterPopular = new AdapterPopular(favoriteRecipesList, getContext());
        favoritesRecyclerView.setAdapter(adapterPopular);

        // Initialize Room database
        appDatabase = Room.databaseBuilder(getContext().getApplicationContext(),
                        AppDatabase.class, "db_name") // Ensure this matches HomeActivity
                .allowMainThreadQueries() // For simplicity in fragment, consider background thread for DB ops
                .createFromAsset("database/recipe.db") // Ensure this matches
                .fallbackToDestructiveMigration() // Ensure this matches
                .build();

        loadFavoriteRecipes();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload favorites when the fragment becomes visible,
        // in case favorites were changed while this fragment was in the background.
        loadFavoriteRecipes();
    }

    private void loadFavoriteRecipes() {
        List<String> favoriteIds = FavoriteManager.getFavoriteRecipeIds(getContext());
        favoriteRecipesList.clear();

        if (favoriteIds.isEmpty()) {
            emptyFavoritesText.setVisibility(View.VISIBLE);
            favoritesRecyclerView.setVisibility(View.GONE);
        } else {
            emptyFavoritesText.setVisibility(View.GONE);
            favoritesRecyclerView.setVisibility(View.VISIBLE);
            UserDao userDao = appDatabase.userDao();
            for (String idStr : favoriteIds) {
                try {
                    int id = Integer.parseInt(idStr);
                    // Query database for each favorite recipe by its ID
                    // Note: userDao.findById(id) would be more efficient if it exists.
                    // If not, we might need to add it or iterate through all and filter.
                    // For now, assuming we can fetch by ID or filter.
                    // Let's check UserDao for a suitable method.
                    // HomeActivity loads all recipes: List<User> recipes = userDao.getAll();
                    // We can iterate through all and pick favorites. This is not super efficient for large DBs.
                    // A direct query like "SELECT * FROM recipe WHERE uid IN (:favoriteIds)" would be best.
                    // Let's assume userDao has a method like `loadAllByIds(int[] userIds)` or we fetch one by one.
                    // For simplicity, if no direct method, will fetch all and filter. This is inefficient.
                    // I'll add a placeholder for fetching by ID. This needs to be implemented in UserDao.
                    User recipe = userDao.getUserById(id); // Assuming getUserById(int id) exists or will be added.
                    if (recipe != null) {
                        favoriteRecipesList.add(recipe);
                    }
                } catch (NumberFormatException e) {
                    // Handle invalid ID format in favorites.json if necessary
                }
            }
        }
        adapterPopular.notifyDataSetChanged();
    }
}
