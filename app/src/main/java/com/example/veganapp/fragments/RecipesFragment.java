package com.example.veganapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.veganapp.custom_adapters.MyRecipeRecyclerViewAdapter;
import com.example.veganapp.R;
import com.example.veganapp.db_classes.Recipe;
import com.google.firebase.FirebaseException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jp.wasabeef.recyclerview.animators.LandingAnimator;

public class RecipesFragment extends Fragment {

    protected static final String ARG_COLUMN_COUNT = "column-count";
    protected static final String ARG_FILTER = "filter";
    protected final String RECIPES = "recipes";
    protected static final String SHARED_PREFERENCES = "shared_preferences";
    protected static final String SORT_TYPE = "sort_type";

    protected int mColumnCount = 1;
    protected OnRecipeListFragmentInteractionListener mListener;
    protected OnRecipeLikeFragmentInteractionListener mLikeListener;
    protected SharedPreferences shp;
    protected boolean filter;
    protected MyRecipeRecyclerViewAdapter recyclerViewAdapter;
    protected String[] sortParams;
    protected int sortType;


    public RecipesFragment() {
    }

    public static RecipesFragment newInstance(String sharedPreferences, int columnCount, boolean filter) {
        RecipesFragment fragment = new RecipesFragment();
        Bundle args = new Bundle();
        args.putInt(SORT_TYPE, -1);
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
            sortType = getArguments().getInt(SORT_TYPE);
            shp = getActivity().getSharedPreferences(getArguments().getString(SHARED_PREFERENCES), Context.MODE_PRIVATE);
        }
        if (savedInstanceState != null) {
            mColumnCount = savedInstanceState.getInt(ARG_COLUMN_COUNT);
            filter = savedInstanceState.getBoolean(ARG_FILTER);
            sortType = savedInstanceState.getInt(SORT_TYPE);
            shp = getActivity().getSharedPreferences(savedInstanceState.getString(SHARED_PREFERENCES), Context.MODE_PRIVATE);
        }

        recyclerViewAdapter = new MyRecipeRecyclerViewAdapter(shp, mListener, mLikeListener);
        DatabaseReference recipesRef = FirebaseDatabase.getInstance().getReference().child(RECIPES);
        recipesRef.addValueEventListener(new CustomRecipeValueEventListener());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_list, container, false);

        Context context = view.getContext();
        final RecyclerView recyclerView = view.findViewById(R.id.recipe_adapter);
        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }

        recyclerView.setItemAnimator(new LandingAnimator());

        recyclerView.setAdapter(recyclerViewAdapter);

        sortParams = getResources().getStringArray(R.array.sort_params);

        Spinner sortSpinner = view.findViewById(R.id.sort_spinner);
        final SortSpinnerCustomAdapter spinnerCustomAdapter = new SortSpinnerCustomAdapter(view.getContext(), R.layout.sort_spinner_item, sortParams);
        spinnerCustomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(spinnerCustomAdapter);
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            boolean isFirstTime = true;

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (isFirstTime)
                {
                    isFirstTime = false;
                    return;
                }
                recyclerViewAdapter.sort(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

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

    class CustomRecipeValueEventListener implements ValueEventListener {

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            if (filter) {
                for (DataSnapshot unit : dataSnapshot.getChildren()) {
                    Recipe recipe = unit.getValue(Recipe.class);
                    if (shp.getBoolean("recipe_like_" + recipe.getId(), false))
                        recyclerViewAdapter.addOrChange(recipe, recipe.getId());
                }
            } else {
                for (DataSnapshot unit : dataSnapshot.getChildren()) {
                    Recipe recipe = unit.getValue(Recipe.class);
                    recyclerViewAdapter.addOrChange(recipe, recipe.getId());
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.d("Error", "Failed to read value.");
        }
    }

    public class SortSpinnerCustomAdapter extends ArrayAdapter<String> {

        SortSpinnerCustomAdapter(Context context, int textViewResourceId,
                                        String[] objects) {
            super(context, textViewResourceId, objects);

        }


        @Override
        public View getDropDownView(int position, View convertView,
                                    ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            return getCustomView(position, convertView, parent);
        }

        public View getCustomView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = getActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.sort_spinner_item, parent, false);

            ImageView iv = view.findViewById(R.id.sort_type);
            TextView tv = view.findViewById(R.id.sort_param_name);

            tv.setText(sortParams[position]);
            if (position != 0) {
                if (position % 2 == 0)
                    iv.setImageResource(R.drawable.sort_descending);
                else
                    iv.setImageResource(R.drawable.sort_ascending);
            }
            return view;
        }
    }
}
