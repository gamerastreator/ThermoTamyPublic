package com.tiodev.vegtummy;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton; // Added
import android.widget.ImageView;
import android.widget.Toast; // Added
import android.net.Uri; // Added for deep linking
import android.util.Log; // Added for logging
import androidx.room.Room; // Added for DB access
import com.tiodev.vegtummy.RoomDB.AppDatabase; // Added
import com.tiodev.vegtummy.RoomDB.UserDao; // Added
import com.tiodev.vegtummy.RoomDB.User; // Added


import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class WebviewRecipeActivity extends AppCompatActivity {

    ImageView backBtn;
    WebView webview;
    ImageButton favoriteBtnWebview;
    ImageButton shareBtnWebview; // New share button
    String recipeId; // String UID of the current recipe
    String activityTitle; // Title for the activity
    String htmlPathToLoad; // Path for webview

    private UserDao userDao;
    private static final String TAG_DEEPLINK = "WebviewRecipeDeepLink";

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        webview = findViewById(R.id.webView);
        backBtn = findViewById(R.id.back_btn);
        favoriteBtnWebview = findViewById(R.id.favorite_btn_webview);
        shareBtnWebview = findViewById(R.id.share_btn_webview); // Initialize share button

        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, "db_name").allowMainThreadQueries()
                .createFromAsset("database/recipe.db")
                .fallbackToDestructiveMigration()
                .build();
        userDao = db.userDao();

        final Intent intent = getIntent();
        final String action = intent.getAction();
        final Uri data = intent.getData();

        boolean loadedFromDeepLink = false;
        if (Intent.ACTION_VIEW.equals(action) && data != null) {
            Log.d(TAG_DEEPLINK, "Activity launched with deep link: " + data.toString());
            String deepLinkedIdStr = data.getQueryParameter("id");
            if (deepLinkedIdStr != null && !deepLinkedIdStr.isEmpty()) {
                try {
                    int deepLinkedUid = Integer.parseInt(deepLinkedIdStr);
                    User user = userDao.getUserById(deepLinkedUid);
                    if (user != null) {
                        this.recipeId = String.valueOf(user.getUid()); // Store String UID
                        this.activityTitle = user.getTitle();
                        this.htmlPathToLoad = "data/" + user.getIdentifier() + ".html";
                        Log.d(TAG_DEEPLINK, "Deep link success: recipeId=" + this.recipeId + ", path=" + this.htmlPathToLoad);
                        loadedFromDeepLink = true;
                    } else {
                        Log.e(TAG_DEEPLINK, "No user found for deep linked ID: " + deepLinkedUid);
                    }
                } catch (NumberFormatException e) {
                    Log.e(TAG_DEEPLINK, "Invalid recipe ID format in deep link: " + deepLinkedIdStr, e);
                }
            } else {
                Log.e(TAG_DEEPLINK, "Recipe ID missing in deep link query parameter.");
            }
        }

        if (!loadedFromDeepLink) {
            Log.d(TAG_DEEPLINK, "Activity launched with normal intent.");
            this.recipeId = intent.getStringExtra("id");
            this.htmlPathToLoad = intent.getStringExtra("path");
            this.activityTitle = intent.getStringExtra("tittle");
        }

        if (this.recipeId == null || this.recipeId.isEmpty() || this.htmlPathToLoad == null || this.htmlPathToLoad.isEmpty()) {
            Toast.makeText(this, "Error: Could not load recipe details.", Toast.LENGTH_LONG).show();
            Log.e(TAG_DEEPLINK, "Finishing activity due to missing recipeId or htmlPath. recipeId: " + this.recipeId + ", htmlPath: " + this.htmlPathToLoad);
            finish();
            return;
        }

        initializeWebView(this.htmlPathToLoad);
        updateUiElements();
    }

    private void initializeWebView(String htmlPath) {
        if (webview == null || htmlPath == null || htmlPath.isEmpty()) {
            Log.e(TAG_DEEPLINK, "WebView or HTML path is null/empty in initializeWebView.");
            Toast.makeText(this, "Error initializing recipe view.", Toast.LENGTH_SHORT).show();
            return;
        }
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true); // Consider making this configurable or true by default

        // Existing WebViewClient logic
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {}
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains("Vrk") || url.contains("collection")) {
                    var collectionname = url.split("/")[url.split("/").length-1];
                    String result = loadJSONFromAsset(collectionname); // Ensure loadJSONFromAsset is accessible
                    if (!result.isEmpty()) {
                        Intent intent = new Intent(WebviewRecipeActivity.this, SearchActivity.class);
                        intent.putExtra("collection", result);
                        view.getContext().startActivity(intent);
                        finish();
                        return false; // Return true to indicate we've handled the URL loading
                    }
                } else if(url.contains("file:///")) { // Could be internal navigation within assets
                    var id = url.split("/")[url.split("/").length-1];
                     // This logic might need refinement if it's not just simple HTML files by ID
                    webview.loadUrl("file:///android_asset/data/" + id + ".html");
                    return true; // We've handled it.
                }
                return false; // Let WebView handle other URLs or return true if all handled
            }
            @Override
            public WebResourceResponse shouldInterceptRequest (final WebView view, String url) {
                 if (url.contains(".js") ) { // Allow JS to load from original source if external
                    return null;
                } else if (url.contains(".css")) {
                    var url3 = "data/static/" + url.split("/")[url.split("/").length-1];
                    try {
                        return new WebResourceResponse("text/css", "UTF-8", WebviewRecipeActivity.this.getAssets().open(url3));
                    } catch (IOException e) {
                        // Simplified fallback, original had more complex string manipulation
                        Log.e(TAG_DEEPLINK, "CSS not found: " + url3, e);
                        return null;
                    }
                } else if (url.contains(".woff2")) {
                     var url3 = "data/static/" + url.split("/")[url.split("/").length-1];
                    try {
                        return new WebResourceResponse("font/woff2", "UTF-8", WebviewRecipeActivity.this.getAssets().open(url3));
                    } catch (IOException e) {
                        Log.e(TAG_DEEPLINK, "Font not found: " + url3, e);
                        return null;
                    }
                }  else {
                    return super.shouldInterceptRequest(view, url);
                }
            }
        });
        webview.loadUrl("file:///android_asset/" + htmlPath);
    }

    private void updateUiElements() {
        if (activityTitle != null) {
            setTitle(activityTitle);
        }

        if (this.recipeId != null && !this.recipeId.isEmpty()) {
            if (favoriteBtnWebview != null) {
                //favoriteBtnWebview.setVisibility(View.VISIBLE);
                favoriteBtnWebview.setEnabled(true);
                setFavoriteButtonState();
                favoriteBtnWebview.setOnClickListener(v -> handleFavoriteButtonClick());
            }
            if (shareBtnWebview != null) {
               // shareBtnWebview.setVisibility(View.VISIBLE);
                shareBtnWebview.setEnabled(true);
                shareBtnWebview.setOnClickListener(v -> shareRecipe());
            }
        } else { // No valid recipeId
            if (favoriteBtnWebview != null) {
                favoriteBtnWebview.setEnabled(false);
               // favoriteBtnWebview.setVisibility(View.GONE);
            }
            if (shareBtnWebview != null) {
                shareBtnWebview.setEnabled(false);
               // shareBtnWebview.setVisibility(View.GONE);
            }
            Toast.makeText(this, "Recipe ID not found, actions disabled.", Toast.LENGTH_LONG).show();
        }

        if (backBtn != null) {
            backBtn.setOnClickListener(v -> finish());
        }
    }

    // Methods for favorite list management dialog (if on favorite-lists-management branch)
    // Or simpler favorite toggle (if on recipe-favoriting branch)
    // For now, assuming the multi-list dialog structure exists from previous context
    // If this is the simpler feature/recipe-favoriting branch, setFavoriteButtonState and handleFavoriteButtonClick
    // would use the simpler FavoriteManager.

    private void shareRecipe() {
        if (recipeId == null || recipeId.isEmpty()) {
            Toast.makeText(this, "Cannot share recipe: ID missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        String deepLink = "vegtummy://recipe?id=" + recipeId;
        String shareMessage = "Check out this recipe";
        if (activityTitle != null && !activityTitle.isEmpty()) {
            shareMessage += " '" + activityTitle + "'";
        }
        shareMessage += " in VegTummy: " + deepLink;

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Recipe from VegTummy: " + (activityTitle != null ? activityTitle : ""));
        sendIntent.setType("text/plain");

        // Optional: Target WhatsApp specifically, with fallback to chooser
        // try {
        //     sendIntent.setPackage("com.whatsapp");
        //     startActivity(sendIntent);
        // } catch (android.content.ActivityNotFoundException ex) {
        //     // WhatsApp is not installed, show general chooser
             startActivity(Intent.createChooser(sendIntent, "Share recipe via"));
        // }
    }

    private void setFavoriteButtonState() {
        if (recipeId == null || recipeId.isEmpty() || favoriteBtnWebview == null) {
            if(favoriteBtnWebview != null) favoriteBtnWebview.setImageResource(R.drawable.ic_favorite_border);
            return;
        }
        if (FavoriteListManager.isRecipeInAnyList(this, recipeId)) {
            favoriteBtnWebview.setImageResource(R.drawable.ic_favorite_filled);
        } else {
            favoriteBtnWebview.setImageResource(R.drawable.ic_favorite_border);
        }
    }

    private void handleFavoriteButtonClick() {
        if (recipeId == null || recipeId.isEmpty()) {
            Toast.makeText(this, "Recipe ID not found.", Toast.LENGTH_SHORT).show();
            return;
        }
        showManageFavoriteListsDialog();
    }

    private void showManageFavoriteListsDialog() {
        if (recipeId == null || recipeId.isEmpty()) return;

        List<FavoriteList> allLists = FavoriteListManager.getFavoriteLists(this);
        final boolean[] checkedItems = new boolean[allLists.size()];
        final String[] listIds = new String[allLists.size()];
        String[] listNames = new String[allLists.size()];

        for (int i = 0; i < allLists.size(); i++) {
            FavoriteList list = allLists.get(i);
            listIds[i] = list.getId();
            listNames[i] = list.getName();
            checkedItems[i] = FavoriteListManager.isRecipeInList(this, list.getId(), recipeId);
        }

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Add to Favorite Lists");
        builder.setMultiChoiceItems(listNames, checkedItems, (dialog, which, isChecked) -> {
            checkedItems[which] = isChecked;
        });

        builder.setPositiveButton("Save", (dialog, which) -> {
            for (int i = 0; i < listIds.length; i++) {
                String listId = listIds[i];
                if (checkedItems[i]) {
                    FavoriteListManager.addRecipeToList(this, listId, recipeId);
                } else {
                    FavoriteListManager.removeRecipeFromList(this, listId, recipeId);
                }
            }
            setFavoriteButtonState(); // Update the main heart icon
            Toast.makeText(WebviewRecipeActivity.this, "Favorite lists updated.", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }
    // End of corrected methods for favorite list management

    public String loadJSONFromAsset(String name) {
        String json = null;
        try {
            InputStream is = this.getAssets().open("data/static/collections.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
            JSONArray obj = new JSONArray(json);
            for (int i = 0; i < obj.length(); i++) {
                JSONObject currObject = obj.getJSONObject(i);
                String id = currObject.getString("id");

                if(id.equals(name))
                {

                  return currObject.getString("title");
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return "";
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return "";
    }
}