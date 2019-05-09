package com.example.veganapp.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import com.example.veganapp.R;
import com.example.veganapp.custom_adapters.IngredientsDialogAdapter;
import com.example.veganapp.custom_adapters.ChosenIngredientsAdapter;
import com.example.veganapp.db_classes.Ingredient;
import com.example.veganapp.db_classes.Recipe;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class RecipeByIngredientFragment extends Fragment {

    final static String FOUNDED_RECIPES = "founded_recipes";

    protected List<Recipe> recipes;
    protected Set<Ingredient> uniqueIngredients;
    protected Toolbar mToolbar;
    protected String path;
    RecyclerView recyclerViewDialog;
    RecyclerView recyclerViewChosen;
    IngredientsDialogAdapter ingredientsDialogAdapter;
    ChosenIngredientsAdapter chosenIngredientsAdapter;
    Button findRecipes;
    TextView hideDialog;

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
        getUniqueIngredients();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ingredient_list, container, false);
        mToolbar = view.findViewById(R.id.toolbar_ingredient);
        findRecipes = view.findViewById(R.id.find_recipes);
        Context context = view.getContext();
        recyclerViewChosen = view.findViewById(R.id.ingredient_list);
        recyclerViewChosen.setLayoutManager(new LinearLayoutManager(context));
        chosenIngredientsAdapter = new ChosenIngredientsAdapter(findRecipes);
        recyclerViewChosen.setAdapter(chosenIngredientsAdapter);
        setHasOptionsMenu(true);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("");

        findRecipes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction ftrans = getActivity().getSupportFragmentManager().beginTransaction();
                FoundedRecipesFragment frf = FoundedRecipesFragment.newInstance(chosenIngredientsAdapter.getIngredients());
                ftrans.replace(R.id.fragment_container, frf).addToBackStack(FOUNDED_RECIPES).commit();
            }
        });
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
                List<Ingredient> ingredientList = new ArrayList<>(uniqueIngredients);
                Collections.sort(ingredientList, new Comparator<Ingredient>() {
                    @Override
                    public int compare(Ingredient lhs, Ingredient rhs) {
                        return lhs.getName().compareTo(rhs.getName());
                    }
                });
                ingredientsDialogAdapter = new IngredientsDialogAdapter(ingredientList, (ChosenIngredientsAdapter) recyclerViewChosen.getAdapter(), findRecipes);
                recyclerViewDialog = view.findViewById(R.id.ingredients_search_adapter);
                hideDialog = view.findViewById(R.id.hide_dialog);
                recyclerViewDialog.setLayoutManager(new LinearLayoutManager(view.getContext()));
                recyclerViewDialog.setAdapter(ingredientsDialogAdapter);
                SearchView mSearchView = view.findViewById(R.id.search_ingredients);
                if (mSearchView != null) {
                    mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            onQueryTextChange(query);
                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                            return true;
                        }

                        @Override
                        public boolean onQueryTextChange(String newText) {
                            ingredientsDialogAdapter.getFilter().filter(newText);
                            return true;
                        }
                    });
                    mSearchView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((SearchView) v).setIconified(false);
                        }
                    });
                }
                final AlertDialog alertDialog = builder.setView(view).create();
                alertDialog.show();
                hideDialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });
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

    void getUniqueIngredients() {
        for (Recipe recipe : recipes) {
            for (Ingredient ingredient : recipe.getIngredients()) {
                uniqueIngredients.add(ingredient);
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
