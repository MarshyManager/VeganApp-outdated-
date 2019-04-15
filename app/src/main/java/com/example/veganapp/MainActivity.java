package com.example.veganapp;

import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SupportInterfaces.OnRecipeListFragmentInteractionListener,
        SupportInterfaces.OnRecipeLikeFragmentInteractionListener {

    static final String APP_PREFERENCES = "settings";

    protected JsonClasses.MainJson RnR;
    protected List<JsonClasses.Recipe> recipes;
    protected List<JsonClasses.Restaurant> restaurants;
    protected StorageReference mStorageRef;
    protected FragmentTransaction ftrans;
    protected SharedPreferences shp;
    protected SupportClasses.FirebaseInfoLoader fil;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            RecipesFragment recipesFragment;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    ftrans = getFragmentManager().beginTransaction();
                    recipesFragment = RecipesFragment.newInstance(shp, recipes, 1, false);
                    ftrans.replace(R.id.fragment_container, recipesFragment);
                    ftrans.commit();
                    return true;
                case R.id.navigation_dashboard:
                    ftrans = getFragmentManager().beginTransaction();
                    recipesFragment = RecipesFragment.newInstance(shp, recipes, 1, true);
                    ftrans.replace(R.id.fragment_container, recipesFragment);
                    ftrans.commit();
                    return true;
                case R.id.navigation_notifications:
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        StorageReference baseInfoRef = mStorageRef.child("recipes.json");
        fil = new SupportClasses().new FirebaseInfoLoader(this);
        RnR = fil.getInfoFromFirebase(baseInfoRef);

        if (RnR != null) {
            recipes = RnR.getRecipes();
            restaurants = RnR.getRestaurants();
        }

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        shp = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);
    }

    @Override
    public void onRecipeListFragmentInteraction(JsonClasses.Recipe item) {
        ftrans = getFragmentManager().beginTransaction();
        CookInstructionFragment cif = CookInstructionFragment.newInstance(item, shp);
        ftrans.replace(R.id.fragment_container, cif);
        ftrans.addToBackStack(null);
        ftrans.commit();
    }

    @Override
    public void onRecipeLikeFragmentInteraction(JsonClasses.Recipe item) {
        String s = "recipe_like_" + item.getId();
        SharedPreferences.Editor editor = shp.edit();
        if (!shp.getBoolean(s, false))
            editor.putBoolean(s, true);
        else
            editor.putBoolean(s, false);
        editor.apply();
        StorageReference baseInfoRef = mStorageRef.child("recipes.json");
        fil.getInfoFromFirebase(baseInfoRef);
        RnR.getRecipes().set(item.getId(), item);
        fil.putInfoIntoFirebase(baseInfoRef, RnR);
    }
}
