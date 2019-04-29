package com.example.veganapp.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.example.veganapp.R;
import com.example.veganapp.custom_adapters.IngredientAdapter;
import com.example.veganapp.custom_adapters.MyIngredientsRecyclerViewAdapter;
import com.example.veganapp.db_classes.Ingredient;
import com.example.veganapp.db_classes.Recipe;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;


public class RecipeByIngredientFragment extends Fragment {

    protected List<Recipe> recipes;
    protected HashSet<String> uniqueIngredients;
    protected Toolbar mToolbar;
    protected String path;
    RecyclerView recyclerView;
    IngredientAdapter ingredientAdapter;


    public RecipeByIngredientFragment() {
    }


    public static RecipeByIngredientFragment newInstance(String path) {
        RecipeByIngredientFragment fragment = new RecipeByIngredientFragment();
        Bundle args = new Bundle();
        fragment.path = path;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recipes = new ArrayList<>();
        uniqueIngredients = new HashSet<>();
        readRecipeList();
        chooseIngredients();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ingredient_list, container, false);
        mToolbar = view.findViewById(R.id.toolbar_ingredient);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(new MyIngredientsRecyclerViewAdapter());
        }
        List<String> ingredientList = new ArrayList<>(uniqueIngredients);
        ingredientAdapter = new IngredientAdapter(ingredientList);
        setHasOptionsMenu(true);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("");
        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull final Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.options_menu_ingridients_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem mAddItem = menu.findItem(R.id.options_add);
        mAddItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View view = inflater.inflate(R.layout.ingredient_dialog, null);
                recyclerView = view.findViewById(R.id.ingredients_search_adapter);
                recyclerView.setAdapter(ingredientAdapter);
                builder.setView(view).show();
                return true;
            }
        });
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    void chooseIngredients() {
        for (Recipe recipe : recipes) {
            for (Ingredient ingredient : recipe.getIngredients()) {
                uniqueIngredients.add(ingredient.getName());
            }
        }
    }

    void readRecipeList() {
        recipes.clear();
        int iter = 0;
        Recipe recipe;
        while (true) {
            if ((recipe = readRecipe(iter++)) != null)
                recipes.add(recipe);
            else
                break;
        }
    }

    Recipe readRecipe(int id) {
        Recipe recipe = null;
        try {
            FileInputStream streamIn = new FileInputStream(path + id);
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
}
