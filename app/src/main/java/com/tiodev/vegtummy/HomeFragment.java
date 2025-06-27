package com.tiodev.vegtummy;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.airbnb.lottie.LottieAnimationView;
import com.tiodev.vegtummy.Adapter.AdapterPopular;
import com.tiodev.vegtummy.Adapter.SearchAdapter;
import com.tiodev.vegtummy.RoomDB.AppDatabase;
import com.tiodev.vegtummy.RoomDB.User;
import com.tiodev.vegtummy.RoomDB.UserDao;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class HomeFragment extends Fragment implements AdapterPopular.OnRecipeClickListener {

    ImageView salad, main, drinks, dessert;
    RecyclerView rcview_home;
    List<User> dataPopular = new ArrayList<>();
    LottieAnimationView lottie;
    EditText editText;
    TextView textview4; // This was used to display recipe count
    AppDatabase appDatabase; // Make AppDatabase an instance variable

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Room database
        // Ensure context is not null, use requireContext() for Fragments
        appDatabase = Room.databaseBuilder(requireContext().getApplicationContext(),
                        AppDatabase.class, "db_name")
                .allowMainThreadQueries() // Consider background thread for DB ops in real apps
                .createFromAsset("database/recipe.db")
                .fallbackToDestructiveMigration()
                .build();

        textview4 = view.findViewById(R.id.textView4);
        salad = view.findViewById(R.id.salad);
        main = view.findViewById(R.id.MainDish);
        drinks = view.findViewById(R.id.Drinks);
        dessert = view.findViewById(R.id.Desserts);
        rcview_home = view.findViewById(R.id.rcview_popular);
        lottie = view.findViewById(R.id.lottie);

        if (rcview_home != null) {
            GridLayoutManager mLayoutManager = new GridLayoutManager(getActivity(),2);
            rcview_home.setLayoutManager(mLayoutManager);
           //rcview_home.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            setPopularList();
        }

        if (salad != null) salad.setOnClickListener(v -> startMainActivity("Salad","Salad"));
        if (main != null) main.setOnClickListener(v -> startMainActivity("Dish", "Main dish"));
        if (drinks != null) drinks.setOnClickListener(v -> startMainActivity("Drinks", "Drinks"));
        if (dessert != null) dessert.setOnClickListener(v -> startMainActivity("Desserts", "Dessert"));

        if (editText != null) editText.setOnClickListener(v ->{
            Intent intent = new Intent(getActivity(), SearchFragment.class);
            startActivity(intent);
        });
    }

    public void setPopularList() {
        if (appDatabase == null || lottie == null || rcview_home == null || textview4 == null) {
            // Views might not be initialized yet, or context issue
            return;
        }
        UserDao userDao = appDatabase.userDao();
        List<User> recipes = userDao.getAll(); // This still loads ALL recipes
        Collections.shuffle(recipes);
        List<User> recipesPopular = recipes.stream().limit(10).collect(Collectors.toList());

        // Original code filtered for "Popular" category, but it was commented out.
        // For now, I'll replicate the behavior in HomeActivity which was to add all to dataPopular.
        // If "Popular" filtering is needed, it should be reinstated here.
        dataPopular.clear(); // Clear before adding

        // The original code in HomeActivity for setPopularList did:
        // AdapterPopular adapter = new AdapterPopular(dataPopular, getApplicationContext());
        // rcview_home.setAdapter(adapter);
        // textview4.setText(textview4.getText() + " " + recipes.size() + " recetas!");
        // lottie.setVisibility(View.GONE);
        // This implies dataPopular should be populated with *popular* recipes, not all.
        // However, the loop for filtering was commented out:
        // /*for(int i = 0; i<recipes.size(); i++){
        // if(recipes.get(i).getCategory().contains("Popular")){
        // dataPopular.add(recipes.get(i));
        // }
        // }*/
        // For consistency with the last active code in HomeActivity, I'll add all recipes to dataPopular.
        // This might not be the intended "Popular" list.
        // To actually show popular recipes, the commented-out loop logic should be used.
        // For now, let's assume "Popular" means all recipes for this list as per HomeActivity's last state.

        // The provided code in HomeActivity passed an empty dataPopular to the Adapter,
        // then set textview4 with recipes.size(). This seems like a bug.
        // The popular list would be empty.
        // I will assume the intent was to show *some* recipes in the popular list.
        // For now, I will add a subset or all to dataPopular for display.
        // Let's add all for now, and it can be refined if "Popular" has a specific meaning.

        dataPopular.addAll(recipesPopular); // Populate dataPopular with fetched recipes

        AdapterPopular adapter = new AdapterPopular(dataPopular, getContext(), (AdapterPopular.OnRecipeClickListener) this);
        rcview_home.setAdapter(adapter);

        if (recipes != null && textview4 != null) {
             String currentText = "Tenemos un total de"; // Base text from XML
             textview4.setText(currentText + " " + recipes.size() + " recetas!");
        }
        lottie.setVisibility(View.GONE);
    }

    // Changed method name to avoid conflict if this fragment is nested
    public void startMainActivity(String category, String title){
        if (getActivity() == null) return;
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.putExtra("Category", category);
        intent.putExtra("tittle", title);
        startActivity(intent);
    }
    public void onRecipeClicked(User recipe) {
        if (getActivity() == null || !isAdded() || recipe == null) {
            return;
        }

        // Construct the htmlPath for WebviewRecipeFragment
        // This assumes recipe.getIdentifier() gives the base name for the .html file
        String htmlPath = "data/" + recipe.getIdentifier() + ".html";
        String recipeIdStr = String.valueOf(recipe.getUid());

        WebviewRecipeFragment webviewFragment = WebviewRecipeFragment.newInstance(
                recipeIdStr,
                htmlPath,
                recipe.getTitle()
        );

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, webviewFragment) // Assumes R.id.fragment_container is in HomeActivity
                .addToBackStack(null) // Allows user to navigate back to search results
                .commit();

        // Optional: Hide keyboard if it was open
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && getView() != null) {
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
    }
}
