package com.example.veganapp.activities;

import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.example.veganapp.fragments.BaseRecipesFragment;
import com.example.veganapp.fragments.CookInstructionFragment;
import com.example.veganapp.fragments.DailyMenuFragment;
import com.example.veganapp.fragments.RecipeByIngredientFragment;
import com.example.veganapp.support_classes.ImportantConstants;
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

import static com.example.veganapp.support_classes.ImportantConstants.FOUNDED_RECIPES;
import static com.example.veganapp.support_classes.ImportantConstants.FULL_RECIPE;
import static com.example.veganapp.support_classes.ImportantConstants.RECIPES;
import static com.example.veganapp.support_classes.ImportantConstants.RESTAURANTS;

public class MainActivity extends AppCompatActivity implements BaseRecipesFragment.OnRecipeListFragmentInteractionListener,
        BaseRecipesFragment.OnRecipeLikeFragmentInteractionListener {

    static final String APP_PREFERENCES = "settings";
    protected static final String RECIPES_SER = "/recipes_ser";

    protected List<Recipe> recipes;
    protected List<Restaurant> restaurants;
    protected FirebaseDatabase mDB;
    protected FragmentManager fm;
    protected FragmentTransaction ftrans;
    protected SharedPreferences sharedPreferences;
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
                    recipesFragment = RecipesFragment.newInstance( false);
                    ftrans.replace(R.id.fragment_container, recipesFragment).commit();
                    return true;
                case R.id.navigation_favourite:
                    recipesFragment = RecipesFragment.newInstance(true);
                    ftrans.replace(R.id.fragment_container, recipesFragment).commit();
                    return true;
                case R.id.navigation_pick_recipe:
                    RecipeByIngredientFragment rbiFragment = RecipeByIngredientFragment.newInstance(recipesPath);
                    mProgressBar.setVisibility(View.GONE);
                    ftrans.replace(R.id.fragment_container, rbiFragment).commit();
                    return true;
                case R.id.navigation_create_menu:
                    DailyMenuFragment dailyMenuFragment = DailyMenuFragment.newInstance();
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

        sharedPreferences = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);

        mProgressBar = findViewById(R.id.loading_data_progress_bar);

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
            RecipesFragment recipesFragment = RecipesFragment.newInstance( false);
            ftrans.replace(R.id.fragment_container, recipesFragment).commit();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public void onRecipeListFragmentInteraction(Recipe item, BaseRecipesFragment baseRecipesFragment) {
        ftrans = getSupportFragmentManager().beginTransaction();
        CookInstructionFragment cif = CookInstructionFragment.newInstance(item, APP_PREFERENCES, baseRecipesFragment);
        ftrans.replace(R.id.fragment_container, cif).addToBackStack(FULL_RECIPE).commit();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        final String viewsNum = "views_dif_" + item.getId();
        editor.putInt(viewsNum, sharedPreferences.getInt(viewsNum, 0) + 1);
        editor.apply();
        mDB.getReference(RECIPES + "/" + item.getId() + "/views")
                .addListenerForSingleValueEvent(new ViewsValueChanged(viewsNum, sharedPreferences));
    }

    @Override
    public void onRecipeLikeFragmentInteraction(Recipe item) {
        final String offlineLike = ImportantConstants.RECIPES_OFFLINE_LIKE_TAG + item.getId();
        final String onlineLike = ImportantConstants.RECIPES_ONLINE_LIKE_TAG + item.getId();
        int dif;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (!sharedPreferences.getBoolean(offlineLike, false)) {
            editor.putBoolean(offlineLike, true);
            dif = +1;
        } else {
            editor.putBoolean(offlineLike, false);
            dif = -1;
        }
        editor.apply();
        item.setRate(item.getRate() + dif);
        mDB.getReference(RECIPES + "/" + item.getId() + "/rate").addListenerForSingleValueEvent(new LikeValueChanged(offlineLike, onlineLike, sharedPreferences));
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

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }
}


