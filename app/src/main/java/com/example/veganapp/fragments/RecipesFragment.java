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

import com.example.veganapp.MyRecipeRecyclerViewAdapter;
import com.example.veganapp.R;
import com.example.veganapp.db_classes.Recipe;

import java.util.ArrayList;
import java.util.List;


public class RecipesFragment extends Fragment {

    // TODO: Customize parameter argument names
    protected static final String ARG_COLUMN_COUNT = "column-count";
    protected static final String ARG_FILTER = "filter";
    // TODO: Customize parameters
    protected int mColumnCount = 1;
    protected OnRecipeListFragmentInteractionListener mListener;
    protected OnRecipeLikeFragmentInteractionListener mLikeListener;
    protected List<Recipe> recipes;
    protected List<Recipe> favRecipes;
    protected SharedPreferences shp;
    protected boolean filter;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RecipesFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static RecipesFragment newInstance(SharedPreferences shp, List<Recipe> recipes, int columnCount, boolean filter) {
        RecipesFragment fragment = new RecipesFragment();
        Bundle args = new Bundle();
        fragment.shp = shp;
        fragment.recipes = recipes;
        args.putBoolean(ARG_FILTER, filter);
        args.putInt(ARG_COLUMN_COUNT, columnCount);
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnRecipeListFragmentInteractionListener {
        void onRecipeListFragmentInteraction(Recipe item);
    }

    public interface OnRecipeLikeFragmentInteractionListener {
        void onRecipeLikeFragmentInteraction(Recipe item);
    }
}
