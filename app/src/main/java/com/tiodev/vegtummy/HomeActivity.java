package com.tiodev.vegtummy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.tiodev.vegtummy.Adapter.AdapterPopular;
import com.tiodev.vegtummy.Model.ResModel;
import com.tiodev.vegtummy.RoomDB.AppDatabase;
import com.tiodev.vegtummy.RoomDB.User;
import com.google.android.material.bottomnavigation.BottomNavigationView; // Added
import com.tiodev.vegtummy.RoomDB.UserDao;

import java.util.ArrayList;
import java.util.List;

// Import Fragment and FragmentManager
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        // Load the default fragment (HomeFragment)
        if (savedInstanceState == null) { // Important to avoid recreating fragment on config change
            loadFragment(new HomeFragment(), false); // Load HomeFragment by default
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            item -> {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_home) {
                    selectedFragment = new HomeFragment();
                } else if (itemId == R.id.navigation_favorites) {
                    selectedFragment = new FavoritesFragment();
                } else if (itemId == R.id.navigation_collections) {
                    selectedFragment = new CollectionsFragment();
                }

                if (selectedFragment != null) {
                    loadFragment(selectedFragment, true); // Replace existing fragment
                }
                return true;
            };

    private void loadFragment(Fragment fragment, boolean replace) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (replace) {
            transaction.replace(R.id.fragment_container, fragment);
        } else {
            // Initial load, just add
            transaction.add(R.id.fragment_container, fragment);
        }
        // transaction.addToBackStack(null); // Optional: if you want back stack behavior for fragments
        transaction.commit();
    }

    // The methods setPopularList(), start(), and showBottomSheet() have been moved
    // or are no longer relevant here as their UI components are in HomeFragment.
    // If start() is a general utility, it could be made static or moved to a helper class
    // if other activities need it. For now, HomeFragment has its own startMainActivity.
}