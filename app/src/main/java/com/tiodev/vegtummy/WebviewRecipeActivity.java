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
    ImageButton favoriteBtnWebview; // Added
    String recipeId; // Added to store recipe ID

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        // Find views
        webview = findViewById(R.id.webView);
        backBtn = findViewById(R.id.back_btn);
        favoriteBtnWebview = findViewById(R.id.favorite_btn_webview); // Initialize favorite button

        // Get Recipe ID from Intent (to be added by SearchAdapter)
        recipeId = getIntent().getStringExtra("id");

        if (recipeId == null || recipeId.isEmpty()) {
            // Fallback or error handling if ID is not passed correctly
            // Try to use title as a fallback if SearchAdapter also passes title for ID, though not ideal.
            // For now, if ID is truly missing, disable button.
            // recipeId = getIntent().getStringExtra("tittle"); // Example fallback, but ID from UID is better
            if (favoriteBtnWebview != null) { // Check if button exists before disabling
                 Toast.makeText(this, "Recipe ID not found, favoriting disabled.", Toast.LENGTH_LONG).show();
                 favoriteBtnWebview.setEnabled(false);
                 favoriteBtnWebview.setVisibility(View.GONE); // Hide if not usable
            }
        } else {
            if (favoriteBtnWebview != null) {
                setFavoriteButtonState();
                favoriteBtnWebview.setOnClickListener(v -> toggleFavoriteStatus());
            }
        }


        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url)
            {
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                if (url.contains("Vrk") || url.contains("collection")) {
                    var collectionname = url.split("/")[url.split("/").length-1];
                    String result = loadJSONFromAsset(collectionname);
                    if (!result.isEmpty()) {
                        Intent intent = new Intent(WebviewRecipeActivity.this,SearchActivity.class);

                        intent.putExtra("collection", result);
                        //startActivity(intent);
                        view.getContext().startActivity(intent);
                        finish();
                        //webViewContext.startActivity(intent);
                        return false;
                    }

                } else if(url.contains("file:///")) {
                    var id = url.split("/")[url.split("/").length-1];
                    webview.loadUrl("file:///android_asset/data/" + id + ".html");
                }
                //url.contains("collection") filtrar por el ultimo path
                return false;
            }
            @Override
            public WebResourceResponse shouldInterceptRequest (final WebView view, String url) {
                if (url.contains(".js") ) {
                    return null;
                } else if (url.contains(".css")) {
                    var url2 = "file:///android_asset/data/static/" + url.split("/")[url.split("/").length-1];
                    var url3 = "data/static/" + url.split("/")[url.split("/").length-1];

                    //view.loadUrl("file:///android_asset/data/static/" + url.split("/")[url.split("/").length-1]);
                    //return null;
                    try {
                        return new WebResourceResponse("text/css", "UTF-8", WebviewRecipeActivity.this.getAssets().open(url3));
                    } catch (IOException e) {
                        ArrayList<String> arrayUrl = new ArrayList(Arrays.asList(url2.split("-")));
                       arrayUrl.remove(arrayUrl.size()-1);

                       url3 = String.join("-", arrayUrl) + ".css";
                        try {
                            return new WebResourceResponse("font/woff2", "UTF-8", WebviewRecipeActivity.this.getAssets().open(url3));
                        } catch (IOException ex) {
                            return null;
                        }

                    }
                } else if (url.contains(".woff2")) {
                    var url2 = "file:///android_asset/data/static/" + url.split("/")[url.split("/").length-1];
                    var url3 = "data/static/" + url.split("/")[url.split("/").length-1];

                    //view.loadUrl("file:///android_asset/data/static/" + url.split("/")[url.split("/").length-1]);
                    //return null;
                    try {
                        return new WebResourceResponse("font/woff2", "UTF-8", WebviewRecipeActivity.this.getAssets().open(url3));
                    } catch (IOException e) {
                        ArrayList<String> arrayUrl = new ArrayList(Arrays.asList(url2.split("-")));
                        arrayUrl.remove(arrayUrl.size()-1);

                        url3 = String.join("-", arrayUrl) + ".woff2";
                        try {
                            return new WebResourceResponse("font/woff2", "UTF-8", WebviewRecipeActivity.this.getAssets().open(url3));
                        } catch (IOException ex) {
                            return null;
                        }

                    }
                }  else {
                    return super.shouldInterceptRequest(view, url);
                }
            }
        });

        webview.loadUrl("file:///android_asset/" + getIntent().getStringExtra("path"));
        // Set recipe title
        // Exit activity
        if (backBtn != null) { // Ensure backBtn is not null if layout changes
            backBtn.setOnClickListener(v -> finish());
        }
    }

    private void setFavoriteButtonState() {
        if (recipeId == null || recipeId.isEmpty() || favoriteBtnWebview == null) return;
        if (FavoriteManager.isFavorite(this, recipeId)) {
            favoriteBtnWebview.setImageResource(R.drawable.ic_favorite_filled);
        } else {
            favoriteBtnWebview.setImageResource(R.drawable.ic_favorite_border);
        }
    }

    private void toggleFavoriteStatus() {
        if (recipeId == null || recipeId.isEmpty() || favoriteBtnWebview == null) return;
        if (FavoriteManager.isFavorite(this, recipeId)) {
            FavoriteManager.removeFavorite(this, recipeId);
            favoriteBtnWebview.setImageResource(R.drawable.ic_favorite_border);
            Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
        } else {
            FavoriteManager.addFavorite(this, recipeId);
            favoriteBtnWebview.setImageResource(R.drawable.ic_favorite_filled);
            Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show();
        }
    }

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