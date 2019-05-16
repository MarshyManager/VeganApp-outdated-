package com.example.veganapp.activities;

import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.example.veganapp.fragments.CookInstructionFragment;
import com.example.veganapp.fragments.DailyMenuFragment;
import com.example.veganapp.fragments.RecipeByIngredientFragment;
import com.example.veganapp.support_classes.LikeValueChanged;
import com.example.veganapp.support_classes.ViewsValueChanged;
import com.google.android.libraries.places.api.Places;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.example.veganapp.R;
import com.example.veganapp.db_classes.Recipe;
import com.example.veganapp.db_classes.Restaurant;

import com.example.veganapp.fragments.MapFragment;
import com.example.veganapp.fragments.RecipesFragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements RecipesFragment.OnRecipeListFragmentInteractionListener,
        RecipesFragment.OnRecipeLikeFragmentInteractionListener {

    static final String APP_PREFERENCES = "settings";
    static final String RECIPES = "recipes";
    static final String RESTAURANTS = "restaurants";
    static final String FULL_RECIPE = "full_recipe";
    static final String FOUNDED_RECIPES = "founded_recipes";
    protected static final String RECIPES_SER = "/recipes_ser";

    protected List<Recipe> recipes;
    protected List<Restaurant> restaurants;
    protected FirebaseDatabase mDB;
    protected FragmentManager fm;
    protected FragmentTransaction ftrans;
    protected SharedPreferences shp;
    protected DatabaseReference DBRecipes;
    protected DatabaseReference DBRestaurants;
    protected ProgressBar mProgressBar;
    protected BottomNavigationView navigation;
    protected String recipesPath;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            RecipesFragment recipesFragment;
            fm = getSupportFragmentManager();
            mProgressBar.setVisibility(View.VISIBLE);
            fm.popBackStack(FULL_RECIPE, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fm.popBackStack(FOUNDED_RECIPES, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            ftrans = fm.beginTransaction();
            switch (item.getItemId()) {
                case R.id.navigation_recipe_list:
                    recipesFragment = RecipesFragment.newInstance(APP_PREFERENCES, false);
                    ftrans.replace(R.id.fragment_container, recipesFragment).commit();
                    return true;
                case R.id.navigation_favourite:
                    recipesFragment = RecipesFragment.newInstance(APP_PREFERENCES, true);
                    ftrans.replace(R.id.fragment_container, recipesFragment).commit();
                    return true;
                case R.id.navigation_pick_recipe:
                    RecipeByIngredientFragment rbiFragment = RecipeByIngredientFragment.newInstance(recipesPath);
                    mProgressBar.setVisibility(View.GONE);
                    ftrans.replace(R.id.fragment_container, rbiFragment).commit();
                    return true;

                case R.id.navigation_create_menu:
                    DailyMenuFragment dailyMenuFragment = DailyMenuFragment.newInstance();
                    mProgressBar.setVisibility(View.GONE);
                    ftrans.replace(R.id.fragment_container, dailyMenuFragment).commit();
                    return true;
                case R.id.navigation_map:
                    MapFragment mapFragment = MapFragment.newInstance(restaurants);
                    ftrans.replace(R.id.fragment_container, mapFragment).commit();
                    mProgressBar.setVisibility(View.GONE);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recipesPath = getApplicationContext().getFilesDir().getPath() + RECIPES_SER;

        Places.initialize(getApplicationContext(), "AIzaSyCQ_4795C2OlIunuSiI7ku224GWfAVzIcY");

        shp = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);

        mProgressBar = findViewById(R.id.load_data);

        mDB = FirebaseDatabase.getInstance();
        DBRecipes = mDB.getReference(RECIPES);
        DBRestaurants = mDB.getReference(RESTAURANTS);

        DBRestaurants.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot unit : dataSnapshot.getChildren()) {
                    restaurants.add(unit.getValue(Restaurant.class));
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        recipes = new ArrayList<>();
        restaurants = new ArrayList<>();

        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        fm = getSupportFragmentManager();
        if (fm.getFragments().size() == 0) {
            ftrans = fm.beginTransaction();
            RecipesFragment recipesFragment = RecipesFragment.newInstance(APP_PREFERENCES, false);
            ftrans.replace(R.id.fragment_container, recipesFragment).commit();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onRecipeListFragmentInteraction(Recipe item, RecipesFragment recipesFragment) {
        ftrans = getSupportFragmentManager().beginTransaction();
        CookInstructionFragment cif = CookInstructionFragment.newInstance(item, APP_PREFERENCES, recipesFragment);
        ftrans.replace(R.id.fragment_container, cif).addToBackStack(FULL_RECIPE).commit();
        SharedPreferences.Editor editor = shp.edit();
        final String viewsNum = "views_dif_" + item.getId();
        editor.putInt(viewsNum, shp.getInt(viewsNum, 0) + 1);
        editor.apply();
        mDB.getReference(RECIPES + "/" + String.valueOf(item.getId()) + "/views")
                .addListenerForSingleValueEvent(new ViewsValueChanged(viewsNum, shp));
    }

    @Override
    public void onRecipeLikeFragmentInteraction(Recipe item) {
        final String offlineLike = "recipe_offline_like_" + item.getId();
        final String onlineLike = "recipe_online_like_" + item.getId();
        int dif;
        SharedPreferences.Editor editor = shp.edit();
        if (!shp.getBoolean(offlineLike, false)) {
            editor.putBoolean(offlineLike, true);
            dif = +1;
        } else {
            editor.putBoolean(offlineLike, false);
            dif = -1;
        }
        editor.apply();
        item.setRate(item.getRate() + dif);
        mDB.getReference(RECIPES + "/" + String.valueOf(item.getId()) + "/rate").addListenerForSingleValueEvent(new LikeValueChanged(offlineLike, onlineLike, shp));
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        View f = activity.getCurrentFocus();
        if (null != f && null != f.getWindowToken() && EditText.class.isAssignableFrom(f.getClass()))
            imm.hideSoftInputFromWindow(f.getWindowToken(), 0);
        else
            activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void onBackPressed() {
        if ((navigation.getSelectedItemId() != R.id.navigation_recipe_list)
                && (getSupportFragmentManager().getBackStackEntryCount() == 0))
            navigation.setSelectedItemId(R.id.navigation_recipe_list);
        else
            super.onBackPressed();
    }

    public SharedPreferences getShp() {
        return shp;
    }
}


