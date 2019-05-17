package com.example.veganapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.veganapp.R;
import com.example.veganapp.activities.MainActivity;
import com.example.veganapp.db_classes.Recipe;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BaseRecipesFragment extends Fragment {

    protected static final String RECIPES_SERIALIZED_PATH = "/recipes_serialized";

    protected List<Recipe> mRecipeList;
    protected String mPath;
    protected SharedPreferences sharedPreferences;
    protected ProgressBar mProgressBar;

    protected OnRecipeListFragmentInteractionListener mListListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRecipeList = new ArrayList<>();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mProgressBar = ((MainActivity) Objects.requireNonNull(getContext())).findViewById(R.id.loading_data_progress_bar);
        sharedPreferences = ((MainActivity) Objects.requireNonNull(getContext())).getSharedPreferences();
    }

    void readRecipeList() {
        mRecipeList.clear();
        int iterator = 0;
        Recipe recipe;
        while (true) {
            if ((recipe = readRecipe(iterator++)) != null) {
                mRecipeList.add(recipe);
            }
            else
                break;
        }
        mProgressBar.setVisibility(View.GONE);
    }

    Recipe readRecipe(int id) {
        Recipe recipe = null;
        try {
            FileInputStream streamIn = new FileInputStream(mPath + id);
            ObjectInputStream objectinputstream = new ObjectInputStream(streamIn);
            recipe = (Recipe) objectinputstream.readObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return recipe;
    }

    void writeRecipeList() {
        for (Recipe recipe : mRecipeList) {
            writeRecipe(recipe, recipe.getId());
        }
    }

    void writeRecipe(Recipe recipe, int id) {
        try {
            FileOutputStream fout = new FileOutputStream(mPath + id);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(recipe);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface OnRecipeListFragmentInteractionListener {
        void onRecipeListFragmentInteraction(Recipe item, BaseRecipesFragment baseRecipesFragment);
    }

    public interface OnRecipeLikeFragmentInteractionListener {
        void onRecipeLikeFragmentInteraction(Recipe item);
    }
}
