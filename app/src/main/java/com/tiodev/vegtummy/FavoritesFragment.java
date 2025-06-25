package com.tiodev.vegtummy;

import android.content.Intent; // Ensure this import is present
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tiodev.vegtummy.Adapter.FavoriteListNameAdapter; // New Adapter
import com.tiodev.vegtummy.FavoriteList; // New POJO
import com.tiodev.vegtummy.FavoriteListManager; // New Manager

import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment implements FavoriteListNameAdapter.OnFavoriteListClickListener {

    private RecyclerView favoritesRecyclerView;
    private FavoriteListNameAdapter listNameAdapter; // Changed adapter
    private List<FavoriteList> favoriteMasterList; // Changed data type
    private TextView emptyFavoritesText;
    private FloatingActionButton fabCreateList;

    // For context menu, to know which item was long-pressed
    private FavoriteList selectedFavoriteListForContextMenu;


    public FavoritesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        favoritesRecyclerView = view.findViewById(R.id.favorites_recycler_view);
        emptyFavoritesText = view.findViewById(R.id.empty_favorites_text);
        fabCreateList = view.findViewById(R.id.fab_create_list);

        favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        favoriteMasterList = new ArrayList<>();
        listNameAdapter = new FavoriteListNameAdapter(getContext(), favoriteMasterList, this);
        favoritesRecyclerView.setAdapter(listNameAdapter);

        // Register RecyclerView for context menu (for long-press on items)
        registerForContextMenu(favoritesRecyclerView);

        fabCreateList.setOnClickListener(v -> showCreateListDialog());

        // No need for AppDatabase here anymore to load recipes directly into this fragment's main list
        // That will be handled when a specific list is selected to view its recipes.

        loadFavoriteLists(); // Load list names

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavoriteLists(); // Refresh list names on resume
    }

    private void loadFavoriteLists() {
        if (getContext() == null) return; // Context might be null if fragment is detached

        List<FavoriteList> currentLists = FavoriteListManager.getFavoriteLists(getContext());
        favoriteMasterList.clear();
        if (currentLists != null) {
            favoriteMasterList.addAll(currentLists);
        }

        if (favoriteMasterList.isEmpty()) {
            emptyFavoritesText.setVisibility(View.VISIBLE);
            favoritesRecyclerView.setVisibility(View.GONE);
        } else {
            emptyFavoritesText.setVisibility(View.GONE);
            favoritesRecyclerView.setVisibility(View.VISIBLE);
        }
        if (listNameAdapter != null) {
            // The favoriteMasterList instance (which the adapter holds a reference to)
            // has been updated (cleared and new items added).
            // So, just notifying the adapter should be sufficient.
            listNameAdapter.notifyDataSetChanged();
        }
    }

    private void showCreateListDialog() {
        if (getContext() == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Create New Favorite List");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setHint("List Name");
        builder.setView(input);

        builder.setPositiveButton("Create", (dialog, which) -> {
            String listName = input.getText().toString().trim();
            if (!listName.isEmpty()) {
                FavoriteList newList = FavoriteListManager.createFavoriteList(getContext(), listName);
                if (newList != null) {
                    loadFavoriteLists(); // Refresh the list
                    Toast.makeText(getContext(), "List '" + listName + "' created.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to create list.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "List name cannot be empty.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // This is the correctly modified onFavoriteListClick method
    @Override
    public void onFavoriteListClick(FavoriteList favoriteList) {
        if (getActivity() != null && favoriteList != null && favoriteList.getRecipeIds() != null) {
            Intent intent = new Intent(getActivity(), SearchActivity.class);
            intent.putExtra(SearchActivity.EXTRA_FAVORITE_LIST_NAME, favoriteList.getName());
            intent.putStringArrayListExtra(SearchActivity.EXTRA_FAVORITE_RECIPE_UID_LIST, new ArrayList<>(favoriteList.getRecipeIds()));
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), "Error opening list or list is empty.", Toast.LENGTH_SHORT).show();
        }
    }

    // The rest of the methods (onFavoriteListLongClick, onCreateContextMenu, etc.)
    // are part of the original, correct class structure and should remain.
    // The SEARCH block above was intended to remove the *second* erroneous class definition.
    // The key is that the corrected onFavoriteListClick should be part of the *first* class definition.

    // The following methods are part of the FIRST (correct) class definition and should remain
    @Override
    public void onFavoriteListLongClick(FavoriteList favoriteList, View view) {
        selectedFavoriteListForContextMenu = favoriteList;
        if (getActivity() != null) {
            getActivity().openContextMenu(view);
        }
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (getActivity() != null && selectedFavoriteListForContextMenu != null) {
            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.favorite_list_context_menu, menu);
            menu.setHeaderTitle("Manage List: " + selectedFavoriteListForContextMenu.getName());
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (selectedFavoriteListForContextMenu == null || getContext() == null) {
            return super.onContextItemSelected(item);
        }

        int itemId = item.getItemId();
        if (itemId == R.id.action_rename_list) {
            showRenameListDialog(selectedFavoriteListForContextMenu);
            return true;
        } else if (itemId == R.id.action_delete_list) {
            showDeleteListConfirmDialog(selectedFavoriteListForContextMenu);
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }

    private void showRenameListDialog(final FavoriteList listToRename) {
        if (getContext() == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Rename List");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setText(listToRename.getName());
        input.setSelection(input.getText().length());
        builder.setView(input);

        builder.setPositiveButton("Rename", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty() && !newName.equals(listToRename.getName())) {
                if (FavoriteListManager.renameFavoriteList(getContext(), listToRename.getId(), newName)) {
                    loadFavoriteLists();
                    Toast.makeText(getContext(), "List renamed to '" + newName + "'.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to rename list.", Toast.LENGTH_SHORT).show();
                }
            } else if (newName.isEmpty()) {
                Toast.makeText(getContext(), "List name cannot be empty.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showDeleteListConfirmDialog(final FavoriteList listToDelete) {
        if (getContext() == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle("Delete List")
                .setMessage("Are you sure you want to delete the list '" + listToDelete.getName() + "'? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (FavoriteListManager.deleteFavoriteList(getContext(), listToDelete.getId())) {
                        loadFavoriteLists();
                        Toast.makeText(getContext(), "List '" + listToDelete.getName() + "' deleted.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Failed to delete list.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
