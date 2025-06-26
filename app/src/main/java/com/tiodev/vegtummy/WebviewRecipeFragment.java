package com.tiodev.vegtummy;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import com.tiodev.vegtummy.RoomDB.AppDatabase;
import com.tiodev.vegtummy.RoomDB.User;
import com.tiodev.vegtummy.RoomDB.UserDao;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class WebviewRecipeFragment extends Fragment {

    // Argument Keys
    private static final String ARG_RECIPE_ID = "recipeId";
    private static final String ARG_HTML_PATH = "htmlPath";
    private static final String ARG_TITLE = "title";
    private static final String ARG_IS_DEEP_LINK = "isDeepLink";

    // UI Elements
    ImageView backBtn;
    WebView webview;
    ImageButton favoriteBtnWebview;
    ImageButton shareBtnWebview;

    // Data
    String recipeId;
    String fragmentTitle; // Renamed from activityTitle
    String htmlPathToLoad;

    private UserDao userDao;
    private static final String TAG_DEEPLINK = "WebviewRecipeDeepLink"; // Copied from Activity

    public WebviewRecipeFragment() {
        // Required empty public constructor
    }

    // Factory method for standard navigation
    public static WebviewRecipeFragment newInstance(String recipeId, String htmlPath, String title) {
        WebviewRecipeFragment fragment = new WebviewRecipeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_RECIPE_ID, recipeId);
        args.putString(ARG_HTML_PATH, htmlPath);
        args.putString(ARG_TITLE, title);
        args.putBoolean(ARG_IS_DEEP_LINK, false);
        fragment.setArguments(args);
        return fragment;
    }

    // Factory method for deep link navigation
    public static WebviewRecipeFragment newInstanceForDeepLink(String recipeId) {
        WebviewRecipeFragment fragment = new WebviewRecipeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_RECIPE_ID, recipeId);
        args.putBoolean(ARG_IS_DEEP_LINK, true);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppDatabase db = Room.databaseBuilder(requireContext().getApplicationContext(),
                        AppDatabase.class, "db_name").allowMainThreadQueries()
                .createFromAsset("database/recipe.db")
                .fallbackToDestructiveMigration()
                .build();
        userDao = db.userDao();

        if (getArguments() != null) {
            boolean isDeepLink = getArguments().getBoolean(ARG_IS_DEE_LINK, false);
            this.recipeId = getArguments().getString(ARG_RECIPE_ID);

            if (isDeepLink) {
                Log.d(TAG_DEEPLINK, "Fragment launched with deep link for recipeId: " + this.recipeId);
                if (this.recipeId != null && !this.recipeId.isEmpty()) {
                    try {
                        int deepLinkedUid = Integer.parseInt(this.recipeId);
                        User user = userDao.getUserById(deepLinkedUid); // DB access on main thread
                        if (user != null) {
                            this.fragmentTitle = user.getTitle();
                            this.htmlPathToLoad = "data/" + user.getIdentifier() + ".html";
                            Log.d(TAG_DEEPLINK, "Deep link success: fragmentTitle=" + this.fragmentTitle + ", path=" + this.htmlPathToLoad);
                        } else {
                            Log.e(TAG_DEEPLINK, "No user found for deep linked ID: " + deepLinkedUid);
                        }
                    } catch (NumberFormatException e) {
                        Log.e(TAG_DEEPLINK, "Invalid recipe ID format in deep link: " + this.recipeId, e);
                    }
                } else {
                    Log.e(TAG_DEEPLINK, "Recipe ID missing or empty in deep link arguments.");
                }
            } else {
                // Standard navigation, values passed directly
                this.htmlPathToLoad = getArguments().getString(ARG_HTML_PATH);
                this.fragmentTitle = getArguments().getString(ARG_TITLE);
                Log.d(TAG_DEEPLINK, "Fragment launched with normal arguments: recipeId=" + this.recipeId + ", path=" + this.htmlPathToLoad);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_webview_recipe, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        webview = view.findViewById(R.id.webView);
        backBtn = view.findViewById(R.id.back_btn);
        favoriteBtnWebview = view.findViewById(R.id.favorite_btn_webview);
        shareBtnWebview = view.findViewById(R.id.share_btn_webview);

        // Check if essential data is loaded
        if (this.recipeId == null || this.recipeId.isEmpty() || this.htmlPathToLoad == null || this.htmlPathToLoad.isEmpty()) {
            if (isAdded() && getContext() != null) {
                 Toast.makeText(getContext(), "Error: Could not load recipe details.", Toast.LENGTH_LONG).show();
            }
            Log.e(TAG_DEEPLINK, "Finishing fragment due to missing recipeId or htmlPath. recipeId: " + this.recipeId + ", htmlPath: " + this.htmlPathToLoad);
            // Pop back stack or handle error appropriately for a fragment
            getParentFragmentManager().popBackStack();
            return;
        }

        // Set activity title (if applicable and HomeActivity has a visible ActionBar/Toolbar)
        if (getActivity() != null && fragmentTitle != null) {
            getActivity().setTitle(fragmentTitle);
        }

        initializeWebView(this.htmlPathToLoad);
        updateUiElements(); // Setup button listeners etc.
    }

    // Methods to be transferred and adapted:
    // initializeWebView(String htmlPath)
    // updateUiElements()
    // shareRecipe()
    // setFavoriteButtonState()
    // handleFavoriteButtonClick()
    // showManageFavoriteListsDialog()
    // loadJSONFromAsset(String name)

    // Placeholder for initializeWebView - to be filled in next stage
    @SuppressLint("SetJavaScriptEnabled")
    private void initializeWebView(String htmlPath) {
        if (webview == null || htmlPath == null || htmlPath.isEmpty() || getContext() == null || !isAdded()) {
            Log.e(TAG_DEEPLINK, "WebView, HTML path, or context is null/empty in initializeWebView or fragment not added.");
            if (isAdded() && getContext() != null) {
                Toast.makeText(getContext(), "Error initializing recipe view.", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);

        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (getActivity() == null || !isAdded()) return true; // Prevent action if fragment not attached

                if (url.contains("Vrk") || url.contains("collection")) {
                    String collectionIdentifier = url.substring(url.lastIndexOf("/") + 1);
                    String collectionTitle = loadJSONFromAsset(collectionIdentifier);
                    if (collectionTitle != null && !collectionTitle.isEmpty()) {
                        SearchFragment searchFragment = SearchFragment.newInstance(collectionTitle);
                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, searchFragment) // Assuming R.id.fragment_container from HomeActivity
                                .addToBackStack(null)
                                .commit();

                        // Update BottomNavigationView in HomeActivity to select "Search"
                        if (getActivity() instanceof HomeActivity) {
                            BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
                            if (bottomNav != null) {
                                bottomNav.setSelectedItemId(R.id.navigation_launch_search);
                            }
                        }
                        return true;
                    }
                    return true; // Stay in current webview if collection title not found
                } else if (url.startsWith("file:///android_asset/")) {
                    // Allow loading of other local HTML files directly if path is correct
                    // For simplicity, this example only reloads if it's a specific pattern,
                    // otherwise, it lets the WebView handle it or load externally.
                    // The original logic was: webview.loadUrl("file:///android_asset/data/" + id + ".html");
                    // This needs to be more robust if general local file navigation is expected.
                    // For now, let WebView handle it unless it's a collection link.
                    // view.loadUrl(url); // This might cause loops if not careful.
                    // return true;       // We are handling it.
                    return false; // Let webview handle other file:/// links for now.
                }
                // For external links, you might want to open them in a browser
                // Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                // startActivity(intent);
                // return true;
                return false; // Let WebView handle other URLs (e.g. external http/https)
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(final WebView view, String url) {
                if (getContext() == null || !isAdded()) return null;

                if (url.contains(".js")) {
                    return null;
                } else if (url.contains(".css")) {
                    String fileName = url.substring(url.lastIndexOf('/') + 1);
                    String assetPath = "data/static/" + fileName;
                    try {
                        return new WebResourceResponse("text/css", "UTF-8", getContext().getAssets().open(assetPath));
                    } catch (IOException e) {
                        Log.e(TAG_DEEPLINK, "CSS not found in assets: " + assetPath, e);
                        return null;
                    }
                } else if (url.contains(".woff2")) {
                    String fileName = url.substring(url.lastIndexOf('/') + 1);
                    String assetPath = "data/static/" + fileName;
                    try {
                        return new WebResourceResponse("font/woff2", "UTF-8", getContext().getAssets().open(assetPath));
                    } catch (IOException e) {
                        Log.e(TAG_DEEPLINK, "Font not found in assets: " + assetPath, e);
                        return null;
                    }
                }
                return super.shouldInterceptRequest(view, url);
            }
        });
        webview.loadUrl("file:///android_asset/" + htmlPath);
    }

    // Placeholder for updateUiElements - to be filled
    private void updateUiElements() {
        if (getActivity() == null || !isAdded()) return;

        if (fragmentTitle != null) {
            // For a fragment, setting the title might involve communicating with the host Activity
            // if it has a Toolbar or ActionBar.
            // For now, let's assume HomeActivity might handle this if it observes fragment changes.
            // Or, if the title is purely for the fragment's internal use (e.g. sharing).
            // getActivity().setTitle(fragmentTitle); // This would set HomeActivity's title
        }

        if (this.recipeId != null && !this.recipeId.isEmpty()) {
            if (favoriteBtnWebview != null) {
                favoriteBtnWebview.setEnabled(true);
                setFavoriteButtonState(); // To be implemented
                favoriteBtnWebview.setOnClickListener(v -> handleFavoriteButtonClick()); // To be implemented
            }
            if (shareBtnWebview != null) {
                shareBtnWebview.setEnabled(true);
                shareBtnWebview.setOnClickListener(v -> shareRecipe()); // To be implemented
            }
        } else {
            if (favoriteBtnWebview != null) favoriteBtnWebview.setEnabled(false);
            if (shareBtnWebview != null) shareBtnWebview.setEnabled(false);
            if (isAdded() && getContext() != null) {
                 Toast.makeText(getContext(), "Recipe ID not found, actions disabled.", Toast.LENGTH_LONG).show();
            }
        }

        if (backBtn != null) {
            backBtn.setOnClickListener(v -> {
                if (getParentFragmentManager() != null) {
                    getParentFragmentManager().popBackStack();
                }
            });
        }
    }

    // Placeholder for shareRecipe - to be filled
    private void shareRecipe() {
        if (getContext() == null || !isAdded() || recipeId == null || recipeId.isEmpty()) {
            if (isAdded() && getContext() != null) {
                Toast.makeText(getContext(), "Cannot share recipe: ID missing.", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        String deepLink = "vegtummy://recipe?id=" + recipeId;
        String shareMessage = "Check out this recipe";
        if (fragmentTitle != null && !fragmentTitle.isEmpty()) {
            shareMessage += " '" + fragmentTitle + "'";
        }
        shareMessage += " in VegTummy: " + deepLink;

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Recipe from VegTummy: " + (fragmentTitle != null ? fragmentTitle : ""));
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Share recipe via"));
    }

    // Placeholder for setFavoriteButtonState - to be filled
    private void setFavoriteButtonState() {
        if (getContext() == null || !isAdded() || recipeId == null || recipeId.isEmpty() || favoriteBtnWebview == null) {
            if(favoriteBtnWebview != null) favoriteBtnWebview.setImageResource(R.drawable.ic_favorite_border);
            return;
        }
        // Use requireContext() for FavoriteListManager as context is essential here
        if (FavoriteListManager.isRecipeInAnyList(requireContext(), recipeId)) {
            favoriteBtnWebview.setImageResource(R.drawable.ic_favorite_filled);
        } else {
            favoriteBtnWebview.setImageResource(R.drawable.ic_favorite_border);
        }
    }

    // Placeholder for handleFavoriteButtonClick - to be filled
    private void handleFavoriteButtonClick() {
         if (getContext() == null || !isAdded() || recipeId == null || recipeId.isEmpty()) {
            if (isAdded() && getContext() != null) {
                 Toast.makeText(getContext(), "Recipe ID not found.", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        showManageFavoriteListsDialog(); // To be implemented
    }

    // Placeholder for showManageFavoriteListsDialog - to be filled
    private void showManageFavoriteListsDialog() {
        if (getContext() == null || !isAdded() || recipeId == null || recipeId.isEmpty()) return;

        List<FavoriteList> allLists = FavoriteListManager.getFavoriteLists(requireContext());
        final boolean[] checkedItems = new boolean[allLists.size()];
        final String[] listIds = new String[allLists.size()];
        String[] listNames = new String[allLists.size()];

        for (int i = 0; i < allLists.size(); i++) {
            FavoriteList list = allLists.get(i);
            listIds[i] = list.getId();
            listNames[i] = list.getName();
            checkedItems[i] = FavoriteListManager.isRecipeInList(requireContext(), list.getId(), recipeId);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        // These string resources should be created
        builder.setTitle("Add to Favorite Lists"); // TODO: Externalize string R.string.dialog_title_add_to_favorites

        builder.setMultiChoiceItems(listNames, checkedItems, (dialog, which, isChecked) -> {
            checkedItems[which] = isChecked;
        });

        builder.setPositiveButton("Save", (dialog, which) -> { // TODO: Externalize string R.string.button_save
            for (int i = 0; i < listIds.length; i++) {
                String listId = listIds[i];
                if (checkedItems[i]) {
                    FavoriteListManager.addRecipeToList(requireContext(), listId, recipeId);
                } else {
                    FavoriteListManager.removeRecipeFromList(requireContext(), listId, recipeId);
                }
            }
            setFavoriteButtonState();
            // TODO: Externalize string R.string.toast_favorite_lists_updated
            Toast.makeText(getContext(), "Favorite lists updated.", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss()); // TODO: Externalize string R.string.button_cancel (if not already existing)

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Placeholder for loadJSONFromAsset - to be filled
    public String loadJSONFromAsset(String name) {
        if (getContext() == null || !isAdded()) return null;
        String json = null;
        try {
            InputStream is = getContext().getAssets().open("data/static/collections.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
            JSONArray obj = new JSONArray(json);
            for (int i = 0; i < obj.length(); i++) {
                JSONObject currObject = obj.getJSONObject(i);
                String id = currObject.getString("id");
                if(id.equals(name)) {
                  return currObject.getString("title");
                }
            }
        } catch (IOException ex) {
            Log.e(TAG_DEEPLINK, "IOException in loadJSONFromAsset", ex);
            return null;
        } catch (JSONException e) {
            Log.e(TAG_DEEPLINK, "JSONException in loadJSONFromAsset", e);
            return null;
        }
        return null; // Return null if not found or error
    }
}
