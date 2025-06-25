package com.tiodev.vegtummy;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.tiodev.vegtummy.Adapter.FavoritesAdapter; // Using FavoritesAdapter
import com.tiodev.vegtummy.RoomDB.AppDatabase;
import com.tiodev.vegtummy.RoomDB.User;
import com.tiodev.vegtummy.RoomDB.UserDao;

import java.util.ArrayList;
import java.util.List;

public class FavoriteRecipeListFragment extends Fragment {

    private static final String ARG_LIST_ID = "list_id";

    private String listId;
    private FavoriteList currentFavoriteList;

    private RecyclerView recipeRecyclerView;
    private FavoritesAdapter recipesAdapter;
    private List<User> recipeList;
    private TextView listNameTitleTextView;
    private TextView emptyListTextView;
    private AppDatabase appDatabase;

    public FavoriteRecipeListFragment() {
        // Required empty public constructor
    }

    public static FavoriteRecipeListFragment newInstance(String listId) {
        FavoriteRecipeListFragment fragment = new FavoriteRecipeListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LIST_ID, listId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            listId = getArguments().getString(ARG_LIST_ID);
        }
        // Initialize Room database
        if (getContext() != null) {
            appDatabase = Room.databaseBuilder(requireContext().getApplicationContext(),
                            AppDatabase.class, "db_name")
                    .allowMainThreadQueries() // For simplicity, consider background thread for real apps
                    .createFromAsset("database/recipe.db")
                    .fallbackToDestructiveMigration()
                    .build();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorite_recipe_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listNameTitleTextView = view.findViewById(R.id.favorite_list_name_title);
        recipeRecyclerView = view.findViewById(R.id.recipe_list_recycler_view);
        emptyListTextView = view.findViewById(R.id.empty_recipe_list_text);

        recipeRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recipeList = new ArrayList<>();
        // FavoritesAdapter is designed to take List<User> and Context
        // and it opens WebviewRecipeActivity, which is what we want here.
        recipesAdapter = new FavoritesAdapter(recipeList, getContext());
        recipeRecyclerView.setAdapter(recipesAdapter);

        if (listId == null || getContext() == null) {
            Toast.makeText(getContext(), "Error: Favorite list ID not found.", Toast.LENGTH_LONG).show();
            if (getActivity() != null) getActivity().getSupportFragmentManager().popBackStack();
            return;
        }

        currentFavoriteList = FavoriteListManager.getFavoriteListById(getContext(), listId);

        if (currentFavoriteList != null) {
            listNameTitleTextView.setText(currentFavoriteList.getName());
            loadRecipesForList();
        } else {
            listNameTitleTextView.setText("Favorite List Not Found");
            emptyListTextView.setVisibility(View.VISIBLE);
            recipeRecyclerView.setVisibility(View.GONE);
            // Toast.makeText(getContext(), "Could not load the favorite list.", Toast.LONG_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh if recipes in list could have changed externally, or if a recipe was unfavorited
        // from one of the lists it was in, affecting this list.
        if (currentFavoriteList != null && getContext() != null) {
            // Re-fetch the list to get the latest recipe IDs
            FavoriteList updatedList = FavoriteListManager.getFavoriteListById(getContext(), listId);
            if (updatedList != null) {
                currentFavoriteList = updatedList; // update internal state
                loadRecipesForList();
            } else {
                // List might have been deleted
                listNameTitleTextView.setText("Favorite List Not Found");
                emptyListTextView.setVisibility(View.VISIBLE);
                recipeRecyclerView.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Favorite list no longer exists.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void loadRecipesForList() {
        if (getContext() == null || currentFavoriteList == null || appDatabase == null) {
            //Log.e("FavRecipeListFrag", "Context, currentFavoriteList, or appDatabase is null in loadRecipesForList.");
            emptyListTextView.setVisibility(View.VISIBLE);
            recipeRecyclerView.setVisibility(View.GONE);
            return;
        }

        List<String> recipeUids = currentFavoriteList.getRecipeIds();
        recipeList.clear();

        if (recipeUids.isEmpty()) {
            emptyListTextView.setVisibility(View.VISIBLE);
            recipeRecyclerView.setVisibility(View.GONE);
        } else {
            emptyListTextView.setVisibility(View.GONE);
            recipeRecyclerView.setVisibility(View.VISIBLE);
            UserDao userDao = appDatabase.userDao();
            for (String uidStr : recipeUids) {
                try {
                    int uid = Integer.parseInt(uidStr);
                    User recipe = userDao.getUserById(uid); // Assumes getUserById(int id) exists
                    if (recipe != null) {
                        recipeList.add(recipe);
                    }
                } catch (NumberFormatException e) {
                    //Log.e("FavRecipeListFrag", "Invalid recipe UID format: " + uidStr, e);
                }
            }
        }
        if (recipesAdapter != null) {
            recipesAdapter.notifyDataSetChanged();
        } else {
            // Log.e("FavRecipeListFrag", "recipesAdapter is null in loadRecipesForList.");
        }
    }
}
