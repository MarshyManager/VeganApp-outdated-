package com.example.veganapp.activities;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.example.veganapp.R;
import com.example.veganapp.db_classes.Recipe;
import com.example.veganapp.db_classes.Restaurant;

import com.example.veganapp.fragments.CookInstructionFragment;
import com.example.veganapp.fragments.MapFragment;
import com.example.veganapp.fragments.RecipesFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yandex.mapkit.MapKitFactory;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements RecipesFragment.OnRecipeListFragmentInteractionListener,
        RecipesFragment.OnRecipeLikeFragmentInteractionListener, MapFragment.OnFragmentInteractionListener {

    static final String APP_PREFERENCES = "settings";
    static final String RECIPES = "recipes";
    static final String RESTAURANTS = "restaurants";

    protected List<Recipe> recipes;
    protected List<Restaurant> restaurants;
    protected FirebaseDatabase mDB;
    protected FragmentTransaction ftrans;
    protected SharedPreferences shp;



    enum ListType {
        RECIPES,
        RESTAURANTS
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            RecipesFragment recipesFragment;
            switch (item.getItemId()) {
                case R.id.navigation_recipe_list:
                    recipesFragment = RecipesFragment.newInstance(shp, recipes, 1, false);
                    new ChangeFragment(recipesFragment).execute(RECIPES);
                    return true;
                case R.id.navigation_favourite:
                    recipesFragment = RecipesFragment.newInstance(shp, recipes, 1, true);
                    new ChangeFragment(recipesFragment).execute(RECIPES);
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

        mDB = FirebaseDatabase.getInstance();
        DatabaseReference DBRecipes = mDB.getReference(RECIPES);
        DatabaseReference DBRestaurants = mDB.getReference(RESTAURANTS);

        recipes = new ArrayList<>();
        restaurants = new ArrayList<>();

        DBRecipes.addValueEventListener(new CustomValueEventListener(ListType.RECIPES));
        DBRestaurants.addValueEventListener(new CustomValueEventListener(ListType.RESTAURANTS));

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
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
        CookInstructionFragment cif = CookInstructionFragment.newInstance(item, shp);
        ftrans.replace(R.id.fragment_container, cif).addToBackStack(null).commit();
        mDB.getReference().child("recipes").child(item.getId().toString()).child("views").setValue(item.getViews());
    }

    @Override
    public void onRecipeLikeFragmentInteraction(Recipe item) {
        String s = "recipe_like_" + item.getId();
        SharedPreferences.Editor editor = shp.edit();
        mDB.getReference().child("recipes").child(item.getId().toString()).child("rate").setValue(item.getRate());
        if (!shp.getBoolean(s, false))
            editor.putBoolean(s, true);
        else
            editor.putBoolean(s, false);
        editor.apply();
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }


    class CustomValueEventListener implements ValueEventListener {

        ListType listType;

        CustomValueEventListener(ListType listType) {
            this.listType = listType;
        }

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            switch (listType) {
                case RECIPES:
                    recipes.clear();
                    for (DataSnapshot unit : dataSnapshot.getChildren()) {
                        Recipe value = unit.getValue(Recipe.class);
                        recipes.add(value);
                    }
                    break;
                case RESTAURANTS:
                    restaurants.clear();
                    for (DataSnapshot unit : dataSnapshot.getChildren()) {
                        Restaurant value = unit.getValue(Restaurant.class);
                        restaurants.add(value);
                    }
                    break;
                default:
                    break;
            }
            Log.d("Value", "Value is: " + recipes);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.d("Error", "Failed to read value.");
        }
    }


    class ChangeFragment extends AsyncTask<String, Void, Void> {
        Fragment fragment;

        ChangeFragment(Fragment fragment) {
            this.fragment = fragment;
        }

        @Override
        protected Void doInBackground(String... srtings) {
            switch (srtings[0]) {
                case RECIPES:
                    while (recipes.isEmpty()) ;
                case RESTAURANTS:
                    while (restaurants.isEmpty()) ;
                default:
                    break;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            ftrans = getFragmentManager().beginTransaction();
            ftrans.replace(R.id.fragment_container, fragment).commit();
        }
    }

}

