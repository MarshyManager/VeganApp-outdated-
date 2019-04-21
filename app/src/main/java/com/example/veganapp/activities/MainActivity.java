package com.example.veganapp.activities;

import androidx.fragment.app.FragmentTransaction;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.veganapp.R;
import com.example.veganapp.db_classes.Recipe;
import com.example.veganapp.db_classes.Restaurant;

import com.example.veganapp.fragments.CookInstructionFragment;
import com.example.veganapp.fragments.MapFragment;
import com.example.veganapp.fragments.RecipesFragment;
import com.google.firebase.FirebaseException;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yandex.mapkit.MapKitFactory;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements RecipesFragment.OnRecipeListFragmentInteractionListener,
        RecipesFragment.OnRecipeLikeFragmentInteractionListener, MapFragment.OnFragmentInteractionListener {

    static final String APP_PREFERENCES = "settings";
    static final String RECIPES = "recipes";
    static final String FULL_RECIPE = "full_recipe";


    protected List<Recipe> recipes;
    protected List<Restaurant> restaurants;
    protected FirebaseDatabase mDB;
    protected FragmentManager fm;
    protected FragmentTransaction ftrans;
    protected SharedPreferences shp;

    protected DatabaseReference DBRecipes;

    protected ProgressBar progressBar;
    protected BottomNavigationView navigation;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            RecipesFragment recipesFragment;
            fm = getSupportFragmentManager();
            progressBar.setVisibility(View.VISIBLE);
            fm.popBackStack(FULL_RECIPE, 0);
            ftrans = fm.beginTransaction();
            switch (item.getItemId()) {
                case R.id.navigation_recipe_list:
                    recipesFragment = RecipesFragment.newInstance(APP_PREFERENCES, 1, false);
                    ftrans.replace(R.id.fragment_container, recipesFragment).commit();
                    return true;
                case R.id.navigation_favourite:
                    recipesFragment = RecipesFragment.newInstance(APP_PREFERENCES, 1, true);
                    ftrans.replace(R.id.fragment_container, recipesFragment).commit();
                    return true;
                case R.id.navigation_map:
                    MapFragment mapFragment = MapFragment.newInstance(restaurants);
                    ftrans.replace(R.id.fragment_container, mapFragment).commit();
                    progressBar.setVisibility(View.GONE);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MapKitFactory.setApiKey("e85d69c5-1f52-4e55-863a-418618587a97");
        MapKitFactory.initialize(this);

        shp = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);

        progressBar = findViewById(R.id.load_data);

        mDB = FirebaseDatabase.getInstance();
        DBRecipes = mDB.getReference(RECIPES);

//        DatabaseReference DBRestaurants = mDB.getReference(RESTAURANTS);

        recipes = new ArrayList<>();
        restaurants = new ArrayList<>();

        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        fm = getSupportFragmentManager();
        if (fm.getFragments().size() == 0) {
            ftrans = fm.beginTransaction();
            RecipesFragment recipesFragment = RecipesFragment.newInstance(APP_PREFERENCES, 1, false);
            ftrans.replace(R.id.fragment_container, recipesFragment).commit();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        MapKitFactory.getInstance().onStop();
    }

    @Override
    public void onRecipeListFragmentInteraction(Recipe item) {
        ftrans = getSupportFragmentManager().beginTransaction();
        CookInstructionFragment cif = CookInstructionFragment.newInstance(item, APP_PREFERENCES);
        ftrans.replace(R.id.fragment_container, cif).addToBackStack(FULL_RECIPE).commit();
        mDB.getReference().child("recipes").child(item.getId().toString()).child("views").setValue(item.getViews());
    }

    @Override
    public void onRecipeLikeFragmentInteraction(Recipe item, int rate) {
        String s = "recipe_like_" + item.getId();
        SharedPreferences.Editor editor = shp.edit();
        if (!shp.getBoolean(s, false)) {
            editor.putBoolean(s, true);
            ++rate;
        } else {
            editor.putBoolean(s, false);
            --rate;
        }
        editor.apply();
        item.setRate(rate);
        mDB.getReference().child("recipes").child(item.getId().toString()).child("rate").setValue(rate);
    }

    @Override
    public void onBackPressed() {
        if ((navigation.getSelectedItemId() != R.id.navigation_recipe_list)
                && (getSupportFragmentManager().getBackStackEntryCount() == 0))
            navigation.setSelectedItemId(R.id.navigation_recipe_list);
        else
            super.onBackPressed();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}


