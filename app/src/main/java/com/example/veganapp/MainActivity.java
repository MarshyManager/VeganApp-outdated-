package com.example.veganapp;

import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class MainActivity extends AppCompatActivity implements RecipesFragment.OnListFragmentInteractionListener,
        CookInstructionFragment.OnFragmentInteractionListener, RecipesFragment.OnLikeFragmentInteractionListener {

    final String APP_PREFERENCES = "settings";

    protected JsonClasses.MainJson RnR;
    protected List<JsonClasses.Recipe> recipes;
    protected List<JsonClasses.Restaurant> restaurants;
    protected StorageReference mStorageRef;
    protected FragmentTransaction ftrans;
    protected SharedPreferences mShp;
    protected RecipesFragment recipesFragment;

    final long ONE_MEGABYTE = 1024 * 1024;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    ftrans = getFragmentManager().beginTransaction();
                    recipesFragment = RecipesFragment.newInstance(1, false);
                    ftrans.replace(R.id.fragment_container, recipesFragment);
                    ftrans.commit();
                    return true;
                case R.id.navigation_dashboard:
                    ftrans = getFragmentManager().beginTransaction();
                    recipesFragment = RecipesFragment.newInstance(1, true);
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
        getInfoFromFirebase(baseInfoRef);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        mShp = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);
    }

    void getInfoFromFirebase(final StorageReference targetRef) {

        targetRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                try {
                    FileOutputStream fout = openFileOutput(targetRef.getName(), MODE_PRIVATE);
                    fout.write(bytes);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        });

        try {
            FileInputStream fin = openFileInput(targetRef.getName());
            Gson gson = new Gson();
            BufferedReader br = new BufferedReader(new InputStreamReader(fin));

            StringBuilder sb = new StringBuilder();
            String s;
            while ((s = br.readLine()) != null) {
                sb.append(s).append("\n");
            }
            RnR = gson.fromJson(sb.toString(), JsonClasses.MainJson.class);
            if (RnR != null) {
                recipes = RnR.getRecipes();
                restaurants = RnR.getRestaurants();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void putInfoIntoFirebase(final StorageReference targetRef) {

        Gson gson = new Gson();
        String r_n_r = gson.toJson(RnR);
        byte[] bytes = r_n_r.getBytes();

        targetRef.putBytes(bytes).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot snapshot) {

            }
        });


        try {
            FileOutputStream fout = openFileOutput(targetRef.getName(), MODE_PRIVATE);
            fout.write(bytes);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onListFragmentInteraction(JsonClasses.Recipe item) {
        ftrans = getFragmentManager().beginTransaction();
        ftrans.replace(R.id.fragment_container, new CookInstructionFragment());
        ftrans.addToBackStack(null);
        ftrans.commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onLikeFragmentInteraction(JsonClasses.Recipe item) {
        StorageReference baseInfoRef = mStorageRef.child("recipes.json");
        getInfoFromFirebase(baseInfoRef);
        RnR.getRecipes().set(item.getId(), item);
        putInfoIntoFirebase(baseInfoRef);
    }

    public List<JsonClasses.Recipe> getRecipes() {
        return recipes;
    }

    public void setRecipes(List<JsonClasses.Recipe> recipes) {
        this.recipes = recipes;
    }

    public List<JsonClasses.Restaurant> getRestaurants() {
        return restaurants;
    }

    public void setRestaurants(List<JsonClasses.Restaurant> restaurants) {
        this.restaurants = restaurants;
    }

    public SharedPreferences getShp() {
        return mShp;
    }

    public void setShp(SharedPreferences mShp) {
        this.mShp = mShp;
    }
}
