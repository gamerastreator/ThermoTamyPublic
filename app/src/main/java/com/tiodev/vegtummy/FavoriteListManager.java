package com.tiodev.vegtummy;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class FavoriteListManager {

    private static final String FILE_NAME = "favorites_lists.json";
    private static final String TAG = "FavoriteListManager";

    // Helper to read the main JSON object from the file
    private static JSONObject readListsFile(Context context) {
        try {
            File file = new File(context.getFilesDir(), FILE_NAME);
            if (!file.exists()) {
                return new JSONObject(); // Return empty object if file doesn't exist
            }

            FileInputStream fis = context.openFileInput(FILE_NAME);
            InputStreamReader inputStreamReader = new InputStreamReader(fis, StandardCharsets.UTF_8);
            StringBuilder stringBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
                String line = reader.readLine();
                while (line != null) {
                    stringBuilder.append(line);
                    line = reader.readLine();
                }
            }
            String jsonString = stringBuilder.toString();
            if (jsonString.isEmpty()) {
                return new JSONObject(); // Return empty object if file is empty
            }
            return new JSONObject(jsonString);
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Error reading favorites lists file", e);
            return new JSONObject(); // Return empty object on error
        }
    }

    // Helper to write the main JSON object to the file
    private static void writeListsFile(Context context, JSONObject listsJson) {
        try {
            FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            fos.write(listsJson.toString(2).getBytes(StandardCharsets.UTF_8)); // Using 2 for indentation
            fos.close();
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Error writing favorites lists file", e);
        }
    }

    public static FavoriteList createFavoriteList(Context context, String listName) {
        if (listName == null || listName.trim().isEmpty()) {
            Log.w(TAG, "List name cannot be empty.");
            return null;
        }

        JSONObject listsJson = readListsFile(context);
        String listId = UUID.randomUUID().toString();

        JSONObject newListJson = new JSONObject();
        try {
            newListJson.put("name", listName.trim());
            newListJson.put("recipeIds", new JSONArray());
            listsJson.put(listId, newListJson);
            writeListsFile(context, listsJson);
            return new FavoriteList(listId, listName.trim());
        } catch (JSONException e) {
            Log.e(TAG, "Error creating new list JSON object", e);
            return null;
        }
    }

    public static boolean deleteFavoriteList(Context context, String listId) {
        if (listId == null || listId.isEmpty()) return false;
        JSONObject listsJson = readListsFile(context);
        if (listsJson.has(listId)) {
            listsJson.remove(listId);
            writeListsFile(context, listsJson);
            return true;
        }
        return false;
    }

    public static boolean renameFavoriteList(Context context, String listId, String newListName) {
        if (listId == null || listId.isEmpty() || newListName == null || newListName.trim().isEmpty()) {
            return false;
        }
        JSONObject listsJson = readListsFile(context);
        if (listsJson.has(listId)) {
            try {
                JSONObject listJson = listsJson.getJSONObject(listId);
                listJson.put("name", newListName.trim());
                writeListsFile(context, listsJson);
                return true;
            } catch (JSONException e) {
                Log.e(TAG, "Error renaming list: " + listId, e);
                return false;
            }
        }
        return false;
    }

    public static boolean addRecipeToList(Context context, String listId, String recipeId) {
        if (listId == null || recipeId == null || recipeId.isEmpty()) return false;
        JSONObject listsJson = readListsFile(context);
        if (listsJson.has(listId)) {
            try {
                JSONObject listJson = listsJson.getJSONObject(listId);
                JSONArray recipeIdsJson = listJson.getJSONArray("recipeIds");
                // Check if recipeId already exists
                for (int i = 0; i < recipeIdsJson.length(); i++) {
                    if (recipeId.equals(recipeIdsJson.getString(i))) {
                        return true; // Already exists, no change needed but operation is "successful"
                    }
                }
                recipeIdsJson.put(recipeId);
                writeListsFile(context, listsJson);
                return true;
            } catch (JSONException e) {
                Log.e(TAG, "Error adding recipe to list: " + listId, e);
                return false;
            }
        }
        return false; // List not found
    }

    public static boolean removeRecipeFromList(Context context, String listId, String recipeId) {
        if (listId == null || recipeId == null) return false;
        JSONObject listsJson = readListsFile(context);
        if (listsJson.has(listId)) {
            try {
                JSONObject listJson = listsJson.getJSONObject(listId);
                JSONArray recipeIdsJson = listJson.getJSONArray("recipeIds");
                JSONArray newRecipeIdsJson = new JSONArray();
                boolean found = false;
                for (int i = 0; i < recipeIdsJson.length(); i++) {
                    if (recipeId.equals(recipeIdsJson.getString(i))) {
                        found = true;
                    } else {
                        newRecipeIdsJson.put(recipeIdsJson.getString(i));
                    }
                }
                if (found) {
                    listJson.put("recipeIds", newRecipeIdsJson);
                    writeListsFile(context, listsJson);
                }
                return found;
            } catch (JSONException e) {
                Log.e(TAG, "Error removing recipe from list: " + listId, e);
                return false;
            }
        }
        return false; // List not found
    }

    public static List<FavoriteList> getFavoriteLists(Context context) {
        JSONObject listsJson = readListsFile(context);
        List<FavoriteList> favLists = new ArrayList<>();
        Iterator<String> keys = listsJson.keys();
        while (keys.hasNext()) {
            String listId = keys.next();
            try {
                JSONObject listJson = listsJson.getJSONObject(listId);
                String name = listJson.getString("name");
                JSONArray recipeIdsJson = listJson.getJSONArray("recipeIds");
                List<String> recipeIds = new ArrayList<>();
                for (int i = 0; i < recipeIdsJson.length(); i++) {
                    recipeIds.add(recipeIdsJson.getString(i));
                }
                favLists.add(new FavoriteList(listId, name, recipeIds));
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing list: " + listId, e);
            }
        }
        return favLists;
    }

    public static FavoriteList getFavoriteListById(Context context, String listId) {
        if (listId == null || listId.isEmpty()) return null;
        JSONObject listsJson = readListsFile(context);
         if (listsJson.has(listId)) {
            try {
                JSONObject listJson = listsJson.getJSONObject(listId);
                String name = listJson.getString("name");
                JSONArray recipeIdsJson = listJson.getJSONArray("recipeIds");
                List<String> recipeIds = new ArrayList<>();
                for (int i = 0; i < recipeIdsJson.length(); i++) {
                    recipeIds.add(recipeIdsJson.getString(i));
                }
                return new FavoriteList(listId, name, recipeIds);
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing list: " + listId, e);
            }
        }
        return null;
    }


    public static List<String> getRecipesInList(Context context, String listId) {
        FavoriteList list = getFavoriteListById(context, listId);
        if (list != null) {
            return list.getRecipeIds(); // Returns a copy
        }
        return new ArrayList<>(); // Return empty list if list not found
    }

    public static boolean isRecipeInList(Context context, String listId, String recipeId) {
        if (recipeId == null) return false;
        List<String> recipes = getRecipesInList(context, listId);
        return recipes.contains(recipeId);
    }

    public static List<FavoriteList> getAllListsContainingRecipe(Context context, String recipeId) {
        if (recipeId == null) return new ArrayList<>();
        List<FavoriteList> allLists = getFavoriteLists(context);
        List<FavoriteList> listsContainingRecipe = new ArrayList<>();
        for (FavoriteList list : allLists) {
            if (list.getRecipeIds().contains(recipeId)) {
                listsContainingRecipe.add(list);
            }
        }
        return listsContainingRecipe;
    }

    public static boolean isRecipeInAnyList(Context context, String recipeId) {
        if (recipeId == null) return false;
        List<FavoriteList> allLists = getFavoriteLists(context);
        for (FavoriteList list : allLists) {
            if (list.getRecipeIds().contains(recipeId)) {
                return true;
            }
        }
        return false;
    }

    // Method to delete the old favorites.json file if it exists
    public static void deleteOldFavoritesFile(Context context) {
        File oldFile = new File(context.getFilesDir(), "favorites.json");
        if (oldFile.exists()) {
            if (oldFile.delete()) {
                Log.i(TAG, "Old favorites.json file deleted successfully.");
            } else {
                Log.w(TAG, "Failed to delete old favorites.json file.");
            }
        }
    }
}
