package com.example.veganapp.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.veganapp.R;
import com.example.veganapp.activities.MainActivity;
import com.example.veganapp.custom_adapters.RecipeRecyclerViewAdapter;
import com.example.veganapp.db_classes.Ingredient;
import com.example.veganapp.db_classes.Recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FoundedRecipesFragment extends RecipesFragment {

    List<Ingredient> givenIngredients;
    List<Boolean> addTypeList;
    RecipeRecyclerViewAdapter foundedRecipes;
    TextView infoMessage;

    public FoundedRecipesFragment() {
    }

    public static FoundedRecipesFragment newInstance(List<Ingredient> ingredients, List<Boolean> addTypeList) {
        FoundedRecipesFragment fragment = new FoundedRecipesFragment();
        Bundle args = new Bundle();
        fragment.givenIngredients = ingredients;
        fragment.addTypeList = addTypeList;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        shp = ((MainActivity) getActivity()).getShp();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_foundedrecipes_list, container, false);

        infoMessage = view.findViewById(R.id.message_no_recipes_found);
        mProgressBar = Objects.requireNonNull(getActivity()).findViewById(R.id.load_data);
        Context context = view.getContext();
        RecyclerView recyclerView = view.findViewById(R.id.founded_recipes);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        foundedRecipes = new RecipeRecyclerViewAdapter(shp, mListener, mLikeListener, this, false);
        recyclerView.setAdapter(foundedRecipes);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getView().post(new Runnable() {
            @Override
            public void run() {
                readRecipeList();
                List<Recipe> temp = new ArrayList<>(recipes);
                for (Recipe recipe : temp) {
                    int counter = 0;
                    boolean containsBanned = false;
                    int size = recipe.getIngredients().size();
                    for (int i = 0; i < givenIngredients.size(); ++i) {
                        if (addTypeList.get(i)) {
                            if (recipe.getIngredients().contains(givenIngredients.get(i)))
                                ++counter;
                        } else {
                            if (recipe.getIngredients().contains(givenIngredients.get(i)))
                                containsBanned = true;
                        }

                    }
                    if (!containsBanned)
                        if ((double) counter / size >= 0.8)
                            foundedRecipes.addOrChange(recipe);
                }
                if (foundedRecipes.getRecipes().isEmpty())
                    infoMessage.setVisibility(View.VISIBLE);
            }
        });
    }
}
