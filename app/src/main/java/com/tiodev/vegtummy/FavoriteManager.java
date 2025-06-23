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
import java.util.List;

public class FavoriteManager {

    private static final String FILE_NAME = "favorites.json";
    private static final String TAG = "FavoriteManager";

    // Helper to read the JSON file and return a JSONArray
    private static JSONArray readFavoritesFile(Context context) {
        try {
            File file = new File(context.getFilesDir(), FILE_NAME);
            if (!file.exists()) {
                // Create an empty JSON array if the file doesn't exist
                return new JSONArray();
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
                return new JSONArray(); // Return empty array if file is empty
            }
            return new JSONArray(jsonString);
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Error reading favorites file", e);
            return new JSONArray(); // Return empty array on error
        }
    }

    // Helper to write a JSONArray to the JSON file
    private static void writeFavoritesFile(Context context, JSONArray jsonArray) {
        try {
            FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            fos.write(jsonArray.toString().getBytes(StandardCharsets.UTF_8));
            fos.close();
        } catch (IOException e) {
            Log.e(TAG, "Error writing favorites file", e);
        }
    }

    public static List<String> getFavoriteRecipeIds(Context context) {
        JSONArray jsonArray = readFavoritesFile(context);
        List<String> favoriteIds = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                favoriteIds.add(jsonArray.getString(i));
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing favorite ID from JSON array", e);
            }
        }
        return favoriteIds;
    }

    public static void addFavorite(Context context, String recipeId) {
        if (recipeId == null || recipeId.isEmpty()) {
            Log.w(TAG, "Recipe ID is null or empty, cannot add to favorites.");
            return;
        }
        JSONArray jsonArray = readFavoritesFile(context);
        // Check if already favorite to prevent duplicates
        boolean found = false;
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                if (recipeId.equals(jsonArray.getString(i))) {
                    found = true;
                    break;
                }
            } catch (JSONException e) {
                // Should not happen if write logic is correct
            }
        }
        if (!found) {
            jsonArray.put(recipeId);
            writeFavoritesFile(context, jsonArray);
        }
    }

    public static void removeFavorite(Context context, String recipeId) {
        if (recipeId == null || recipeId.isEmpty()) {
            Log.w(TAG, "Recipe ID is null or empty, cannot remove from favorites.");
            return;
        }
        JSONArray jsonArray = readFavoritesFile(context);
        JSONArray newJsonArray = new JSONArray();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                if (!recipeId.equals(jsonArray.getString(i))) {
                    newJsonArray.put(jsonArray.getString(i));
                }
            } catch (JSONException e) {
                 Log.e(TAG, "Error processing favorite ID during removal", e);
            }
        }
        writeFavoritesFile(context, newJsonArray);
    }

    public static boolean isFavorite(Context context, String recipeId) {
        if (recipeId == null || recipeId.isEmpty()) {
            return false;
        }
        JSONArray jsonArray = readFavoritesFile(context);
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                if (recipeId.equals(jsonArray.getString(i))) {
                    return true;
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error comparing favorite ID", e);
            }
        }
        return false;
    }
}
