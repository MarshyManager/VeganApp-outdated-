package com.example.veganapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.veganapp.custom_adapters.MyRecipeRecyclerViewAdapter;
import com.example.veganapp.R;
import com.example.veganapp.db_classes.Recipe;
import com.google.firebase.FirebaseException;

import java.util.ArrayList;
import java.util.List;


public class RecipesFragment extends Fragment {

    protected static final String ARG_COLUMN_COUNT = "column-count";
    protected static final String ARG_FILTER = "filter";
    static final String SHARED_PREFERENCES = "shared_preferences";
    final String RECIPES = "recipes";


    protected int mColumnCount = 1;
    protected OnRecipeListFragmentInteractionListener mListener;
    protected OnRecipeLikeFragmentInteractionListener mLikeListener;
    protected List<Recipe> recipes;
    protected List<Recipe> favRecipes;
    protected SharedPreferences shp;
    protected boolean filter;

    public RecipesFragment() {
    }

    public static RecipesFragment newInstance(String sharedPreferences, List<Recipe> recipes, int columnCount, boolean filter) {
        RecipesFragment fragment = new RecipesFragment();
        Bundle args = new Bundle();
        fragment.recipes = recipes;
        args.putBoolean(ARG_FILTER, filter);
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putString(SHARED_PREFERENCES, sharedPreferences);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
            filter = getArguments().getBoolean(ARG_FILTER);
        }
        if (savedInstanceState != null) {
            recipes = (ArrayList<Recipe>) savedInstanceState.getSerializable(RECIPES);
            shp = getActivity().getSharedPreferences(savedInstanceState.getString(SHARED_PREFERENCES), Context.MODE_PRIVATE);
            mColumnCount = savedInstanceState.getInt(ARG_COLUMN_COUNT);
            filter = savedInstanceState.getBoolean(ARG_FILTER);
        }
        shp = getActivity().getSharedPreferences(getArguments().getString(SHARED_PREFERENCES), Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_list, container, false);

        favRecipes = new ArrayList<>();
        favRecipes.addAll(recipes);
        if (filter) {
            for (int i = 0; i < favRecipes.size(); i++) {
                if (!shp.getBoolean("recipe_like_" + favRecipes.get(i).getId(), false))
                    favRecipes.remove(i--);
            }
        }


        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(new MyRecipeRecyclerViewAdapter(favRecipes, shp, mListener, mLikeListener));
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnRecipeLikeFragmentInteractionListener) {
            mListener = (OnRecipeListFragmentInteractionListener) context;
            mLikeListener = (OnRecipeLikeFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(RECIPES, (ArrayList)recipes);
        outState.putInt(ARG_COLUMN_COUNT, getArguments().getInt(ARG_COLUMN_COUNT));
        outState.putBoolean(ARG_FILTER, filter);
        outState.putString(SHARED_PREFERENCES, getArguments().getString(SHARED_PREFERENCES));
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnRecipeListFragmentInteractionListener {
        void onRecipeListFragmentInteraction(Recipe item);
    }

    public interface OnRecipeLikeFragmentInteractionListener {
        void onRecipeLikeFragmentInteraction(Recipe item) throws FirebaseException;
    }
}
