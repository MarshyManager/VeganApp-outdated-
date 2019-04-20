package com.example.veganapp.activities;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

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
    final String RECIPES = "recipes";
    final String RESTAURANTS = "restaurants";

    protected List<Recipe> recipes;
    protected List<Restaurant> restaurants;
    protected FirebaseDatabase mDB;
    protected FragmentTransaction ftrans;
    protected SharedPreferences shp;

    DatabaseReference DBRecipes;

    protected ProgressBar progressBar;

    enum ListType {
        RECIPES,
        RESTAURANTS
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            RecipesFragment recipesFragment;
//            progressBar.setVisibility(View.VISIBLE);
            switch (item.getItemId()) {
                case R.id.navigation_recipe_list:
                    recipesFragment = RecipesFragment.newInstance(APP_PREFERENCES, 1, false);
                    ftrans = getFragmentManager().beginTransaction();
                    ftrans.replace(R.id.fragment_container, recipesFragment).commit();
                    return true;
                case R.id.navigation_favourite:
                    recipesFragment = RecipesFragment.newInstance(APP_PREFERENCES, 1, true);
                    ftrans = getFragmentManager().beginTransaction();
                    ftrans.replace(R.id.fragment_container, recipesFragment).commit();
                    return true;
                case R.id.navigation_map:
                    MapFragment mapFragment = MapFragment.newInstance(restaurants);
                    ftrans = getFragmentManager().beginTransaction();
                    ftrans.replace(R.id.fragment_container, mapFragment).commit();
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

        progressBar = findViewById(R.id.loadData);

        mDB = FirebaseDatabase.getInstance();
        DBRecipes = mDB.getReference(RECIPES);
        DatabaseReference DBRestaurants = mDB.getReference(RESTAURANTS);

        recipes = new ArrayList<>();
        restaurants = new ArrayList<>();

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        RecipesFragment recipesFragment = RecipesFragment.newInstance(APP_PREFERENCES, 1, false);
        ftrans = getFragmentManager().beginTransaction();
        ftrans.replace(R.id.fragment_container, recipesFragment).commit();
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
        ftrans = getFragmentManager().beginTransaction();
        CookInstructionFragment cif = CookInstructionFragment.newInstance(item, APP_PREFERENCES);
        ftrans.replace(R.id.fragment_container, cif).addToBackStack(null).commit();
        mDB.getReference().child("recipes").child(item.getId().toString()).child("views").setValue(item.getViews());
    }

    @Override
    public void onRecipeLikeFragmentInteraction(Recipe item) throws FirebaseException {
        String s = "recipe_like_" + item.getId();
        SharedPreferences.Editor editor = shp.edit();
        if (mDB.getReference().child("recipes").child(item.getId().toString()).child("rate").setValue(item.getRate()).isSuccessful()) {
            if (!shp.getBoolean(s, false))
                editor.putBoolean(s, true);
            else
                editor.putBoolean(s, false);
            editor.apply();
        } else throw new FirebaseException("Database is not available");
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}

